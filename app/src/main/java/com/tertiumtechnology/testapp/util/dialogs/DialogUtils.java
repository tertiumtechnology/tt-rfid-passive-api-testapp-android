package com.tertiumtechnology.testapp.util.dialogs;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogUtils {

    public static class HexDataTextWatcher implements TextWatcher {

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

    private static void appendAllCapsInputFilter(EditText dataText) {
        InputFilter[] dataTextFilters = dataText.getFilters();

        InputFilter[] newDataTextFilters = new InputFilter[dataTextFilters.length + 1];

        System.arraycopy(dataTextFilters, 0, newDataTextFilters, 0, dataTextFilters.length);
        newDataTextFilters[dataTextFilters.length] = new InputFilter.AllCaps();

        dataText.setFilters(newDataTextFilters);
    }

    private static void appendMaxLengthInputFilter(EditText dataText, int maxLength) {
        InputFilter[] dataTextFilters = dataText.getFilters();

        InputFilter[] newDataTextFilters = new InputFilter[dataTextFilters.length + 1];

        System.arraycopy(dataTextFilters, 0, newDataTextFilters, 0, dataTextFilters.length);
        newDataTextFilters[dataTextFilters.length] = new InputFilter.LengthFilter(maxLength);

        dataText.setFilters(newDataTextFilters);
    }

    static void appendAllDialogInputFilters(EditText dataText, int maxLength) {
        appendAllCapsInputFilter(dataText);
        appendMaxLengthInputFilter(dataText, maxLength);
    }
}
