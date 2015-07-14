package com.myMinistry.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.PrefUtils;
import com.squareup.phrase.Phrase;

public class EntryTypeManagerEditorFrag extends Fragment {
	public static final String ARG_ENTRY_TYPE_ID = "entry_type_id";
	
	private boolean is_dual_pane = false;
	
	private EditText et_name;
	private CheckBox cb_is_active;
	private TextView tv_note;
	private FloatingActionButton fab;
	
	private long resID = 0;
	
	private MinistryService database;
	
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
		fab = (FloatingActionButton) root.findViewById(R.id.fab);

		database = new MinistryService(getActivity().getApplicationContext());
		
    	return root;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchForm(0);
			}
		});
    	
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
            fab.setVisibility(View.VISIBLE);
    	}
    	else {
    		et_name.setText("");
    		cb_is_active.setChecked(true);
			cb_is_active.setEnabled(true);
    		tv_note.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
    	}
    	cursor.close();
    	database.close();
    }
}