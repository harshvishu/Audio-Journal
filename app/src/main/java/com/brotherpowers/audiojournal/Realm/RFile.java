package com.brotherpowers.audiojournal.Realm;

import android.content.Context;
import android.text.TextUtils;

import com.brotherpowers.audiojournal.Utils.FileUtils;

import java.io.File;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by harsh_v on 11/4/16.
 */

public class RFile extends RealmObject {
    @PrimaryKey
    private long id;

    private String file_name;

    private String file_url;

    // Enum to support FileType
    private int fileType;


    public String getFileName() {
        return file_name;
    }

    public void setFileName(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public FileUtils.Type fileType() {
        return FileUtils.Type.valueOf(fileType);
    }

    public RFile setFileType(FileUtils.Type fileType) {
        this.fileType = fileType.value;
        return this;
    }

    public long getId() {
        return id;
    }

    public RFile setId(long id) {
        this.id = id;
        return this;
    }

    public File file(Context context) {
        if (TextUtils.isEmpty(file_name)) {
            return null;
        }
        return FileUtils.sharedInstance.getFile(fileType(), file_name, context);
    }

}
