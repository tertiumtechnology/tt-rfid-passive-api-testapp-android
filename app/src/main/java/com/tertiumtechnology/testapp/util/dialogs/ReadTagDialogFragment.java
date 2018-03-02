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

public class ReadTagDialogFragment extends AppCompatDialogFragment {

    public interface ReadTagListener {
        void onReadTag(int address, int block);
    }

    private static final String TAG_TITLE = "TAG_TITLE";

    public static ReadTagDialogFragment newInstance(String tag) {
        ReadTagDialogFragment dialog = new ReadTagDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
        dialog.setArguments(args);

        return dialog;
    }

    private ReadTagListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ReadTagListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ReadTagListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.read_tag_dialog, null);
        final EditText addressText = dialogView.findViewById(R.id.tag_read_address);
        final EditText blockText = dialogView.findViewById(R.id.tag_read_block);

        builder.setView(dialogView)
                .setTitle(getString(R.string.read_dialog_title, getArguments().getString(TAG_TITLE)))
                .setPositiveButton(R.string.read_dialog_read_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int address = 0;
                        int block = 0;

                        try {
                            address = Integer.parseInt(addressText.getText().toString());
                        } catch (NumberFormatException e) {
                        }

                        try {
                            block = Integer.parseInt(blockText.getText().toString());
                        } catch (NumberFormatException e) {
                        }

                        listener.onReadTag(address, block);
                    }
                })
                .setNegativeButton(R.string.rw_dialog_cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}