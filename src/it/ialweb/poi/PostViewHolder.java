package it.ialweb.poi;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import it.ialweb.poi.R;

/**
 * Created by TSAIM044 on 08/07/2015.
 */
class PostViewHolder extends RecyclerView.ViewHolder {

    TextView postText;
    TextView userName;
    ImageView favourite;

    public PostViewHolder(View view) {
        super(view);
        postText = (TextView) view.findViewById(R.id.textView_posText);
        userName = (TextView) view.findViewById(R.id.textView_username);
        favourite = (ImageView) view.findViewById(R.id.imageView_toggleFavourite);
    }
}
