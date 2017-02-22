package com.brotherpowers.audiojournal.Utils;

import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by harsh_v on 2/22/17.
 */

public final class DBHelper {

    /**
     * @return Attachment with fileType image for particular dataentry
     */
    public static RealmQuery<Attachment> images(DataEntry entry) {
        return entry.getAttachments()
                .where()
                .equalTo("fileType", FileUtils.Type.IMAGE.value);
    }

    public static RealmQuery<DataEntry> findEntryForId(long id, Realm realm){
        return RealmQuery.createQuery(realm,DataEntry.class)
                .equalTo("id", id);
    }

    public static RealmQuery<Attachment> filterFilesForType(FileUtils.Type type, RealmQuery<Attachment> query){
        return query.equalTo("fileType", FileUtils.Type.IMAGE.value);
    }
}
