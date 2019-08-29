package com.qeh.lyubo.qeh;

public class QnEhUser {
    private int user_type;
    private String name;
    private String endpoint;

    public QnEhUser(){ }

    public QnEhUser(String name, int user_type, String endpoint) {
        this.name = name;
        this.user_type = user_type;
        this.endpoint = endpoint;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getUserType(){
        return this.user_type;
    }

    public void setUserType(int user_type){
        this.user_type = user_type;
    }

    public String getEndpoint(){
        return this.endpoint;
    }

    public void setEndpoint(String endpoint){
        this.endpoint = endpoint;
    }
}
