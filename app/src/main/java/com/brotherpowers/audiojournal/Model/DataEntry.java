package com.brotherpowers.audiojournal.Model;

import com.brotherpowers.audiojournal.Utils.Constants;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by harsh_v on 11/4/16.
 */

public class DataEntry extends RealmObject implements RealmDelegate {


    public String text_note = Constants.TEXT_NOTE_DEFAULT_JSON;    // Default JSON for text_note

    @PrimaryKey
    private long id;
    private Attachment audioFile;
    private String title;
    private long length;
    private RealmList<Attachment> attachments;
    private Date created_at = new Date();
    private Reminder reminder = new Reminder();

    public long getId() {
        return id;
    }

    public DataEntry setId(long id) {
        this.id = id;
        return this;
    }

    public Attachment audioFile() {
        return audioFile;
    }

    public DataEntry setAudioFile(Attachment audioFile) {
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

    public RealmList<Attachment> getAttachments() {
        return attachments;
    }

    public DataEntry setAttachments(RealmList<Attachment> attachments) {
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

    public Reminder getRemindAt() {
        return reminder;
    }

    /**
     * set reminder time & Activate
     */
    public void setRemindAt(Long remind_at) {
        this.reminder.setRemindAt(remind_at)
                .set(true);
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
        newID += 1;
        return newID;
    }

    @Override
    public DataEntry generateId(Realm realm) {
        setId(nexID(realm));
        return this;
    }
}
