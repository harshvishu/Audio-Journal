package com.brotherpowers.audiojournal.Model;

import io.realm.RealmObject;

/**
 * Created by harsh_v on 2/22/17.
 */

public class Reminder extends RealmObject {
    private Long remind_at = null;
    private boolean enable = false;

    public Long getRemindAt() {
        return remind_at;
    }

    Reminder setRemindAt(Long remind_at) {
        this.remind_at = remind_at;

        // If remind_at in NULL then disable alarm otherwise new reminder is set the it is enabled by default
        enable = remind_at != null;
        return this;
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean enable(boolean enable) {
        this.enable = enable && remind_at != null;
        return this.enable;
    }

}
