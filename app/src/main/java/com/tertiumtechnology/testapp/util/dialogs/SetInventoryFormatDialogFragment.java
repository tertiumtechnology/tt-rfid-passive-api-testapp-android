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

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;

import com.tertiumtechnology.api.rfidpassiveapilib.PassiveReader;
import com.tertiumtechnology.testapp.R;

import java.util.ArrayList;

public class SetInventoryFormatDialogFragment extends AppCompatDialogFragment {

    public interface SetInventoryFormatTagListener {
        void onSetFormat(int inventoryFormat);
    }

    private static class InventoryFormatType {
        private final int type;
        private final String name;

        InventoryFormatType(int type, String name) {
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

    private static final ArrayList<InventoryFormatType> INVENTORY_FORMAT_TYPES = new ArrayList<>();

    static {
        INVENTORY_FORMAT_TYPES.add(new InventoryFormatType(PassiveReader.EPC_ONLY_FORMAT, "EPC ONLY"));
        INVENTORY_FORMAT_TYPES.add(new InventoryFormatType(PassiveReader.EPC_AND_PC_FORMAT, "PC & EPC"));
    }

    public static SetInventoryFormatDialogFragment newInstance() {
        SetInventoryFormatDialogFragment dialog = new SetInventoryFormatDialogFragment();

        return dialog;
    }

    private SetInventoryFormatTagListener listener;

    private int selectedInventoryFormatType;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (SetInventoryFormatTagListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement SetInventoryFormatTagListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.set_inventory_format_dialog, null);

        AppCompatSpinner inventoryFormatTypeSpinner = dialogView.findViewById(R.id.set_inventory_format_type_spinner);

        ArrayAdapter<InventoryFormatType> inventoryFormatTypeArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout
                .simple_spinner_dropdown_item, INVENTORY_FORMAT_TYPES);
        inventoryFormatTypeSpinner.setAdapter(inventoryFormatTypeArrayAdapter);
        inventoryFormatTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedInventoryFormatType = ((InventoryFormatType) parent.getSelectedItem()).getType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        inventoryFormatTypeSpinner.setSelection(0);

        builder.setView(dialogView)
                .setTitle(getString(R.string.set_inventory_format_dialog_title))
                .setPositiveButton(R.string.set_inventory_format_dialog_button, (dialog, id) -> listener.onSetFormat(selectedInventoryFormatType))
                .setNegativeButton(R.string.dialog_cancel_button, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}