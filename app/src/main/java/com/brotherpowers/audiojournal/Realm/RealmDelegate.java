package com.brotherpowers.audiojournal.Realm;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by harsh_v on 11/29/16.
 */

interface RealmDelegate {
    long nexID(Realm realm);

    RealmObject generateId(Realm realm);
}
