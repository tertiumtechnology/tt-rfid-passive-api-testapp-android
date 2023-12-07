package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;

import com.tertiumtechnology.testapp.R;
import com.tertiumtechnology.testapp.util.format.BadFormatException;
import com.tertiumtechnology.testapp.util.format.SgtinFormat;

import java.util.ArrayList;

public class WriteIdDialogFragment extends AppCompatDialogFragment {

    public enum WriteIdType {
        HEX("HEX"), ASCII("ASCII"), SGTIN("SGTIN");

        private final String value;

        WriteIdType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public interface WriteIdListener {
        void onWriteId(String hexId, short nsi);
    }
//
//    private static class WriteIdType {
//        private final int type;
//        private final String name;
//
//        WriteIdType(int type, String name) {
//            this.type = type;
//            this.name = name;
//        }
//
//        @Override
//        public String toString() {
//            return name;
//        }
//
//        String getName() {
//            return name;
//        }
//
//        int getType() {
//            return type;
//        }
//    }

    private static final String TAG_TITLE = "TAG_TITLE";

    private static final ArrayList<WriteIdType> writeIdTypes = new ArrayList<>();

    static {
        writeIdTypes.add(WriteIdType.HEX);
        writeIdTypes.add(WriteIdType.ASCII);
        writeIdTypes.add(WriteIdType.SGTIN);
    }

    public static WriteIdDialogFragment newInstance(String tag) {
        WriteIdDialogFragment dialog = new WriteIdDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
        dialog.setArguments(args);

        return dialog;
    }

    private WriteIdListener listener;

    private WriteIdType selectedWriteIdType;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (WriteIdListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement WriteIdListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.write_id_dialog, null);

        final EditText writeIdData = dialogView.findViewById(R.id.write_id_data);
        final EditText writeIdNsi = dialogView.findViewById(R.id.write_id_nsi);

        DialogUtils.appendAllCapsInputFilter(writeIdData);
        DialogUtils.appendAllCapsInputFilter(writeIdNsi);

        AppCompatSpinner wrietIdTypeSpinner = dialogView.findViewById(R.id.write_id_data_type_spinner);

        ArrayAdapter<WriteIdType> writeIdTypeArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout
                .simple_spinner_dropdown_item, writeIdTypes);
        wrietIdTypeSpinner.setAdapter(writeIdTypeArrayAdapter);
        wrietIdTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWriteIdType = ((WriteIdType) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        wrietIdTypeSpinner.setSelection(0);

        builder.setView(dialogView)
                .setTitle(getString(R.string.write_id_dialog_title, getArguments().getString(TAG_TITLE)))
                .setPositiveButton(R.string.write_id_dialog_write_button, (dialog, id) -> {
                    String idToWrite = writeIdData.getText().toString();

                    if (selectedWriteIdType == WriteIdType.ASCII) {
                        idToWrite = asciiToHex(idToWrite);
                        // rightPad
                        idToWrite = String.format("%1$-" + 24 + "s", idToWrite).replace(' ', '0');
                    } else if (selectedWriteIdType == WriteIdType.SGTIN) {
                        try {
                            idToWrite = SgtinFormat.SGTIN96toEPChex(idToWrite);
                        } catch (BadFormatException e) {
                            Toast.makeText(getContext(), R.string.error_write_id_bad_format, Toast.LENGTH_SHORT).show();
                        }
                    }

                    short nsiToWrite = 0;

                    try {
                        nsiToWrite = Short.parseShort(writeIdNsi.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), R.string.error_write_id_invalid_nsi, Toast.LENGTH_SHORT).show();
                    }

                    listener.onWriteId(idToWrite, nsiToWrite);
                })
                .setNegativeButton(R.string.dialog_cancel_button, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private String asciiToHex(String asciiString) {
        char[] chars = asciiString.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();
    }
}