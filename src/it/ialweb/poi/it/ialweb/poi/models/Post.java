package it.ialweb.poi.it.ialweb.poi.models;

/**
 * Created by TSAIM044 on 07/07/2015.
 */
public class Post {
    private String id;
    private String Text;

    public Post(String id, String text) {
        this.id = id;
        Text = text;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", Text='" + Text + '\'' +
                '}';
    }
}
