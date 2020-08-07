package org.transmartproject.rest

import grails.converters.JSON
import grails.web.http.HttpHeaders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.transmartproject.core.contact.ContactResource
import org.transmartproject.core.multidimquery.query.Constraint

class ContactController extends AbstractQueryController {

    @Autowired
    ContactResource contactResource

    def save(@RequestParam('api_version') String apiVersion) {
        def args = getGetOrPostParams()

        int queryId = args.queryId
        String synopsis = args.synopsis
        try {
            def contactResponse = contactResource.contactForQuery(queryId, synopsis, authContext.user)
            render contactResponse as JSON
        } catch (e) {
            response.sendError(400)
        }
    }

    def get(@RequestParam('api_version') String apiVersion, @PathVariable('queryId') String queryId) {
        try {
            def contactResponse = contactResource.getContactRecord(queryId, authContext.user)
            render contactResponse as JSON
        } catch (e) {
            response.sendError(404)
        }
    }
}
