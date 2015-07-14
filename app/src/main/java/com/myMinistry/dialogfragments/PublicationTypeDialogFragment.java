package com.myMinistry.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.DialogItemAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

public class PublicationTypeDialogFragment extends DialogFragment {
	public static final String ARG_SHOW_FLOW = "show_flow";
	
	private LiteratureTypeDialogFragmentListener sListener;
	private DialogItemAdapter adapter;
	private boolean showFlow = false;
	
	public static PublicationTypeDialogFragment newInstance(boolean _showFlow) {
		PublicationTypeDialogFragment frag = new PublicationTypeDialogFragment();
		Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_FLOW, _showFlow);
        frag.setArguments(args);
        return frag; 
    }
	
	public interface LiteratureTypeDialogFragmentListener {
	    public void LiteratureTypeDialogFragmentListenerSet(int _ID, String _name);
	}
	
	public void setLiteratureTypeDialogFragmentListener(LiteratureTypeDialogFragmentListener listener){
		sListener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		MinistryService database = new MinistryService(getActivity());
		adapter = new DialogItemAdapter(getActivity().getApplicationContext());
		
		Bundle args = getArguments();
		
		if(args != null) {
			if(args.containsKey(ARG_SHOW_FLOW))
				showFlow = args.getBoolean(ARG_SHOW_FLOW, false);
		}
		
		database.openWritable();
		final Cursor cursor = database.fetchActiveTypesOfLiterature();
		
		while(cursor.moveToNext())
			adapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))), cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))));
		
		cursor.close();
		database.close();
		
		builder.setTitle(getActivity().getApplicationContext().getString(R.string.navdrawer_item_publications));
		
		builder.setAdapter(adapter,  new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
    			sListener.LiteratureTypeDialogFragmentListenerSet(adapter.getItem(which).getID(), adapter.getItem(which).toString());
			}
		});
		
		if(showFlow) {
		    builder.setNegativeButton(R.string.menu_no_publications, new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					sListener.LiteratureTypeDialogFragmentListenerSet(MinistryDatabase.CREATE_ID, "");
				}
		    });
		}
	    
	    return builder.create();
    }
}