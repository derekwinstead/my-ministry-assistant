package com.myMinistry.dialogfragments;

import static com.myMinistry.util.LogUtils.makeLogTag;
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
import android.widget.CheckBox;
import android.widget.EditText;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryService;

public class HouseholderNewDialogFragment extends DialogFragment {
	public static final String TAG = makeLogTag(HouseholderNewDialogFragment.class);
	
	private HouseholderNewDialogFragmentListener sListener;
	private View view;
	private MinistryService database;
	
	public interface HouseholderNewDialogFragmentListener {
	    public void setPositiveButton(int _ID, String _name, Boolean _isReturnVisit);
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
			/** Get the input value */
			EditText editText = (EditText) view.findViewById(R.id.text1);
			CheckBox cb = (CheckBox) view.findViewById(R.id.cb_is_not_return_visit);
			String _name = editText.getText().toString();
			long _newID = 0;
			if(!TextUtils.isEmpty(_name)) {
            	/** Create a new householder */
				ContentValues values = new ContentValues();
				values.put(Householder.NAME, _name.trim());
				values.put(Householder.ACTIVE, MinistryService.ACTIVE);
				
				database = new MinistryService(getActivity());
				database.openWritable();
				_newID = database.createHouseholder(values);
				database.close();
			}
			/** Call back to the DialogFragment listener */
			sListener.setPositiveButton((int)_newID, _name, cb.isChecked());
		}
	};
}