package org.transmartproject.rest.client.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.transmartproject.rest.client.KeycloakRestClient

@Slf4j
@CompileStatic
class ImpersonationInterceptor implements ClientHttpRequestInterceptor {

    private static String impersonatedUserName

    ImpersonationInterceptor(String username) {
        impersonatedUserName = username
    }

    @Override
    ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders()
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            def accessToken = new KeycloakRestClient().getImpersonatedTokenByOfflineTokenForUser(impersonatedUserName)
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        }
        return execution.execute(request, body)
    }
}
