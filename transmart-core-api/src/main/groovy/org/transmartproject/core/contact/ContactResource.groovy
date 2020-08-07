package org.transmartproject.core.contact

import org.transmartproject.core.multidimquery.query.Constraint
import org.transmartproject.core.users.User

interface ContactResource {

    /**
     *
     * @param constraint - The constraint to filter contacts with
     * @param queryId - The queryId to filter by
     * @param synopsis - The synopsis to send to the cohort
     * @return the number of DIDs successfully contacted
     */
    ContactResponse contactForQuery(Long queryId, String synopsis, User user)

    ContactResponse getContactRecord(queryId, User user)
}