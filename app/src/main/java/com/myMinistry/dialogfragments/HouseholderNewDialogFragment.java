package com.myMinistry.dialogfragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.myMinistry.R;
import com.myMinistry.bean.Householder;
import com.myMinistry.db.HouseholderDAO;
import com.myMinistry.utils.HelpUtils;

import androidx.fragment.app.DialogFragment;

public class HouseholderNewDialogFragment extends DialogFragment {
    private HouseholderNewDialogFragmentListener sListener;
    private View view;

    public interface HouseholderNewDialogFragmentListener {
        void setPositiveButton(int _ID, String _name, Boolean _isReturnVisit);
    }

    public static HouseholderNewDialogFragment newInstance() {
        return new HouseholderNewDialogFragment();
    }

    public void setPositiveButton(HouseholderNewDialogFragmentListener listener) {
        sListener = listener;
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.d_edit_text_with_cb_return_visit, null);
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
            /* Get the input value */
            EditText editText = view.findViewById(R.id.text1);
            CheckBox cb = view.findViewById(R.id.cb_is_not_return_visit);
            String _name = editText.getText().toString();
            long _newID = 0;
            if (!TextUtils.isEmpty(_name)) {
                Householder householder = new Householder(
                        _name.trim()
                        ,null
                        ,null
                        ,null
                        ,null
                        ,null
                         ,HelpUtils.booleanConversionsToInt(true)
                        ,HelpUtils.booleanConversionsToInt(false)
                );


                //Householder householder = new Householder();
                //householder.setName(_name.trim());
                //householder.setIsActive(true);
                //householder.setIsDefault(false);
                _newID = new HouseholderDAO(getActivity().getApplicationContext()).create(householder);
                //householder.setId(_newID);
            }
            /* Call back to the DialogFragment listener */
            sListener.setPositiveButton((int) _newID, _name, cb.isChecked());
        }
    };
}