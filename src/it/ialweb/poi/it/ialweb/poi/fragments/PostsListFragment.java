package it.ialweb.poi.it.ialweb.poi.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.ialweb.poi.R;
import it.ialweb.poi.it.ialweb.poi.models.Post;
import it.ialweb.poi.it.ialweb.poi.models.User;

/**
 * Created by TSAIM044 on 08/07/2015.
 */
public class PostsListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new PostsAdapter((PostsHandler) getActivity(), getActivity()));

        return recyclerView;
    }

    private class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private PostsHandler postsHandler;
        private List<Post> posts;
        private Context mContext;

        public PostsAdapter(PostsHandler postsHandler, Context context) {
            this.postsHandler = postsHandler;
            mContext = context;
            posts = postsHandler.getPostsDataSet(this);
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            PostViewHolder viewHolder = (PostViewHolder)holder;
            final Post post = posts.get(position);
            if (post != null) {
                viewHolder.userName.setText(post.getID());
                viewHolder.postText.setText(post.getText());
                boolean favourite = false;
                /*
                if (getUser().getFavourites().contains(post)) {
                    viewHolder.favourite.setImageDrawable(getResources().getDrawable(R.drawable.abc_btn_rating_star_on_mtrl_alpha));
                    favourite = true;
                } */
                final boolean finalFavourite = favourite;
                //post.getFavourites()
                viewHolder.favourite.setOnClickListener(new View.OnClickListener() {
                    private boolean isFavourite = finalFavourite;
                    @Override
                    public void onClick(View v) {
                        if (!isFavourite) {
                            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.abc_btn_rating_star_on_mtrl_alpha));
                            isFavourite = true;
                            postsHandler.addToFavourites(post.getID());
                        } else {
                            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.abc_btn_rating_star_off_mtrl_alpha));
                            isFavourite = false;
                            postsHandler.removeFromFavourites(post.getID());
                        }
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.element_post, parent, false);
            return new PostViewHolder(view);
        }
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {

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

    public interface PostsHandler {
        void addToFavourites(String postId);
        void removeFromFavourites(String postId);
        List<Post> getPostsDataSet(RecyclerView.Adapter adapter);
        User getUser();
    }
}