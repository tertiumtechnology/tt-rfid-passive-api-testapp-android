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

public class SetNameDialogFragment extends AppCompatDialogFragment {

    public interface SetNameListener {
        void onSetName(String name);
    }

    public static SetNameDialogFragment newInstance() {
        return new SetNameDialogFragment();
    }

    private SetNameListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (SetNameListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement SetNameListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.set_name_dialog, null);

        final EditText nameValueText = dialogView.findViewById(R.id.set_name_value);

        builder.setView(dialogView)
                .setTitle(getString(R.string.set_name_dialog_title))
                .setPositiveButton(R.string.set_name_dialog_button, (dialog, id) -> listener.onSetName(nameValueText.getText().toString()))
                .setNegativeButton(R.string.dialog_cancel_button, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}