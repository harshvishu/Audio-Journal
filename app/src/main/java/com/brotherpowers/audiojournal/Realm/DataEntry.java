package com.brotherpowers.audiojournal.Realm;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by harsh_v on 11/4/16.
 */

public class DataEntry extends RealmObject implements Essentials {

    @PrimaryKey
    private long id;

    private RFile audioFile;

    private String title;

    private long length;

    private Long remind_at;

    private RealmList<RFile> attachments;

    // No Setter
    private Date created_at;

    public DataEntry() {
        super();
        created_at = new Date();
    }


    public long getId() {
        return id;
    }

    public DataEntry setId(long id) {
        this.id = id;
        return this;
    }

    public RFile audioFile() {
        return audioFile;
    }

    public DataEntry setAudioFile(RFile audioFile) {
        this.audioFile = audioFile;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DataEntry setTitle(String title) {
        this.title = title;
        return this;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public RealmList<RFile> getAttachments() {
        return attachments;
    }

    public DataEntry setAttachments(RealmList<RFile> attachments) {
        this.attachments = attachments;
        return this;
    }


    public long getLength() {
        return length;
    }

    public DataEntry setLength(long length) {
        this.length = length;
        return this;
    }

    public void setRemindAt(Long remind_at) {
        this.remind_at = remind_at;
    }

    public Long getRemindAt() {
        return remind_at;
    }

    @Override
    public long nexID(Realm realm) {
        long newID = 0;
        try {
            Number maxID = realm.where(getClass()).max("id");
            newID = maxID == null ? 0 : maxID.longValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        newID += 1L;
        return newID;
    }

    @Override
    public DataEntry generateId(Realm realm) {
        setId(nexID(realm));
        return this;
    }
}
