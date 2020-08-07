package org.transmartproject.db.contact

class Contact implements  Serializable {
    String user_id
    Integer count
    Long query_id
    String synopsis

    static constraints = {
        user_id nullable: false
        query_id nullable: false
    }

    static mapping = {
        id composite: ['user_id', 'query_id']
        table name: 'contact', schema: 'i2b2demodata'
    }
}
