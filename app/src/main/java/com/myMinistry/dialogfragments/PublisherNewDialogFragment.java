package com.myMinistry.dialogfragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryService;

public class PublisherNewDialogFragment extends DialogFragment {
	private PublisherNewDialogFragmentListener sListener;
	private View view;
	private MinistryService database;
	
	public interface PublisherNewDialogFragmentListener {
	    public void setPositiveButton(int _ID, String _name);
	}
	
	public static PublisherNewDialogFragment newInstance() {
		return new PublisherNewDialogFragment();
    }
	
	public void setPositiveButton(PublisherNewDialogFragmentListener listener) {
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
			long _newID = 0;
			if(!TextUtils.isEmpty(_name)) {
				ContentValues values = new ContentValues();
				values.put(Publisher.NAME, _name.trim());
				values.put(Publisher.ACTIVE, MinistryService.ACTIVE);
				
				database = new MinistryService(getActivity());
				database.openWritable();
				_newID = database.createPublisher(values);
				database.close();
			}
			/** Call back to the DialogFragment listener */
			sListener.setPositiveButton((int)_newID, _name);
		}
	};
}