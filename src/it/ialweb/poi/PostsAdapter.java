package it.ialweb.poi;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.ialweb.poi.MainActivity;
import it.ialweb.poi.PostViewHolder;

/**
 * Created by TSAIM044 on 07/07/2015.
 */
public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    public PostsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RecyclerView.ViewHolder fHolder = holder;

        ((TextView) fHolder.itemView).setText(((MainActivity)mContext).getItem(position));


/*        ((MainActivity)mContext).getItem(position + "", new TableOperationCallback<Post>() {
            @Override
            public void onCompleted(Post entity, Exception exception, ServiceFilterResponse response) {
                if (entity != null)
                    ((TextView) fHolder.itemView).setText(entity.getText());
            }
        }); */
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PostViewHolder(view);
    }

}
