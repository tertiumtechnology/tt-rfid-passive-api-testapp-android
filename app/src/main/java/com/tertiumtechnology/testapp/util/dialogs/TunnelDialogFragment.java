package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.tertiumtechnology.testapp.R;
import com.tertiumtechnology.testapp.util.dialogs.DialogUtils.HexDataTextWatcher;

public class TunnelDialogFragment extends AppCompatDialogFragment {

    public interface TunnelListener {
        void onStartTunnel(String hexCommand, boolean encrypted, String hexEncryptedFlag);
    }

    public static TunnelDialogFragment newInstance() {
        TunnelDialogFragment dialog = new TunnelDialogFragment();

        Bundle args = new Bundle();
        dialog.setArguments(args);

        return dialog;
    }

    private TunnelListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (TunnelListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement TunnelListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.tunnel_dialog, null);
        final AppCompatEditText command = dialogView.findViewById(R.id.tunnel_command);
        final AppCompatCheckBox encrypted = dialogView.findViewById(R.id.tunnel_encrypted);
        final AppCompatEditText encryptedFlag = dialogView.findViewById(R.id.tunnel_encrypted_flag);

        encrypted.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                encryptedFlag.setEnabled(isChecked);

                if (!isChecked) {
                    encryptedFlag.setText("");
                }
            }
        });

        command.addTextChangedListener(new HexDataTextWatcher(encryptedFlag));
        DialogUtils.appendAllDialogInputFilters(command, 64);

        encryptedFlag.addTextChangedListener(new HexDataTextWatcher(encryptedFlag));
        DialogUtils.appendAllDialogInputFilters(encryptedFlag, 2);

        builder.setView(dialogView)
                .setTitle(getString(R.string.tunnel_dialog_title))
                .setPositiveButton(R.string.tunnel_dialog_tunnel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        listener.onStartTunnel(command.getText().toString(), encrypted.isChecked(),
                                encryptedFlag.getText().toString());
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