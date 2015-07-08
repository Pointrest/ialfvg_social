package it.ialweb.poi;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class NewPostDialog extends DialogFragment {

    public static final String POSTTEXT = "POSTTEXT";
    private EditText postText;
    private Callback callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback)
            callback = (Callback) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vView = inflater.inflate(R.layout.fragment_new_post_dialog, null);

        postText = (EditText) vView.findViewById(R.id.editText_post);

        if (savedInstanceState != null)
            postText.setText(savedInstanceState.getString(POSTTEXT));

        builder.setView(vView)
                // Add action buttons
                .setPositiveButton(R.string.post, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        callback.onPost(postText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewPostDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(POSTTEXT, postText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public interface Callback {
        void onPost(String txt);
    }
}
