package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.tertiumtechnology.api.rfidpassiveapilib.EPC_tag;
import com.tertiumtechnology.testapp.R;
import com.tertiumtechnology.testapp.util.dialogs.DialogUtils.HexDataTextWatcher;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;

public class LockTagDialogFragment extends AppCompatDialogFragment {

    public interface LockTagListener {
        void onLockTag(int lockType, String hexPassword);
    }

    private static class LockType {
        private int type;
        private String name;

        LockType(int type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        String getName() {
            return name;
        }

        int getType() {
            return type;
        }
    }

    private static final String TAG_TITLE = "TAG_TITLE";

    private static ArrayList<LockType> lockTypes = new ArrayList<>();

    static {
        lockTypes.add(new LockType(EPC_tag.MEMORY_PASSWORD_WRITABLE, "Memory write protected"));
        lockTypes.add(new LockType(EPC_tag.MEMORY_NOTWRITABLE, "Memory write forbidden"));
        lockTypes.add(new LockType(EPC_tag.ID_NOTWRITABLE, "ID rewrite forbidden"));
        lockTypes.add(new LockType(EPC_tag.ACCESSPASSWORD_PASSWORD_READABLE_WRITABLE, "Access-password protected"));
        lockTypes.add(new LockType(EPC_tag.KILLPASSWORD_PASSWORD_READABLE_WRITABLE, "Kill-password protected"));
        lockTypes.add(new LockType(EPC_tag.ACCESSPASSWORD_UNREADABLE_UNWRITABLE, "Access-password rewrite forbidden"));
        lockTypes.add(new LockType(EPC_tag.KILLPASSWORD_UNREADABLE_UNWRITABLE, "Kill-password rewrite forbidden"));
    }

    public static LockTagDialogFragment newInstance(String tag) {
        LockTagDialogFragment dialog = new LockTagDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
        dialog.setArguments(args);

        return dialog;
    }

    private LockTagListener listener;

    private int selectedLockType;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (LockTagListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement LockTagListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.lock_tag_dialog, null);

        final EditText passwordText = dialogView.findViewById(R.id.lock_tag_password);
        passwordText.addTextChangedListener(new HexDataTextWatcher(passwordText));
        DialogUtils.appendAllDialogInputFilters(passwordText, 8);

        AppCompatSpinner lockTypeSpinner = dialogView.findViewById(R.id.lock_tag_type_spinner);

        ArrayAdapter<LockType> lockTypeArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout
                .simple_spinner_dropdown_item, lockTypes);
        lockTypeSpinner.setAdapter(lockTypeArrayAdapter);
        lockTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLockType = ((LockType) parent.getSelectedItem()).getType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lockTypeSpinner.setSelection(0);

        builder.setView(dialogView)
                .setTitle(getString(R.string.lock_tag_dialog_title, getArguments().getString(TAG_TITLE)))
                .setPositiveButton(R.string.lock_tag_dialog_lock_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onLockTag(selectedLockType, passwordText.getText().toString());
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