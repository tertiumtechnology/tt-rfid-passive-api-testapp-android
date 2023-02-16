package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;

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
            throw new ClassCastException(context
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

        encrypted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            encryptedFlag.setEnabled(isChecked);

            if (!isChecked) {
                encryptedFlag.setText("");
            }
        });

        command.addTextChangedListener(new HexDataTextWatcher(command));
        DialogUtils.appendAllDialogInputFilters(command, 64);

        encryptedFlag.addTextChangedListener(new HexDataTextWatcher(encryptedFlag));
        DialogUtils.appendAllDialogInputFilters(encryptedFlag, 2);

        builder.setView(dialogView)
                .setTitle(getString(R.string.tunnel_dialog_title))
                .setPositiveButton(R.string.tunnel_dialog_tunnel_button, (dialog, id) -> listener.onStartTunnel(command.getText().toString(), encrypted.isChecked(),
                        encryptedFlag.getText().toString()))
                .setNegativeButton(R.string.dialog_cancel_button, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}