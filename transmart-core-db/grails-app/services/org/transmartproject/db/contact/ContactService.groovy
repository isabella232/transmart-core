package org.transmartproject.db.contact

import com.google.common.collect.Sets
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.contact.ContactResource
import org.transmartproject.core.contact.ContactResponse
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.multidimquery.Hypercube
import org.transmartproject.core.multidimquery.MultiDimensionalDataResource
import org.transmartproject.core.multidimquery.query.Constraint
import org.transmartproject.core.users.User
import org.transmartproject.db.multidimquery.DimensionImpl

class ContactService implements ContactResource {

    @Autowired
    SessionFactory sessionFactory

    @Autowired
    MultiDimensionalDataResource multiDimensionalDataResource

    @Override
    ContactResponse contactForConstraint(Constraint constraint, User user) {
        String constraintJSON = constraint.toJson()
        String hash =  constraintJSON.encodeAsSHA1()

        def invite = user.getPublicInvitation()

        def existingRecord = Contact.findWhere(hash: hash, user_id: user.getUsername())

        Hypercube data = multiDimensionalDataResource.retrieveClinicalData(constraint, user)

        def did = new HashSet<String>()
        for (Patient patient : data.dimensionElements(DimensionImpl.PATIENT)) {
            patient.subjectIds.each { k, v ->
                did.add(v)
            }
        }

        if (existingRecord != null) {
            def diff = Sets.difference(did, existingRecord.contacted)
            if (diff.isEmpty()) {
                log.info "You've already contacted all patients with `${invite}"
                return new ContactResponse(
                        previouslyContacted: true,
                        previousContactCount: existingRecord.count,
                        contactCount: existingRecord.count
                )
            }
            log.info "Contacting ${diff} and sending them `${invite}`"

            // TODO: Update the count and record
            return new ContactResponse(
                    previouslyContacted: true,
                    previousContactCount: existingRecord.count,
                    contactCount: existingRecord.count
            )
        }

        // TODO: Make the call to inform the DIDs and update the count
        def count = did.size()
        log.info "Contacting patients with identifiers ${did} and sending them `${invite}`"


        Contact contactRecord = new Contact(
                hash: hash,
                user_id: user.getUsername(),
                count: count,
                contacted: did, // TODO: Remove failed contacts from this set
        )
        contactRecord.save(flush: true, failOnError: true)

        new ContactResponse(
                previouslyContacted: false,
                previousContactCount: contactRecord.count,
                contactCount: contactRecord.count
        )
    }
}
