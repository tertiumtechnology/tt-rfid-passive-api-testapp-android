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

public class WriteUserMemoryDialogFragment extends AppCompatDialogFragment {

    public interface WriteUserMemoryListener {
        void onWriteUserMemory(int block, String data);
    }

    public static WriteUserMemoryDialogFragment newInstance() {
        return new WriteUserMemoryDialogFragment();
    }

    private WriteUserMemoryListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (WriteUserMemoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement WriteUserMemoryListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.write_user_memory_dialog, null);

        final EditText dataText = dialogView.findViewById(R.id.write_user_memory_data);
        final EditText blockText = dialogView.findViewById(R.id.write_user_memory_block);

        dataText.addTextChangedListener(new HexDataTextWatcher(dataText));

        builder.setView(dialogView)
                .setTitle(getString(R.string.write_user_memory_dialog_title))
                .setPositiveButton(R.string.write_user_memory_dialog_button, (dialog, id) -> {
                    int block = 0;

                    try {
                        block = Integer.parseInt(blockText.getText().toString());
                    } catch (NumberFormatException e) {
                    }

                    listener.onWriteUserMemory(block, dataText.getText().toString());

                })
                .setNegativeButton(R.string.dialog_cancel_button, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}