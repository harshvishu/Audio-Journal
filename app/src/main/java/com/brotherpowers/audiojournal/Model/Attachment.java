package com.brotherpowers.audiojournal.Model;

import android.content.Context;
import android.text.TextUtils;

import com.brotherpowers.audiojournal.Utils.FileUtils;

import java.io.File;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by harsh_v on 11/4/16.
 */

public class Attachment extends RealmObject implements RealmDelegate {

    @PrimaryKey
    private long id;

    private String file_name;

    private String file_url;

    // Enum to support FileType
    private int fileType;

    private Date created_at = new Date();

    public String getFileName() {
        return file_name;
    }

    public Attachment setFileName(String file_name) {
        this.file_name = file_name;
        return this;
    }

    public String getFile_url() {
        return file_url;
    }

    public Attachment setFileUrl(String file_url) {
        this.file_url = file_url;
        return this;
    }

    public FileUtils.Type fileType() {
        return FileUtils.Type.valueOf(fileType);
    }

    public Attachment setFileType(FileUtils.Type fileType) {
        this.fileType = fileType.value;
        return this;
    }

    public long getId() {
        return id;
    }

    public Attachment setId(long id) {
        this.id = id;
        return this;
    }


    public File file(Context context) {
        if (TextUtils.isEmpty(file_name)) {
            return null;
        }
        return FileUtils.sharedInstance.getFile(fileType(), file_name, context);
    }

    public Date getCreatedAt() {
        return created_at;
    }

    @Override
    public long nexID(Realm realm) {
        long newID = 0;
        try {
            Number maxID = realm.where(getClass()).max("id");
            newID = maxID.longValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        newID += 1L;
        return newID;
    }

    @Override
    public Attachment generateId(Realm realm) {
        setId(nexID(realm));
        return this;
    }

}
