package org.transmartproject.rest.client

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.rest.client.utils.BearerTokenInterceptor
import org.transmartproject.rest.client.utils.ImpersonationInterceptor
import org.transmartproject.rest.client.utils.RestTemplateResponseErrorHandler
import org.transmartproject.rest.user.AuthContext

@Component
@Slf4j
@CompileStatic
abstract class AbstractRestClient {

    @Autowired
    AuthContext authContext

    /**
     * Do not set this flag to true in production!
     */
    @Lazy
    private Boolean disableTrustManager = {
        Holders.config.getProperty('disable-trust-manager', Boolean, false)
    }()

    private static HttpHeaders getJsonHeaders() {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.setAccept([MediaType.APPLICATION_JSON])
        headers
    }

    protected <T> T postOnBehalfOf(String impersonatedUserName, URI uri, Map<String, Object> body, Class<T> type) {
        log.info "User impersonation! User ${authContext.user.username} " +
                "sending request on behalf of user $impersonatedUserName, requestURL: $uri"
        post(uri, body, type, getRestTemplateWithAuthorizationOnBehalfOf(impersonatedUserName))
    }

    protected <T> T postAsCurrentUser(URI uri, Map<String, Object> body, Class<T> type) {
        log.info "Sending authorised post request from ${authContext.user.username} user."
        post(uri, body, type, getRestTemplateWithAuthorizationToken(authContext.token))
    }

    protected static <T> T post(URI uri, Map<String, Object> body, Class<T> type, RestTemplate restTemplate) throws InvalidRequestException {

        def httpEntity = new HttpEntity(body, jsonHeaders)
        ResponseEntity<T> response = restTemplate.exchange(uri,
                HttpMethod.POST, httpEntity, type)

        if (response.statusCode != HttpStatus.OK) {
            throw new InvalidRequestException(response.statusCode.toString())
        }

        return response.body
    }

    protected <T> T getAsCurrentUser(URI uri, Class<T> type) {
        log.info "Sending authorised get request from ${authContext.user.username} user."
        get(uri, type, getRestTemplateWithAuthorizationToken(authContext.token))
    }

    protected static <T> T get(URI uri, Class<T> type, RestTemplate restTemplate) throws InvalidRequestException {

        def httpEntity = new HttpEntity(jsonHeaders)
        ResponseEntity<T> response = restTemplate.exchange(uri,
                HttpMethod.GET, httpEntity, type)

        if (response.statusCode != HttpStatus.OK) {
            throw new InvalidRequestException(response.statusCode.toString())
        }

        return response.body
    }

    protected RestTemplate getRestTemplateWithAuthorizationToken(String userToken) {
        def restTemplate = getRestTemplate()
        restTemplate.interceptors.add(new BearerTokenInterceptor(userToken))
        return restTemplate
    }

    protected RestTemplate getRestTemplateWithAuthorizationOnBehalfOf(String impersonatedUserName) {
        def restTemplate = getRestTemplate()
        restTemplate.interceptors.add(new ImpersonationInterceptor(impersonatedUserName))
        return restTemplate
    }

    protected RestTemplate getRestTemplate() {
        def requestFactory = new HttpComponentsClientHttpRequestFactory()
        def restTemplate = new RestTemplate(requestFactory)
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler())
        return restTemplate
    }
}
