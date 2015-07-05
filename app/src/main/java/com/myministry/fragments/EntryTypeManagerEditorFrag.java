package com.myministry.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myministry.FragmentActivityStatus;
import com.myministry.R;
import com.myministry.provider.MinistryContract.EntryType;
import com.myministry.provider.MinistryDatabase;
import com.myministry.provider.MinistryService;
import com.myministry.util.PrefUtils;
import com.squareup.phrase.Phrase;

public class EntryTypeManagerEditorFrag extends Fragment {
	public static final String ARG_ENTRY_TYPE_ID = "entry_type_id";
	
	private boolean is_dual_pane = false;
	
	private EditText et_name;
	private CheckBox cb_is_active;
	private TextView tv_note;
	
	private long resID = 0;
	
	private MinistryService database;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	public EntryTypeManagerEditorFrag newInstance() {
    	return new EntryTypeManagerEditorFrag();
    }
	
	public EntryTypeManagerEditorFrag newInstance(long id) {
    	EntryTypeManagerEditorFrag f = new EntryTypeManagerEditorFrag();
        Bundle args = new Bundle();
        args.putLong(ARG_ENTRY_TYPE_ID, id);
        f.setArguments(args);
        return f;
    }
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
		if(menu.findItem(R.id.menu_save) != null)
    		menu.findItem(R.id.menu_save).setVisible(!drawerOpen);
    	if(menu.findItem(R.id.menu_cancel) != null)
    		menu.findItem(R.id.menu_cancel).setVisible(!drawerOpen);
    	
    	super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.save_cancel, menu);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.entry_type_manager_editor, container, false);
		Bundle args = getArguments();
		if(args != null && args.containsKey(ARG_ENTRY_TYPE_ID))
			setPublication(args.getLong(ARG_ENTRY_TYPE_ID));
		
		setHasOptionsMenu(true);
		
		et_name = (EditText) root.findViewById(R.id.et_name);
		cb_is_active = (CheckBox) root.findViewById(R.id.cb_is_active);
		tv_note = (TextView) root.findViewById(R.id.tv_note);
		
		database = new MinistryService(getActivity().getApplicationContext());
		
    	return root;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	fillForm();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		
		switch (item.getItemId()) {
			case R.id.menu_save:
				if(et_name.getText().toString().trim().length() > 0) {
    				ContentValues values = new ContentValues();
    				values.put(EntryType.NAME, et_name.getText().toString().trim());
					values.put(EntryType.ACTIVE, (cb_is_active.isChecked()) ? 1 : 0);
					
					if(database.isOpen())
						database.close();
					
					database.openWritable();
					
					if(resID > 0) {
						if(database.saveEntryType(resID, values) > 0) {
							Toast.makeText(getActivity()
									, Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_problem_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
					}
					else {
						if(database.createEntryType(values) > 0) {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_problem_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
					}
					database.close();
					
					if(is_dual_pane) {
						EntryTypeManagerFrag f = (EntryTypeManagerFrag) fm.findFragmentById(R.id.primary_fragment_container);
						f.sortList(PrefUtils.getEntryTypeSort(getActivity()));
						f.reloadCursor();
					}
					else {
						Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
						EntryTypeManagerFrag f = new EntryTypeManagerFrag().newInstance();
						
						if(frag != null)
							ft.remove(frag);
						
						ft.add(R.id.primary_fragment_container, f);
						ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
						ft.addToBackStack(null);
						
			        	ft.commit();
					}
    			}
    			else {
    				et_name.setError(getActivity().getApplicationContext().getString(R.string.toast_provide_name));
    				et_name.setFocusable(true);
    				et_name.requestFocus();
    			}
				
				return true;
			case R.id.menu_cancel:
				if(is_dual_pane)
					switchForm(MinistryDatabase.CREATE_ID);
				else {
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
					EntryTypeManagerFrag f = new EntryTypeManagerFrag().newInstance();
					
					if(frag != null)
						ft.remove(frag);
					
					ft.add(R.id.primary_fragment_container, f);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.addToBackStack(null);
					
		        	ft.commit();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void setPublication(long _id) {
		resID = _id;
    }
    
    public void switchForm(long _id) {
    	setPublication(_id);
    	fillForm();
    }
    
    public void fillForm() {
    	et_name.setError(null);
    	database.openWritable();
    	Cursor cursor = database.fetchEntryType((int)resID);
    	if(cursor.moveToFirst()) {
    		et_name.setText(cursor.getString(cursor.getColumnIndex(EntryType.NAME)));
    		cb_is_active.setChecked((cursor.getInt(cursor.getColumnIndex(EntryType.ACTIVE)) == 1) ? true : false);
    		if((int)resID == MinistryDatabase.ID_ROLLOVER) {
    			tv_note.setVisibility(View.VISIBLE);
    			cb_is_active.setEnabled(false);	
    		} else {
    			tv_note.setVisibility(View.GONE);
    			cb_is_active.setEnabled(true);
    		}
    	}
    	else {
    		et_name.setText("");
    		cb_is_active.setChecked(true);
    		tv_note.setVisibility(View.GONE);
    	}
    	cursor.close();
    	database.close();
    }
}