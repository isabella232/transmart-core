package org.transmartproject.rest

import grails.validation.ValidationException
import groovy.util.logging.Slf4j
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

        def invite = user.getPublicInvitation()

        def existingRecord = Contact.findWhere(queryId: queryID, user_id: user.getUsername())

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

        Contact contactRecord = new Contact(
                user_id: user.getUsername(),
                count: count,
                queryId: queryId,
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

        def existingRecord = Contact.findWhere(queryId: queryID, user_id: user.getUsername())
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
