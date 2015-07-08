package it.ialweb.poi.it.ialweb.poi.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.ialweb.poi.R;
import it.ialweb.poi.it.ialweb.poi.models.User;

/**
 * Created by TSAIM044 on 08/07/2015.
 */
public class UserProfileFragment extends Fragment {

    public static final String USERID = "USERID";
    private String userId;

    public static UserProfileFragment getInstance(String userId) {
        UserProfileFragment upf = new UserProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString(USERID, userId);
        upf.setArguments(bundle);
        return  upf;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, null);

        Bundle bundle = getArguments();
        String userId = "";
        if (bundle == null)
            bundle = savedInstanceState;

        userId = bundle.getString(USERID);
        ((TextView)view.findViewById(R.id.textView_username)).setText(userId);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(USERID, userId);
        super.onSaveInstanceState(outState);
    }
}
