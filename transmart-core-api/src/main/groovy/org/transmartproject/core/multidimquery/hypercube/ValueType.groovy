/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package org.transmartproject.core.multidimquery.hypercube

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import groovy.transform.CompileStatic

@CompileStatic
enum ValueType {

    STRING('String'),
    INT('Int'),
    DOUBLE('Double'),
    TIMESTAMP('Timestamp'),
    MAP('Object'),
    NONE(null)

    private String jsonType

    ValueType(String jsonType) {
        this.jsonType = jsonType
    }

    @JsonValue
    String toJson() {
        jsonType
    }

    String toString() {
        name().toLowerCase()
    }

    private static final Map<String, ValueType> mapping = new HashMap<>()
    static {
        for (ValueType type : values()) {
            mapping.put(type.name().toLowerCase(), type)
        }
    }

    @JsonCreator
    static ValueType forName(String name) {
        name = name.toLowerCase()
        if (mapping.containsKey(name)) {
            return mapping[name]
        } else {
            return NONE
        }
    }

    static ValueType forClass(Class cls) {
        switch (cls) {
            case String:
                return STRING
            case Integer:
            case Long:
            case Short:
                return INT
            case Double:
            case Float:
            case Number:
                return DOUBLE
            case Date:
                return TIMESTAMP
            case Map:
                return MAP
            default:
                throw new RuntimeException("Unsupported type: ${cls}. This type is not serializable")
        }
    }

}
