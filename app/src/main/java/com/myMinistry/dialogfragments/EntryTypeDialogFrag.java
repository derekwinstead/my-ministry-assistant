package com.myMinistry.dialogfragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.utils.AppConstants;

public class EntryTypeDialogFrag extends DialogFragment {
    public static final String ARG_ID = "notes";
    private EntryTypeDialogFragListener sListener;
    private View view;

    public static EntryTypeDialogFrag newInstance(int id, String name, int isActive) {
        EntryTypeDialogFrag f = new EntryTypeDialogFrag();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(AppConstants.ARG_NAME, name);
        args.putInt(AppConstants.ARG_IS_ACTIVE, isActive);
        f.setArguments(args);
        return f;
    }

    public interface EntryTypeDialogFragListener {
        void setPositiveButton(String name, int isActive);
    }

    public void setPositiveButton(EntryTypeDialogFragListener listener) {
        sListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.d_edit_text_with_cb, null);
        Bundle args = getArguments();

        int id = AppConstants.CREATE_ID;

        EditText et_text1 = view.findViewById(R.id.text1);
        CheckBox cb_is_active = view.findViewById(R.id.cb_is_active);
        TextView tv_note = view.findViewById(R.id.tv_note);

        if (args != null) {
            id = args.getInt(ARG_ID);
            et_text1.setText(args.getString(AppConstants.ARG_NAME));
            cb_is_active.setChecked(args.getInt(AppConstants.ARG_IS_ACTIVE) != 0 ? true : false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (id == AppConstants.ID_ENTRY_TYPE_ROLLOVER) {
            cb_is_active.setEnabled(false);
            cb_is_active.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.holo_grey_light));
            tv_note.setVisibility(View.VISIBLE);
        }

        builder.setView(view);
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.form_rename));
        builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
        builder.setPositiveButton(R.string.menu_save, PositiveButtonListener);
        return builder.create();
    }

    private DialogInterface.OnClickListener PositiveButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            EditText editText = view.findViewById(R.id.text1);
            CheckBox cb = view.findViewById(R.id.cb_is_active);
            sListener.setPositiveButton(editText.getText().toString(), cb.isChecked() ? 1 : 0);
        }
    };
}