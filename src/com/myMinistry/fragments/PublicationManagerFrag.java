package com.myMinistry.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.HelpUtils;
import com.myMinistry.util.PrefUtils;

public class PublicationManagerFrag extends ListFragment {
	private boolean is_dual_pane = false;
	
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private ContentValues values = null;
	private MinistryService database;
	private FragmentManager fm;
	private FragmentActivityStatus fragmentActivityStatus;
	
	public PublicationManagerFrag newInstance() {
		return new PublicationManagerFrag();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.publication_manager, container, false);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.sorting, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
        if(menu.findItem(R.id.manage_new) != null)
    		menu.removeItem(R.id.manage_new);

        if(menu.findItem(R.id.sort_container) != null)
    		menu.findItem(R.id.sort_container).setVisible(!drawerOpen);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.sort_alpha:
				PrefUtils.setPublicationTypeSort(getActivity(), MinistryDatabase.SORT_BY_ASC);
				HelpUtils.sortPublicationTypes(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_ASC);
				reloadCursor();
				return true;
			case R.id.sort_alpha_desc:
				PrefUtils.setPublicationTypeSort(getActivity(), MinistryDatabase.SORT_BY_DESC);
				HelpUtils.sortPublicationTypes(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_DESC);
				reloadCursor();
				return true;
			case R.id.sort_most_placed:
				PrefUtils.setPublicationTypeSort(getActivity(), MinistryDatabase.SORT_BY_POPULAR);
				HelpUtils.sortPublicationTypes(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_POPULAR);
				reloadCursor();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	setHasOptionsMenu(true);
    	
    	fm = getActivity().getSupportFragmentManager();
    	
    	getActivity().setTitle(R.string.form_publication_types);
    	
    	database = new MinistryService(getActivity().getApplicationContext());
		database.openWritable();
		
		loadCursor();
		adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.li_bg_card_tv, cursor, new String[] {LiteratureType.NAME}, new int[] {android.R.id.text1});
		setListAdapter(adapter);
        
        database.close();
    	
        if (is_dual_pane) {
    		Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
    		PublicationManagerEditorFrag f = new PublicationManagerEditorFrag().newInstance(MinistryDatabase.CREATE_ID);
        	
    		FragmentTransaction ft = fm.beginTransaction();
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        	
        	if(frag != null)
        		ft.remove(frag);
        	
        	ft.add(R.id.secondary_fragment_container, f);
        	
        	ft.commit();
    	}
	}
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	if((int)id > MinistryDatabase.MAX_PUBLICATION_TYPE_ID) {
    		showTransferToDialog((int)id, cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)));
    	} else {
			if(is_dual_pane) {
				PublicationManagerEditorFrag f = (PublicationManagerEditorFrag) fm.findFragmentById(R.id.secondary_fragment_container);
				f.switchForm(id);
			} else {
				cursor.moveToPosition(position);
				createDialog(id, cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), cursor.getInt(cursor.getColumnIndex(LiteratureType.ACTIVE)));
			}
    	}
    }
	
    private void createDialog(final long id, String name, int isActive) {
    	if((int)id <= MinistryDatabase.MAX_PUBLICATION_TYPE_ID)
			showEditTextDialog((int)id,name,isActive);
    	else
    		showTransferToDialog((int) id, name);
    }
    
	@SuppressLint("InflateParams")
	private void showEditTextDialog(final int id, String name, int isActive) {
		View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.d_edit_text, null);
    	AlertDialog.Builder builder = new Builder(PublicationManagerFrag.this.getActivity());
    	final EditText editText = (EditText) view.findViewById(R.id.text1);
    	
    	editText.setText(name);
    	
    	builder.setView(view);
    	builder.setTitle(R.string.form_rename);
		builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
		builder.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(values == null)
					values = new ContentValues();
				
				values.put(LiteratureType.NAME, editText.getText().toString());
				values.put(LiteratureType.ACTIVE, MinistryService.ACTIVE);
				
				database.openWritable();
				database.savePublicationType(id, values);
				reloadCursor();
				database.close();
			}
		});
		builder.show();
    }
	
	public void showTransferToDialog(final int id, final String name) {
		AlertDialog.Builder builder = new Builder(PublicationManagerFrag.this.getActivity());
    	database.openWritable();
		final Cursor defaults = database.fetchDefaultPublicationTypes();
		builder.setTitle(R.string.menu_transfer_to);
		builder.setCursor(defaults, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				defaults.moveToPosition(which);
				database.reassignPublications(id, defaults.getInt(defaults.getColumnIndex(LiteratureType._ID)));
				database.removePublication(id);
				
				HelpUtils.sortPublicationTypes(getActivity().getApplicationContext(), PrefUtils.getPublicationTypeSort(getActivity()));
				reloadCursor();
			}
		}, LiteratureType.NAME);
		builder.show();
	}
	
	private void loadCursor() {
		if(!database.isOpen())
			database.openWritable();
		cursor = database.fetchAllPublicationTypes();
	}
	
	public void reloadCursor() {
		loadCursor();
		adapter.changeCursor(cursor);
	}
}