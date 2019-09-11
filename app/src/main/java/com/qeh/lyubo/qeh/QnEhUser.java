package com.qeh.lyubo.qeh;

import java.util.ArrayList;
import java.util.HashMap;

public class QnEhUser {
    private int user_type;
    private String name;
    private String endpoint;
    private boolean is_connected;
    HashMap<String, QnEhUser> advertisers;

    public QnEhUser(){ }

    public QnEhUser(String name, int user_type, String endpoint) {
        this.name = name;
        this.user_type = user_type;
        this.endpoint = endpoint;
        this.advertisers  = new HashMap<String, QnEhUser>();
        this.is_connected = false;
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

    public void setAdvertisers(HashMap<String, QnEhUser> advertisers){
        this.advertisers = advertisers;
    }
    public HashMap<String, QnEhUser> getAdvertisers(){
        return this.advertisers;
    }
}
