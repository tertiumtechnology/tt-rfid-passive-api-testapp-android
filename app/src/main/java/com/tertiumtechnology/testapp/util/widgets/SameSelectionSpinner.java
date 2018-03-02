package com.tertiumtechnology.testapp.util.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

public class SameSelectionSpinner extends AppCompatSpinner {

    public SameSelectionSpinner(Context context) {
        super(context);
    }

    public SameSelectionSpinner(Context context, int mode) {
        super(context, mode);
    }

    public SameSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SameSelectionSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SameSelectionSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public SameSelectionSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme
            popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);

        if (sameSelected) {
            OnItemSelectedListener listener = getOnItemSelectedListener();
            if (listener != null) {
                listener.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);

        if (sameSelected) {
            OnItemSelectedListener listener = getOnItemSelectedListener();
            if (listener != null) {
                listener.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }
}
