package org.transmartproject.rest

import grails.converters.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestParam
import org.transmartproject.core.contact.ContactResource
import org.transmartproject.core.multidimquery.query.Constraint

class ContactController extends AbstractQueryController {

    @Autowired
    ContactResource contactResource

    def save(@RequestParam('api_version') String apiVersion) {
        def args = getGetOrPostParams()

        Constraint constraint = bindConstraint((String) args.constraint)
        def contactResponse = contactResource.contactForConstraint(constraint, authContext.user)
        render contactResponse as JSON
    }
}
