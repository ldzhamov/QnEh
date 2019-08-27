package com.qeh.lyubo.qeh;

public class QnEhUser {
    private int user_type;
    private String name;

    public QnEhUser(){ }

    public QnEhUser(String name, int user_type) {
        this.name = name;
        this.user_type = user_type;
    }

    private String getName(){
        return this.name;
    }

    private void setName(String name){
        this.name = name;
    }

    private int getUserType(){
        return this.user_type;
    }

    private void setUserType(int user_type){
        this.user_type = user_type;
    }
}
