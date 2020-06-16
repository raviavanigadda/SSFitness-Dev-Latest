package com.app.ssfitness_dev.data.models;

import java.io.Serializable;

public class GroupModel implements Serializable {

    String group_id, group_name, created_by, created_on, photoUrl;

    public GroupModel(){

    }

    public GroupModel(String group_id, String group_name, String created_by, String created_on, String photoUrl) {
        this.group_id = group_id;
        this.group_name = group_name;
        this.created_by = created_by;
        this.created_on = created_on;
        this.photoUrl = photoUrl;
    }

    public String getGroup_id() {
        return group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public String getCreated_by() {
        return created_by;
    }

    public String getCreated_on() {
        return created_on;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
