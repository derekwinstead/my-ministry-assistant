package com.myMinistry.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.adapters.TimeEntryAdapter;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

public class PublisherEditorFragment extends ListFragment {
	public static final String ARG_PUBLISHER_ID = "publisher_id";
	
	private boolean is_dual_pane = false;
	
	private EditText et_name;
	private CheckBox cb_is_active;
	private TextView recent_activity_text;
	
	static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
	private long publisherId = CREATE_ID;
	
	private MinistryService database;
	private Cursor activity;
	private TimeEntryAdapter adapter;
	private FragmentManager fm;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	public PublisherEditorFragment newInstance() {
    	return new PublisherEditorFragment();
    }
	
	public PublisherEditorFragment newInstance(long _publisherID) {
    	PublisherEditorFragment f = new PublisherEditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PUBLISHER_ID, _publisherID);
        f.setArguments(args);
        return f;
    }
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
		if(menu.findItem(R.id.menu_discard) != null)
    		menu.findItem(R.id.menu_discard).setVisible(!drawerOpen);
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
		if(publisherId == CREATE_ID)
			inflater.inflate(R.menu.save_cancel, menu);
		else
			inflater.inflate(R.menu.save_cancel_discard, menu);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.publisher_editor, container, false);
		Bundle args = getArguments();
		if(args != null)
			setPublisher(args.getLong(ARG_PUBLISHER_ID));
		
		setHasOptionsMenu(true);
		
		fm = getActivity().getSupportFragmentManager();
		
		et_name = (EditText) root.findViewById(R.id.et_name);
		cb_is_active = (CheckBox) root.findViewById(R.id.cb_is_active);
    	recent_activity_text = (TextView) root.findViewById(R.id.recent_activity_text);
		
    	adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), activity);
    	setListAdapter(adapter);
		
		database = new MinistryService(getActivity().getApplicationContext());
		
    	return root;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	if(!is_dual_pane)
    		getActivity().setTitle(R.string.title_publisher_edit);
    	
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
    				values.put(Publisher.NAME, et_name.getText().toString().trim());
					values.put(Publisher.ACTIVE, (cb_is_active.isChecked()) ? 1 : 0);
					
					database.openWritable();
					if(publisherId > 0) {
						if(database.savePublisher(publisherId, values) > 0) {
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
        			else {
    					if(database.createPublisher(values) > 0) {
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
						PublishersFragment f = (PublishersFragment) fm.findFragmentById(R.id.primary_fragment_container);
						f.updatePublisherList();
					}
					else {
						Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
						PublishersFragment f = new PublishersFragment().newInstance();
						
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
					switchForm(CREATE_ID);
				else {
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
					PublishersFragment f = new PublishersFragment().newInstance();
					
					if(frag != null)
						ft.remove(frag);
					
					ft.add(R.id.primary_fragment_container, f);
					ft.addToBackStack(null);
					
		        	ft.commit();
				}
				return true;
			case R.id.menu_discard:
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				        	FragmentManager fm1 = getActivity().getSupportFragmentManager();
							database.openWritable();
							database.deletePublisherByID((int)publisherId);
							database.close();
							
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_deleted_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
							
							if(is_dual_pane) {
								PublishersFragment f = (PublishersFragment)fm1.findFragmentById(R.id.primary_fragment_container);
								f.updatePublisherList();
								switchForm(CREATE_ID);
							}
							else {
								FragmentTransaction ft = fm1.beginTransaction();
								Fragment frag = fm1.findFragmentById(R.id.primary_fragment_container);
								PublishersFragment f = new PublishersFragment().newInstance();
								
								if(frag != null)
									ft.remove(frag);
								
								ft.add(R.id.primary_fragment_container, f);
								ft.addToBackStack(null);
								
					        	ft.commit();
							}
							
							break;
				        }
				    }
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.confirm_deletion)
					.setPositiveButton(R.string.menu_delete, dialogClickListener)
					.setNegativeButton(R.string.menu_cancel, dialogClickListener)
					.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void setPublisher(long _id) {
		publisherId = _id;
    }
    
    public void switchForm(long _id) {
    	ActivityCompat.invalidateOptionsMenu(getActivity());
    	setPublisher(_id);
    	fillForm();
    }
    
    public void fillForm() {
    	et_name.setError(null);
    	if(publisherId == CREATE_ID) {
    		et_name.setText("");
    		cb_is_active.setChecked(true);
    		
    		recent_activity_text.setVisibility(View.GONE);
    		getListView().setVisibility(View.GONE);
    		getListView().getEmptyView().setVisibility(View.GONE);
    	}
    	else {
    		recent_activity_text.setVisibility(View.VISIBLE);
    		getListView().setVisibility(View.VISIBLE);
    		getListView().getEmptyView().setVisibility(View.VISIBLE);
    		
	    	database.openWritable();
	    	Cursor publisher = database.fetchPublisher((int)publisherId);
	    	if(publisher.moveToFirst()) {
	    		et_name.setText(publisher.getString(publisher.getColumnIndex(Publisher.NAME)));
	    		cb_is_active.setChecked((publisher.getInt(publisher.getColumnIndex(Publisher.ACTIVE)) == 1) ? true : false);
	    	}
	    	else {
	    		et_name.setText("");
	    		cb_is_active.setChecked(true);
	    	}
	    	publisher.close();
	    	activity = database.fetchActivityForPublisher((int) publisherId);
	    	adapter.changeCursor(activity);
	    	database.close();
	    }
    }
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
    	
    	FragmentTransaction ft = fm.beginTransaction();
    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	
    	Fragment frag = fm.findFragmentById(LAYOUT_ID);
    	TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id);
    	
    	if(frag != null)
    		ft.remove(frag);
    	
    	ft.add(LAYOUT_ID, f);
    	ft.addToBackStack(null);
    	
    	ft.commit();
	}
}