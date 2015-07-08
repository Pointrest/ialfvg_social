package it.ialweb.poi.it.ialweb.poi.models;

/**
 * Created by TSAIM044 on 07/07/2015.
 */
public class Post {
    private int ID;
    private String Text;

    public Post(int id, String text) {
        this.ID = id;
        this.Text = text;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + ID + '\'' +
                ", Text='" + Text + '\'' +
                '}';
    }
}
