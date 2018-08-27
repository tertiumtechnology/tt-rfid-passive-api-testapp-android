package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.tertiumtechnology.testapp.R;
import com.tertiumtechnology.testapp.util.dialogs.DialogUtils.HexDataTextWatcher;

public class KillTagDialogFragment extends AppCompatDialogFragment {

    public interface KillTagListener {
        void onKillTag(String hexPassword);
    }

    private static final String TAG_TITLE = "TAG_TITLE";

    public static KillTagDialogFragment newInstance(String tag) {
        KillTagDialogFragment dialog = new KillTagDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
        dialog.setArguments(args);

        return dialog;
    }

    private KillTagListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (KillTagListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement KillTagListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.kill_tag_dialog, null);

        final EditText passwordText = dialogView.findViewById(R.id.kill_tag_password);
        passwordText.addTextChangedListener(new HexDataTextWatcher(passwordText));
        DialogUtils.appendAllDialogInputFilters(passwordText, 8);

        builder.setView(dialogView)
                .setTitle(getString(R.string.kill_tag_dialog_title, getArguments().getString(TAG_TITLE)))
                .setPositiveButton(R.string.kill_tag_dialog_kill_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onKillTag(passwordText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}