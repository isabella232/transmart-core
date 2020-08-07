package org.transmartproject.rest.marshallers

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic

@CompileStatic
enum SubscriptionFrequency {

    DAILY,
    WEEKLY

    private static final Map<String, SubscriptionFrequency> mapping = new HashMap<>()
    static {
        for (SubscriptionFrequency type: values()) {
            mapping.put(type.name().toLowerCase(), type)
        }
    }

    @JsonCreator
    static SubscriptionFrequency forName(String name) {
        name = name.toLowerCase()
        if (mapping.containsKey(name)) {
            return mapping[name]
        } else {
            return null
        }
    }

}
