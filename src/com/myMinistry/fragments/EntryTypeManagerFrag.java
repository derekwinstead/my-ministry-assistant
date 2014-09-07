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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.dialogfragments.EntryTypeNewDialogFrag;
import com.myMinistry.dialogfragments.EntryTypeNewDialogFrag.EntryTypeNewDialogFragListener;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.PrefUtils;

public class EntryTypeManagerFrag extends ListFragment {
	private boolean is_dual_pane = false;
	
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private ContentValues values = null;
	private MinistryService database;
	private FragmentManager fm;
	private FragmentActivityStatus fragmentActivityStatus;
	
	private final int RENAME_ID = 0;
	private final int TRANSFER_ID = 1;
	private final int DELETE_ID = 2;
	
	public EntryTypeManagerFrag newInstance() {
		return new EntryTypeManagerFrag();
    }
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.entry_type_manager, container, false);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.sorting_used, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
        if(menu.findItem(R.id.sort_container) != null)
    		menu.findItem(R.id.sort_container).setVisible(!drawerOpen);
        if(menu.findItem(R.id.manage_new) != null)
    		menu.findItem(R.id.manage_new).setVisible(!drawerOpen);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.manage_new:
				if(is_dual_pane) {
					populateEditor(MinistryDatabase.CREATE_ID);
				}
				else {
					EntryTypeNewDialogFrag f = EntryTypeNewDialogFrag.newInstance();
					f.setPositiveButton(new EntryTypeNewDialogFragListener() {
						@Override
						public void setPositiveButton(boolean created) {
							sortList(PrefUtils.getEntryTypeSort(getActivity()));
						}
					});
					f.show(fm, EntryTypeNewDialogFrag.class.getName());
				}
				return true;
			case R.id.sort_alpha:
				PrefUtils.setEntryTypeSort(getActivity(), MinistryDatabase.SORT_BY_ASC);
				sortList(MinistryDatabase.SORT_BY_ASC);
				return true;
			case R.id.sort_alpha_desc:
				PrefUtils.setEntryTypeSort(getActivity(), MinistryDatabase.SORT_BY_DESC);
				sortList(MinistryDatabase.SORT_BY_DESC);
				return true;
			case R.id.sort_most_used:
				PrefUtils.setEntryTypeSort(getActivity(), MinistryDatabase.SORT_BY_POPULAR);
				sortList(MinistryDatabase.SORT_BY_POPULAR);
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
    	
    	database = new MinistryService(getActivity().getApplicationContext());
		database.openWritable();
		
		loadCursor();
		adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.li_bg_card_tv, cursor, new String[] {EntryType.NAME}, new int[] {android.R.id.text1});
		setListAdapter(adapter);
        
        database.close();
    	
        if (is_dual_pane) {
    		Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
    		EntryTypeManagerEditorFrag f = new EntryTypeManagerEditorFrag().newInstance(MinistryDatabase.CREATE_ID);
        	FragmentTransaction ft = fm.beginTransaction();
        	
        	if(frag != null)
        		ft.remove(frag);
        	
        	ft.add(R.id.secondary_fragment_container, f);
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        	ft.addToBackStack(null);
        	
        	ft.commit();
    	}
	}
	
	private void populateEditor(long id) {
		EntryTypeManagerEditorFrag f = (EntryTypeManagerEditorFrag) fm.findFragmentById(R.id.secondary_fragment_container);
		f.switchForm(id);
	}
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		cursor.moveToPosition(position);
		createDialog(id, cursor.getString(cursor.getColumnIndex(EntryType.NAME)), cursor.getInt(cursor.getColumnIndex(EntryType.ACTIVE)));
    }
	
    private void createDialog(final long id, String name, int isActive) {
		switch((int)id) {
		case MinistryDatabase.ID_ROLLOVER:
		case MinistryDatabase.ID_BIBLE_STUDY:
		case MinistryDatabase.ID_RETURN_VISIT:
		case MinistryDatabase.ID_SERVICE:
		case MinistryDatabase.ID_RBC:
		case MinistryDatabase.CREATE_ID:
			if(is_dual_pane)
				populateEditor(id);
			else
				showEditTextDialog((int)id,name,isActive);
				
			break;
		default:
			showListItems((int)id,name,isActive);
		}
    }
	
	@SuppressLint("InflateParams")
	private void showEditTextDialog(final int id, String name, int isActive) {
		View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.d_edit_text_with_cb, null);
    	AlertDialog.Builder builder = new Builder(EntryTypeManagerFrag.this.getActivity());
    	final EditText editText = (EditText) view.findViewById(R.id.text1);
    	final CheckBox cb_is_active = (CheckBox) view.findViewById(R.id.cb_is_active);
    	
    	editText.setText(name);
    	cb_is_active.setChecked(isActive != 0 ? true : false);
    	
    	builder.setView(view);
    	builder.setTitle(id != MinistryDatabase.CREATE_ID ? R.string.form_rename : R.string.form_name);
		builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
		builder.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(values == null)
					values = new ContentValues();
				
				values.put(EntryType.NAME, editText.getText().toString());
				values.put(EntryType.RBC, id != MinistryDatabase.ID_RBC ? MinistryService.INACTIVE : MinistryService.ACTIVE);
				
				if(id != MinistryDatabase.ID_ROLLOVER)
					values.put(EntryType.ACTIVE, (cb_is_active.isChecked()) ? MinistryService.ACTIVE : MinistryService.INACTIVE);
				else
					values.put(EntryType.ACTIVE, MinistryService.INACTIVE);
				
				database.openWritable();
				
				if(id != MinistryDatabase.CREATE_ID)
					database.saveEntryType(id, values);
				else
					database.createEntryType(values);
				
				reloadCursor();
				
				database.close();
			}
		});
		builder.show();
    }
	
	public void showListItems(final int id, final String name, final int isActive) {
		AlertDialog.Builder builder = new Builder(EntryTypeManagerFrag.this.getActivity());
		builder.setTitle(R.string.menu_options);
		builder.setItems(getResources().getStringArray(R.array.entry_type_list_item_options), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case RENAME_ID:
					if(is_dual_pane) {
						populateEditor(id);
					}
					else {
						showEditTextDialog(id, name, isActive);
						sortList(PrefUtils.getEntryTypeSort(getActivity()));
					}
					break;
				case TRANSFER_ID:
					showTransferToDialog(id, name);
					break;
				case DELETE_ID:
					database.openWritable();
					database.deleteEntryTypeByID(id);
					if(is_dual_pane)
						populateEditor(MinistryDatabase.CREATE_ID);
					sortList(PrefUtils.getEntryTypeSort(getActivity()));
					database.close();
					break;
				}
			}
		});
		builder.show();
	}
	
	public void showTransferToDialog(final int id, final String name) {
		AlertDialog.Builder builder = new Builder(EntryTypeManagerFrag.this.getActivity());
    	database.openWritable();
		final Cursor defaults = database.fetchAllEntryTypesButID(id);
		builder.setTitle(R.string.menu_transfer_to);
		builder.setCursor(defaults, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				defaults.moveToPosition(which);
				database.reassignEntryType(id, defaults.getInt(defaults.getColumnIndex(EntryType._ID)));
				sortList(PrefUtils.getEntryTypeSort(getActivity()));
			}
		}, EntryType.NAME);
		builder.show();
	}
	
	private void loadCursor() {
		if(!database.isOpen())
			database.openWritable();
		cursor = database.fetchAllEntryTypes();
	}
	
	public void reloadCursor() {
		loadCursor();
		adapter.changeCursor(cursor);
	}
	
	public void sortList(int how_to_sort) {
		if(!database.isOpen())
			database.openWritable();
		
		if(how_to_sort == MinistryDatabase.SORT_BY_ASC)
			cursor = database.fetchAllEntryTypes("ASC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_DESC)
			cursor = database.fetchAllEntryTypes("DESC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_POPULAR)
			cursor = database.fetchAllEntryTypesByPopularity();
		
		int count = 0;
		ContentValues values = new ContentValues();
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
			count++;
			values.put(EntryType.SORT_ORDER, count);
			database.saveEntryType(cursor.getLong(cursor.getColumnIndex(EntryType._ID)), values);
		}
		
		reloadCursor();
	}
}