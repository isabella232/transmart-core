package org.transmartproject.rest.client.utils

import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import org.transmartproject.core.exceptions.InvalidRequestException

@Slf4j
@Component
class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return (
                httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                        || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR)
    }

    @Override
    void handleError(ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            log.error "Server error occurred: ${httpResponse.getStatusCode()}"
        } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            log.error "Client error occurred: ${httpResponse.getStatusCode()}"
            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new InvalidRequestException("Error occurred for a request: ${httpResponse.connection.url.toString()}. " +
                        "Status: ${httpResponse.getStatusCode()}")
            }
        } else {
            log.error("Http error occured: ${httpResponse.statusCode}")
        }
    }
}
