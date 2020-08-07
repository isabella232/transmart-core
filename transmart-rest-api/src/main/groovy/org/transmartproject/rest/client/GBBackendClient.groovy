package org.transmartproject.rest.client

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.transmartproject.rest.marshallers.QueryRepresentation

@Component
@Slf4j
@CompileStatic
class GBBackendClient extends AbstractRestClient {

    @Value('${glowingbear.backend-url')
    private String backendUrl

    /**
     * Fetch the query by id for a given user
     * @param queryId - query identifier
     * @return QueryRepresentation
     */
    QueryRepresentation getQuery(Long queryId) {
        URI uri = URI.create("${backendUrl}/queries/${queryId}")
        postAsCurrentUser(uri, null, QueryRepresentation.class)
    }
}
