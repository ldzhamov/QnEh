package com.qeh.lyubo.qeh;

import android.app.Application;

public class QnEhApplication extends Application {
    private QnEhUser user;

    public QnEhUser getUser() {
        return user;
    }

    public void setUser(QnEhUser user) {
        this.user = user;
    }
}
