package org.transmartproject.rest.marshallers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import groovy.transform.Canonical
import groovy.transform.CompileStatic

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@Canonical
@CompileStatic
class QueryRepresentation {

    Long id

    @Size(min = 1)
    String subjectDimension

    @Size(min = 1)
    String username

    @Size(min = 1)
    String name

    @NotNull
    Object queryConstraint

    Boolean bookmarked

    Boolean subscribed

    SubscriptionFrequency subscriptionFreq

    Object queryBlob

    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Date createDate

    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Date updateDate

}

