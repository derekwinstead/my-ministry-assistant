package com.myMinistry.dialogfragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryService;

public class EntryTypeNewDialogFrag extends DialogFragment {
    private EntryTypeNewDialogFragListener sListener;
    private View view;

    public static EntryTypeNewDialogFrag newInstance() {
        return new EntryTypeNewDialogFrag();
    }

    public interface EntryTypeNewDialogFragListener {
        public void setPositiveButton(boolean created);
    }

    public void setPositiveButton(EntryTypeNewDialogFragListener listener) {
        sListener = listener;
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.d_edit_text, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.form_name));
        builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
        builder.setPositiveButton(R.string.menu_save, PositiveButtonListener);
        return builder.create();
    }

    private DialogInterface.OnClickListener PositiveButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            /** Get the input value */
            EditText editText = (EditText) view.findViewById(R.id.text1);
            String _name = editText.getText().toString();
            if(!TextUtils.isEmpty(_name)) {
                /** Create a new householder */
                ContentValues values = new ContentValues();
                values.put(EntryType.NAME, _name.trim());
                values.put(EntryType.ACTIVE, MinistryService.ACTIVE);
                values.put(EntryType.RBC, MinistryService.INACTIVE);

                MinistryService database = new MinistryService(getActivity());
                database.openWritable();
                database.createEntryType(values);
                database.close();
            }
            /** Call back to the DialogFragment listener */
            sListener.setPositiveButton(true);
        }
    };
}