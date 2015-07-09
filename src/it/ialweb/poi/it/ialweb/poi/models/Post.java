package it.ialweb.poi.it.ialweb.poi.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by TSAIM044 on 07/07/2015.
 */
public class Post {

    private String id;

    private String userId;

    private String text;

    public Post(String userId, String text) {
        this.userId = userId;
        this.text = text;
    }

    public Post(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Post{" +
                "Userid='" + userId + '\'' +
                ", Text='" + text + '\'' +
                '}';
    }

    public String getID() {
        return userId;
    }

    public void setID(String userid) {
        this.userId = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
