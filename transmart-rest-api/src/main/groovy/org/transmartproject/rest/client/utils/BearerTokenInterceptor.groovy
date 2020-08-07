package org.transmartproject.rest.client.utils

import groovy.transform.CompileStatic
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

@CompileStatic
class BearerTokenInterceptor implements ClientHttpRequestInterceptor {
    private static String tokenString

    BearerTokenInterceptor(String tokenString) {
        this.tokenString = tokenString
    }

    @Override
    ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders()
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenString)
        }
        return execution.execute(request, body)
    }
}
