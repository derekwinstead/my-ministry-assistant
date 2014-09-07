package com.myMinistry.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.ListView;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.model.TitleAndDateAdapter;
import com.myMinistry.provider.MinistryService;

public class HouseholdersFragment extends ListFragment {
	private boolean is_dual_pane = false;
	
	private Cursor householders;
	private MinistryService database;
	private TitleAndDateAdapter adapter;
	
	private FragmentManager fm;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	public HouseholdersFragment newInstance() {
		return new HouseholdersFragment();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.householders, container, false);
		
		view.findViewById(R.id.btn_add_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openEditor(HouseholderEditorFragment.CREATE_ID);
			}
		});
		
		return view;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
    	
		if(menu.findItem(R.id.householder_create) != null)
    		menu.findItem(R.id.householder_create).setVisible(!drawerOpen);
    	
    	super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.householders, menu);
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	database = new MinistryService(getActivity());
    	
    	setHasOptionsMenu(true);
    	
    	fm = getActivity().getSupportFragmentManager();
    	
    	database.openWritable();
    	householders = database.fetchAllHouseholdersWithActivityDates();
    	adapter = new TitleAndDateAdapter(getActivity().getApplicationContext(), householders, R.string.last_visited_on, false);
    	setListAdapter(adapter);
    	database.close();
    	
    	if (is_dual_pane) {
        	FragmentTransaction ft = fm.beginTransaction();
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        	
        	Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
    		HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(HouseholderEditorFragment.CREATE_ID);
        	
        	if(frag != null)
        		ft.remove(frag);
        	
        	ft.add(R.id.secondary_fragment_container, f);
        	
        	ft.commit();
    	}
	}
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	openEditor(id);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.householder_create:
				openEditor(HouseholderEditorFragment.CREATE_ID);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void updateHouseholderList() {
		database.openWritable();
		householders = database.fetchAllHouseholdersWithActivityDates();
		adapter.changeCursor(householders);
		adapter.resetInactiveFlag();
		adapter.notifyDataSetChanged();
		database.close();
    }
	
	public void openEditor(long id) {
		int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
		
		if (is_dual_pane) {
			if(fm.findFragmentById(LAYOUT_ID) instanceof HouseholderEditorFragment) {
				HouseholderEditorFragment fragment = (HouseholderEditorFragment) fm.findFragmentById(LAYOUT_ID);
				fragment.switchForm(id);
			}
			else {
				FragmentTransaction ft = fm.beginTransaction();
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				
				Fragment frag = fm.findFragmentById(LAYOUT_ID);
				HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(id);
				
				if(frag != null)
					ft.remove(frag);
	        	
	        	ft.add(LAYOUT_ID, f);
	        	ft.addToBackStack(null);
	        	
	        	ft.commit();
			}
    	}
		else {
			FragmentTransaction ft = fm.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			
			Fragment frag = fm.findFragmentById(LAYOUT_ID);
			HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(id);
			
			if(frag != null)
        		ft.remove(frag);
			
			ft.add(LAYOUT_ID, f);
        	ft.addToBackStack(null);
        	
        	ft.commit();
		}
	}
}