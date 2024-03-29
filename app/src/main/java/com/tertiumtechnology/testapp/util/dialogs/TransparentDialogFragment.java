package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.tertiumtechnology.testapp.R;
import com.tertiumtechnology.testapp.util.dialogs.DialogUtils.HexDataTextWatcher;

public class TransparentDialogFragment extends AppCompatDialogFragment {

    public interface TransparentCommandListener {
        void onTransparentCommand(String hexCommand);
    }

    public static TransparentDialogFragment newInstance() {
        return new TransparentDialogFragment();
    }

    private TransparentCommandListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (TransparentCommandListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement TransparentCommandListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.transparent_dialog, null);

        final EditText transparentCommandText = dialogView.findViewById(R.id.transparent_command);
        transparentCommandText.addTextChangedListener(new HexDataTextWatcher(transparentCommandText));

        builder.setView(dialogView)
                .setTitle(getString(R.string.transparent_dialog_title))
                .setPositiveButton(R.string.transparent_dialog_button, (dialog, id) -> listener.onTransparentCommand(transparentCommandText.getText().toString()))
                .setNegativeButton(R.string.dialog_cancel_button, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}