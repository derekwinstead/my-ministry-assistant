package com.myMinistry.fragments;

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
import android.widget.EditText;
import android.widget.Toast;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

public class PublicationManagerEditorFrag extends Fragment {
	public static final String ARG_PUBLICATION_TYPE_ID = "publication_type_id";
	
	private boolean is_dual_pane = false;
	
	private EditText et_name;
	
	private long resID = 0;
	
	private MinistryService database;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	public PublicationManagerEditorFrag newInstance() {
    	return new PublicationManagerEditorFrag();
    }
	
	public PublicationManagerEditorFrag newInstance(long id) {
    	PublicationManagerEditorFrag f = new PublicationManagerEditorFrag();
        Bundle args = new Bundle();
        args.putLong(ARG_PUBLICATION_TYPE_ID, id);
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
		View root = inflater.inflate(R.layout.publication_manager_editor, container, false);
		Bundle args = getArguments();
		if(args != null)
			setPublication(args.getLong(ARG_PUBLICATION_TYPE_ID));
		
		setHasOptionsMenu(true);
		
		et_name = (EditText) root.findViewById(R.id.et_name);
		
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
		
		switch (item.getItemId()) {
			case R.id.menu_save:
				if(et_name.getText().toString().trim().length() > 0) {
    				ContentValues values = new ContentValues();
    				values.put(LiteratureType.NAME, et_name.getText().toString().trim());
    				values.put(LiteratureType.ACTIVE, MinistryService.ACTIVE);
					
					if(database.isOpen())
						database.close();
					
					database.openWritable();
					
					if(resID > 0) {
						if(database.savePublicationType(resID, values) > 0) {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_with_space)
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
					database.close();
					
					if(is_dual_pane) {
						PublicationManagerFrag f = (PublicationManagerFrag) fm.findFragmentById(R.id.primary_fragment_container);
						f.reloadCursor();
					}
					else {
						Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
						PublicationManagerFrag f = new PublicationManagerFrag().newInstance();
			        	
						FragmentTransaction ft = fm.beginTransaction();
						ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
						
						if(frag != null)
							ft.remove(frag);
						
						ft.add(R.id.primary_fragment_container, f);
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
					PublicationManagerFrag f = new PublicationManagerFrag().newInstance();
		        	
					FragmentTransaction ft = fm.beginTransaction();
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        	
		        	if(frag != null)
		        		ft.remove(frag);
		        	
		        	ft.add(R.id.primary_fragment_container, f);
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
    	
    	Cursor cursor = database.fetchPublicationTypeByID(resID);
    	if(cursor.moveToFirst())
    		et_name.setText(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)));
    	else
    		et_name.setText("");
    	
    	cursor.close();
    	database.close();
    }
}