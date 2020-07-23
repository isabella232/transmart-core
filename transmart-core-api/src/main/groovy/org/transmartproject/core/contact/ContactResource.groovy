package org.transmartproject.core.contact

import org.transmartproject.core.multidimquery.query.Constraint
import org.transmartproject.core.users.User

interface ContactResource {

    /**
     *
     * @param constraint - The constraint to filter contacts with
     * @return the number of DIDs successfully contacted
     */
    ContactResponse contactForConstraint(Constraint constraint, User user);
}