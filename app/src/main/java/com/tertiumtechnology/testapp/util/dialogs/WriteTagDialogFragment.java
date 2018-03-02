package com.tertiumtechnology.testapp.util.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.tertiumtechnology.testapp.R;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteTagDialogFragment extends AppCompatDialogFragment {

    public interface WriteTagListener {
        void onWriteTag(int address, String hexData);
    }

    private static class HexDataTextWatcher implements TextWatcher {

        private final WeakReference<EditText> weakReferenceDataText;

        public HexDataTextWatcher(EditText dataText) {
            this.weakReferenceDataText = new WeakReference<>(dataText);
        }

        @Override
        public void afterTextChanged(Editable s) {
            String hexPattern = "a-fA-F0-9";

            int currSelectionEnd = Selection.getSelectionEnd(s);
            int textLength = s.length();

            if (textLength > 0) {
                StringBuilder replacement = new StringBuilder();
                for (int i = 0; i < textLength; i++) {
                    replacement.append(s.charAt(i));
                }

                Matcher matcher = Pattern.compile("[" + hexPattern + "]+").matcher(replacement);
                if (!matcher.matches()) {
                    String replaced = replacement.toString().replaceAll("[^" + hexPattern + "]+", "");

                    SpannableStringBuilder spannable = new SpannableStringBuilder(replaced);
                    TextUtils.copySpansFrom(s, 0, spannable.length(), null, spannable, 0);

                    int selection = Math.max(-1, Math.min(currSelectionEnd - 1, spannable.length()));

                    EditText dataText = weakReferenceDataText.get();

                    if (dataText != null) {
                        dataText.setText(spannable);
                        dataText.setSelection(selection);
                    }
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private static final String TAG_TITLE = "TAG_TITLE";

    public static WriteTagDialogFragment newInstance(String tag) {
        WriteTagDialogFragment dialog = new WriteTagDialogFragment();

        Bundle args = new Bundle();
        args.putString(TAG_TITLE, tag);
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


        appendInputFilters(dataText);

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

                        listener.onWriteTag(address, dataText.getText().toString());
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

    private void appendInputFilters(EditText dataText) {
        InputFilter[] dataTextFilters = dataText.getFilters();

        InputFilter[] newDataTextFilters = new InputFilter[dataTextFilters.length + 2];

        System.arraycopy(dataTextFilters, 0, newDataTextFilters, 0, dataTextFilters.length);
        newDataTextFilters[dataTextFilters.length] = new InputFilter.AllCaps();
        newDataTextFilters[dataTextFilters.length + 1] = new InputFilter.LengthFilter(16);

        dataText.setFilters(newDataTextFilters);
    }
}