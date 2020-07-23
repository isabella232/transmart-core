package org.transmartproject.db.contact

class Contact implements  Serializable {
    String hash
    String user_id
    Integer count

    static hasMany = [contacted: String]

    static constraints = {
        hash nullable: false
        user_id nullable: false
    }

    static mapping = {
        id composite: ['hash', 'user_id']
        table name: 'contact', schema: 'i2b2demodata'
    }
}
