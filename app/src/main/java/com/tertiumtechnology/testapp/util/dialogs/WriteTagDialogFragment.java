package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.tertiumtechnology.testapp.R;
import com.tertiumtechnology.testapp.util.dialogs.DialogUtils.HexDataTextWatcher;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatTextView;

public class WriteTagDialogFragment extends AppCompatDialogFragment {

    public interface WriteTagListener {
        void onWriteTag(int address, String hexData, String hexPassword);
    }

    private static final String TAG_TITLE = "TAG_TITLE";
    private static final String REQUIRE_PASSWORD = "REQUIRE_PASSWORD";

    public static WriteTagDialogFragment newInstance(String tag, boolean requirePassword) {
        WriteTagDialogFragment dialog = new WriteTagDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
        args.putBoolean(REQUIRE_PASSWORD, requirePassword);
        dialog.setArguments(args);

        return dialog;
    }

    private WriteTagListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (WriteTagListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement WriteTagListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.write_tag_dialog, null);
        final EditText addressText = dialogView.findViewById(R.id.tag_write_address);
        final EditText dataText = dialogView.findViewById(R.id.tag_write_data);

        dataText.addTextChangedListener(new HexDataTextWatcher(dataText));
        DialogUtils.appendAllDialogInputFilters(dataText, 16);

        AppCompatTextView passwordTextView = dialogView.findViewById(R.id.tag_write_password_text_view_title);
        final EditText passwordText = dialogView.findViewById(R.id.tag_write_password);

        if (getArguments().getBoolean(REQUIRE_PASSWORD)) {
            passwordTextView.setVisibility(View.VISIBLE);
            passwordText.setVisibility(View.VISIBLE);
            passwordText.addTextChangedListener(new HexDataTextWatcher(passwordText));
            DialogUtils.appendAllDialogInputFilters(passwordText, 8);
        }

        builder.setView(dialogView)
                .setTitle(getString(R.string.write_dialog_title, getArguments().getString(TAG_TITLE)))
                .setPositiveButton(R.string.write_dialog_write_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int address = 0;

                        try {
                            address = Integer.parseInt(addressText.getText().toString());
                        } catch (NumberFormatException e) {
                        }

                        listener.onWriteTag(address, dataText.getText().toString(), passwordText.getText().toString());
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