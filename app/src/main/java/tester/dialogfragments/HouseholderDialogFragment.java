package tester.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.myMinistry.R;
import tester.adapters.DialogItemAdapter;
import tester.model.HouseholderForTime;
import tester.model.NavDrawerMenuItem;
import tester.provider.MinistryContract.Householder;
import tester.provider.MinistryDatabase;
import tester.provider.MinistryService;

import static tester.util.LogUtils.makeLogTag;

public class HouseholderDialogFragment extends DialogFragment {
	public static final String TAG = makeLogTag(HouseholderDialogFragment.class);
	
	private HouseholderDialogFragmentListener sListener;
	public static int CREATE_ID = MinistryDatabase.CREATE_ID;
	HouseholderForTime selectedEntry;
	private DialogItemAdapter adapter;
	
	public static HouseholderDialogFragment newInstance() {
		return new HouseholderDialogFragment();
    }
	
	public interface HouseholderDialogFragmentListener {
	    public void householderDialogFragmentSet(int _ID, String _name);
	}
	
	public void setHouseholderFragmentListener(HouseholderDialogFragmentListener listener) {
		sListener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		MinistryService database = new MinistryService(getActivity());
		adapter = new DialogItemAdapter(getActivity().getApplicationContext());
		
		adapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_with_plus), R.drawable.ic_drawer_householder, CREATE_ID));
		
		database.openWritable();
		final Cursor cursor = database.fetchActiveHouseholders();
	    while(cursor.moveToNext())
			adapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(Householder.NAME)), R.drawable.ic_drawer_householder, cursor.getInt(cursor.getColumnIndex(Householder._ID))));
	    cursor.close();
	    database.close();
	    
	    builder.setTitle(getActivity().getApplicationContext().getString(R.string.navdrawer_item_householders));
	    
	    builder.setAdapter(adapter,  new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
    			sListener.householderDialogFragmentSet(adapter.getItem(which).getID(), adapter.getItem(which).toString());
			}
		});
	    
	    builder.setNeutralButton(R.string.menu_no_householder, new OnClickListener() {
	    	@Override public void onClick(final DialogInterface dialog, final int which) {
	    		sListener.householderDialogFragmentSet(MinistryDatabase.NO_HOUSEHOLDER_ID, "");
	      }
	    });
	    
	    return builder.create();
    }
}