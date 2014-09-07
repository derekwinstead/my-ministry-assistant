package com.myMinistry.dialogfragments;

import static com.myMinistry.util.LogUtils.makeLogTag;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.model.DialogItemAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

public class PublicationDialogFragment extends DialogFragment {
	public static final String TAG = makeLogTag(PublicationDialogFragment.class);
	public static final String ARG_PUBLICATION_TYPE_ID = "publication_type_id";
	public static final String ARG_PUBLICATION_NAME = "publication_name";
	
	private LiteratureDialogFragmentListener sListener;
	public static int CREATE_ID = MinistryDatabase.CREATE_ID;
	MinistryService database;
	private int litType = CREATE_ID;
	private DialogItemAdapter adapter;
	
	public static PublicationDialogFragment newInstance(int _typeID, String _name) {
		PublicationDialogFragment frag = new PublicationDialogFragment();
		Bundle args = new Bundle();
        args.putInt(ARG_PUBLICATION_TYPE_ID, _typeID);
        args.putString(ARG_PUBLICATION_NAME, _name);
        frag.setArguments(args);
        return frag;
    }
	
	public interface LiteratureDialogFragmentListener {
	    public void literatureDialogFragmentSet(int _ID, String _name, int _typeID);
	}
	
	public void setLiteratureFragmentListener(LiteratureDialogFragmentListener listener){
		sListener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		adapter = new DialogItemAdapter(getActivity().getApplicationContext());
		String name = getActivity().getApplicationContext().getString(R.string.navdrawer_item_publications);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		database = new MinistryService(getActivity());
		
		Bundle args = getArguments();
		
		if(args != null) {
			if(args.containsKey(ARG_PUBLICATION_TYPE_ID))
				litType = args.getInt(ARG_PUBLICATION_TYPE_ID, 0);
			if(args.containsKey(ARG_PUBLICATION_NAME))
				name = args.getString(ARG_PUBLICATION_NAME);
		}
		
		adapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_with_plus), Helper.getIconResIDByLitTypeID(litType), CREATE_ID));
		
		database.openWritable();
		final Cursor cursor = database.fetchLiteratureByType(litType);
	    
		while(cursor.moveToNext())
			adapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(Literature.NAME)), Helper.getIconResIDByLitTypeID(litType), cursor.getInt(cursor.getColumnIndex(Literature._ID))));
	    
	    cursor.close();
	    database.close();
	    
	    builder.setTitle(name);
	    
	    builder.setAdapter(adapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
    			sListener.literatureDialogFragmentSet(adapter.getItem(which).getID(), adapter.getItem(which).toString(), litType);
			}
		});
	    
	    return builder.create();
    }
}