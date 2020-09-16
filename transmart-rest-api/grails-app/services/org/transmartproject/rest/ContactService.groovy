package org.transmartproject.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.annotations.JsonAdapter
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.grails.web.converters.exceptions.ConverterException
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.contact.ContactResource
import org.transmartproject.core.contact.ContactResponse
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.multidimquery.Hypercube
import org.transmartproject.core.multidimquery.MultiDimensionalDataResource
import org.transmartproject.core.multidimquery.query.Constraint
import org.transmartproject.core.multidimquery.query.ConstraintFactory
import org.transmartproject.core.users.User
import org.transmartproject.db.contact.Contact
import org.transmartproject.db.multidimquery.DimensionImpl
import org.transmartproject.rest.client.GBBackendClient
import org.transmartproject.rest.marshallers.QueryRepresentation


@Slf4j
class ContactService implements ContactResource {

    @Autowired
    MultiDimensionalDataResource multiDimensionalDataResource

    @Autowired
    GBBackendClient client

    private Constraint parseConstraint(Object constraint) {
        if (constraint == null) {
            throw new InvalidArgumentsException('Empty constraint parameter.')
        }
        try {
            def result = ConstraintFactory.create(constraint)
            if (result) {
                return result.normalise()
            }
            throw new InvalidArgumentsException("Invalid constraint parameter: ${constraintText}")
        } catch (ConverterException c) {
            throw new InvalidArgumentsException("Cannot parse constraint parameter: ${constraintText}", c)
        }
    }

    @Override
    ContactResponse contactForQuery(Long queryId, String synopsis, User user) {
        log.info "Getting query representation"
        QueryRepresentation queryRepresentation = client.getQuery(queryId)
        Constraint constraint = parseConstraint(queryRepresentation.queryConstraint)
        log.info "Got constraint ${constraint.toJson()}"
//
        def invite = user.getPublicInvitation()

        def existingRecord = Contact.findWhere(query_id: queryId, user_id: user.getUsername())

        Hypercube data = multiDimensionalDataResource.retrieveClinicalData(constraint, user)

        def did = new HashSet<String>()
        for (Patient patient : data.dimensionElements(DimensionImpl.PATIENT)) {
            patient.subjectIds.each { k, v ->
                did.add(v)
            }
        }

        if (existingRecord != null) {
            log.info "Existing records found for this query id"
            throw new ValidationException("Record already exists for this queryId")
        }

        // TODO: count would be the number of DIDs that received the message successfully
        def count = did.size()
        log.info "Contacting patients with identifiers ${did} and sending them `${invite}`, `${synopsis}` and `${queryId}"

        def didList = new ArrayList<String>()
        for (String id : did) {
            if (id.startsWith("did:sov")) {
                didList.add(id)
            }
        }

        if (didList.size() > 0) {
            ObjectMapper objectMapper = new ObjectMapper()
            HttpClient client = HttpClients.createDefault()
            HttpPost httpPost = new HttpPost("http://localhost:6001/api/1.0/cohort")

            Invite inviteObj = objectMapper.readValue(invite, Invite.class)
            def req = new CreateCohortRequest(did: didList, synopsis: synopsis, cohortID: new String(queryId), invite: inviteObj)
            def marshaled = objectMapper.writeValueAsString(req)
            def marshaledEntity = new StringEntity(marshaled, ContentType.APPLICATION_JSON)

            log.info "Sending request with body: ${marshaled}"

            httpPost.setEntity(marshaledEntity)
            httpPost.setHeader("Content-Type", "application/json")

            def response = client.execute(httpPost)
            client.close()
        } else {
            log.info "No did found"
        }

        Contact contactRecord = new Contact(
                user_id: user.getUsername(),
                count: count,
                query_id: queryId,
                synopsis: synopsis,
        )
        contactRecord.save(flush: true, failOnError: true)

        // TODO: Handle retries if contactCount != totalCount
        new ContactResponse(
                synopsis: synopsis,
                contactCount: contactRecord.count,
                totalCount: did.size()
        )
    }

    @Override
    ContactResponse getContactRecord(Long queryId, User user) {
        log.info "Getting the query representation"
        QueryRepresentation queryRepresentation = client.getQuery(queryId)
        log.info "Got query representation"

        def existingRecord = Contact.findWhere(query_id: queryId, user_id: user.getUsername())
        log.info "Searched for existing record"

        if (existingRecord == null) {
            log.info "No such record found"
            return new NoSuchResourceException()
        }

        new ContactResponse(
                synopsis: existingRecord.synopsis,
                contactCount: existingRecord.count,
                totalCount: existingRecord.count,
        )
    }
}

class CreateCohortRequest {
    List<String> did
    Invite invite
    String synopsis
    String cohortID
}

class Invite {
    @JsonProperty("@id")
    String id
    String label
    String did
    @JsonProperty("@type")
    String type
}
