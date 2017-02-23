package com.brotherpowers.audiojournal.Model;

import io.realm.RealmObject;

/**
 * Created by harsh_v on 2/22/17.
 */

public class Reminder extends RealmObject {
    private Long remind_at = null;
    private boolean is_set = false;

    public Long getRemindAt() {
        return remind_at;
    }

    public Reminder setRemindAt(Long remind_at) {
        this.remind_at = remind_at;
        return this;
    }

    public boolean is_set() {
        return is_set;
    }

    public Reminder set(boolean is_set) {
        this.is_set = is_set;
        return this;
    }
}
