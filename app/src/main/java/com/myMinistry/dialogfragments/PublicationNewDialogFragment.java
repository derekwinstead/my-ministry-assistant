package com.myMinistry.dialogfragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.myMinistry.R;
import com.myMinistry.bean.Publication;
import com.myMinistry.db.PublicationDAO;

public class PublicationNewDialogFragment extends DialogFragment {
    public static final String ARG_PUBLICATION_TYPE_ID = "publication_type_id";

    private LiteratureNewDialogFragmentListener sListener;
    private View view;
    private int litTypeID = 0;

    public interface LiteratureNewDialogFragmentListener {
        void setPositiveButton(int _ID, String _name, int _litTypeID);
    }

    public static PublicationNewDialogFragment newInstance(int _litTypeID) {
        PublicationNewDialogFragment f = new PublicationNewDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PUBLICATION_TYPE_ID, _litTypeID);
        f.setArguments(args);
        return f;
    }

    public void setPositiveButton(LiteratureNewDialogFragmentListener listener) {
        sListener = listener;
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.d_edit_text, null);
        Bundle args = getArguments();

        if (args != null) {
            if (args.containsKey(ARG_PUBLICATION_TYPE_ID)) {
                litTypeID = getArguments().getInt(ARG_PUBLICATION_TYPE_ID);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationNewDialogFragment.this.getActivity());
        builder.setView(view);
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.form_name));
        builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
        builder.setPositiveButton(R.string.menu_create, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the input values
                EditText editText = view.findViewById(R.id.text1);
                String _name = editText.getText().toString();
                long _newID = 0;
                if (!TextUtils.isEmpty(_name)) {
                    Publication publication = new Publication();
                    publication.setName(_name.trim());
                    publication.setTypeId(litTypeID);
                    _newID = new PublicationDAO(getActivity().getApplicationContext()).create(publication);
                    publication.setId(_newID);
                }
                // Call back to the DialogFragment listener
                sListener.setPositiveButton((int) _newID, _name, litTypeID);
            }
        });
        return builder.create();
    }
}