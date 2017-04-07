package com.brotherpowers.audiojournal.Utils;

import java.nio.charset.Charset;

/**
 * Created by harsh_v on 10/28/16.
 */

public final class Constants {
    public static final String MD5 = "MD5";
    public static final Charset UTF_8 = Charset.forName("utf-8");

    public static final int REQ_REC_PERMISSION = 0x1;
    public static final int REQUEST_CAMERA_PERMISSION = 0x1;
    public static final String FRAGMENT_DIALOG = "dialog";

    public static final String TEXT_NOTE_DEFAULT_JSON = "{\"title\":\"\",\"note\":\"\"}";

    public static final class KEYS {
        public static final String entry_id = "entry_id";
        public static final String task_id = "task_id";
        public static final String id = "id";
        public static final String data = "data";
        public static final String attachment_id = "attachment_id";
    }
}
