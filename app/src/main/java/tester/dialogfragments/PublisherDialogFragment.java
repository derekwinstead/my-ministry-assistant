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
import tester.model.NavDrawerMenuItem;
import tester.provider.MinistryContract.Publisher;
import tester.provider.MinistryDatabase;
import tester.provider.MinistryService;

import static tester.util.LogUtils.makeLogTag;

public class PublisherDialogFragment extends DialogFragment {
	public static final String TAG = makeLogTag(PublisherDialogFragment.class);
	
	private PublisherDialogFragmentListener sListener;
	public static int CREATE_ID = MinistryDatabase.CREATE_ID;
	private DialogItemAdapter adapter;
	
	public static PublisherDialogFragment newInstance() {
        return new PublisherDialogFragment();
    }
	
	public interface PublisherDialogFragmentListener {
	    public void publisherDialogFragmentSet(int _ID, String _name);
	}
	
	public void setPublisherDialogFragmentListener(PublisherDialogFragmentListener listener) {
		sListener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		final MinistryService database = new MinistryService(getActivity());
		adapter = new DialogItemAdapter(getActivity().getApplicationContext());
		
		adapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_with_plus), R.drawable.ic_drawer_publisher, CREATE_ID));
		
		database.openWritable();
		final Cursor cursor = database.fetchActivePublishers();
        while(cursor.moveToNext())
        	adapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(Publisher.NAME)), R.drawable.ic_drawer_publisher, cursor.getInt(cursor.getColumnIndex(Publisher._ID))));
        cursor.close();
        database.close();
        
        return new AlertDialog.Builder(getActivity())
			.setTitle(getActivity().getApplicationContext().getString(R.string.navdrawer_item_publishers))
			.setAdapter(adapter, publisherSetListener)
			.create();
    }
	
	private OnClickListener publisherSetListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			sListener.publisherDialogFragmentSet(adapter.getItem(which).getID(), adapter.getItem(which).toString());
		}
	};
}