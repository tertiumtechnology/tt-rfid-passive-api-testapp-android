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

public class ReadTidDialogFragment extends AppCompatDialogFragment {

    public interface ReadTidListener {
        void onReadTid(String hexPassword);
    }

    private static final String TAG_TITLE = "TAG_TITLE";

    public static ReadTidDialogFragment newInstance(String tag) {
        ReadTidDialogFragment dialog = new ReadTidDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
        dialog.setArguments(args);

        return dialog;
    }

    private ReadTidListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ReadTidListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ReadTidListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.read_tid_dialog, null);

        final EditText passwordText = dialogView.findViewById(R.id.tid_read_password);
        passwordText.addTextChangedListener(new HexDataTextWatcher(passwordText));
        DialogUtils.appendAllDialogInputFilters(passwordText, 8);

        builder.setView(dialogView)
                .setTitle(getString(R.string.read_tid_dialog_title, getArguments().getString(TAG_TITLE)))
                .setPositiveButton(R.string.read_tid_dialog_read_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onReadTid(passwordText.getText().toString());
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