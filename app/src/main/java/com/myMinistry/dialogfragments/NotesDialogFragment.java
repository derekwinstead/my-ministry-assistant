package com.myMinistry.dialogfragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.myMinistry.R;
import com.myMinistry.utils.AppConstants;

public class NotesDialogFragment extends DialogFragment {
	private NotesDialogFragmentListener sListener;
	private View view;
	
	public interface NotesDialogFragmentListener {
	    void setPositiveButton(String _notes);
	}
	
	public static NotesDialogFragment newInstance() {
		return new NotesDialogFragment();
    }
	
	public static NotesDialogFragment newInstance(String notes) {
		NotesDialogFragment f = new NotesDialogFragment();
		Bundle args = new Bundle();
		args.putString(AppConstants.ARG_NOTES, notes);
		f.setArguments(args);
		return f;
    }
	
	public void setPositiveButton(NotesDialogFragmentListener listener) {
		sListener = listener;
	}
	
	@SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		view = LayoutInflater.from(getActivity()).inflate(R.layout.d_textarea, null);
		Bundle args = getArguments();
		
		if(args != null) {
			if(args.containsKey(AppConstants.ARG_NOTES)) {
				EditText et_text1 = view.findViewById(R.id.text1);
				et_text1.setText(args.getString(AppConstants.ARG_NOTES));
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view);
		builder.setTitle(getActivity().getApplicationContext().getString(R.string.form_notes));
		builder.setPositiveButton(R.string.menu_save, PositiveButtonListener);
		return builder.create();
    }
	
	private DialogInterface.OnClickListener PositiveButtonListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			EditText editText = view.findViewById(R.id.text1);
			sListener.setPositiveButton(editText.getText().toString());
		}
	};
}