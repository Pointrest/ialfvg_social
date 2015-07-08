package it.ialweb.poi.it.ialweb.poi.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.ialweb.poi.R;
import it.ialweb.poi.it.ialweb.poi.models.Post;
import it.ialweb.poi.it.ialweb.poi.models.User;

/**
 * Created by TSAIM044 on 08/07/2015.
 */
public class UsersListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new UserAdapter(getActivity(), (UserHandler)getActivity()));

        return recyclerView;
    }

    private class UserAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<User> users;
        private UserHandler userHandler;

        public UserAdapter(Context context, UserHandler handler) {
            mContext = context;
            userHandler = handler;
            users = userHandler.getUserDataSet(this);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserViewHolder viewHolder = (UserViewHolder)holder;
            final User user = users.get(position);
            if (user != null) {
                viewHolder.userName.setText(user.getName());
                viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userHandler.followUser(user.getId());
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.element_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
    private class UserViewHolder extends RecyclerView.ViewHolder {

        Button followButton;
        TextView userName;
        ImageView userPicture;

        public UserViewHolder(View view) {
            super(view);
            userName = (TextView) view.findViewById(R.id.textView_username);
            followButton = (Button)view.findViewById(R.id.button_follow);
            userPicture = (ImageView) view.findViewById(R.id.imageView_userPicture);
        }
    }
    public interface UserHandler {
        void followUser(String userId);
        List<User> getUserDataSet(RecyclerView.Adapter adapter);
    }
}
