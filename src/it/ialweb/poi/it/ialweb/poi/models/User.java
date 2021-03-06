package it.ialweb.poi.it.ialweb.poi.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TSAIM044 on 08/07/2015.
 */
public class User {

    String id;

    String name;

    List<User> followers;

    List<User> following;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public User(String id) {
        this.id = id;
    }
    public List<User> getFollowers() {
        if (followers == null)
            followers = new ArrayList<User>();
        return followers;
    }

    public List<User> getFollowing() {
        if (following == null)
            following = new ArrayList<User>();
        return following;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
