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

public class WriteAccessPasswordDialogFragment extends AppCompatDialogFragment {

    public interface WriteAccessPasswordListener {
        void onWriteAccessPassword(String hexOldPassword, String hexNewPassword);
    }

    private static final String TAG_TITLE = "TAG_TITLE";

    public static WriteAccessPasswordDialogFragment newInstance(String tag) {
        WriteAccessPasswordDialogFragment dialog = new WriteAccessPasswordDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
        dialog.setArguments(args);

        return dialog;
    }

    private WriteAccessPasswordListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (WriteAccessPasswordListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement WriteAccessPasswordListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.write_access_password_dialog, null);

        final EditText oldPasswordText = dialogView.findViewById(R.id.old_access_password);
        oldPasswordText.addTextChangedListener(new HexDataTextWatcher(oldPasswordText));
        DialogUtils.appendAllDialogInputFilters(oldPasswordText, 8);

        final EditText newPasswordText = dialogView.findViewById(R.id.new_access_password);
        newPasswordText.addTextChangedListener(new HexDataTextWatcher(newPasswordText));
        DialogUtils.appendAllDialogInputFilters(newPasswordText, 8);

        builder.setView(dialogView)
                .setTitle(getString(R.string.write_access_password_dialog_title, getArguments().getString(TAG_TITLE)))
                .setPositiveButton(R.string.write_access_password_write_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onWriteAccessPassword(oldPasswordText.getText().toString(), newPasswordText.getText
                                ().toString());
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