package org.transmartproject.db.contact

import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.contact.ContactResource
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
    int contactForConstraint(Constraint constraint, User user) {
        String constraintJSON = constraint.toJson()
        String hash =  constraintJSON.encodeAsSHA1()

        Hypercube data = multiDimensionalDataResource.retrieveClinicalData(constraint, user)

        def did = []
        for (Patient patient : data.dimensionElements(DimensionImpl.PATIENT)) {
            patient.subjectIds.each { k, v ->
                did.add(v)
            }
        }

        // TODO: Make the call to inform the DIDs
        def count = did.size()
        def invite = user.getPublicInvitation()
        log.info "Contacting patients with identifiers ${did} and sending them `${invite}`"


        Contact contactRecord = new Contact(
                hash: hash,
                user_id: user.getUsername(),
                count: count,
        )
        contactRecord.save(flush: true, failOnError: true)

        count
    }
}
