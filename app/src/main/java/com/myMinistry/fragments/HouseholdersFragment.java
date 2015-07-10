package com.myMinistry.fragments;

import android.content.ContentValues;
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

import com.myMinistry.R;
import com.myMinistry.adapters.TitleAndDateAdapter;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.PrefUtils;

public class HouseholdersFragment extends ListFragment {
	private boolean is_dual_pane = false;
	
	private Cursor cursor;
	private MinistryService database;
	private TitleAndDateAdapter adapter;
	
	private FragmentManager fm;
	
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
    	
    	loadCursor();
		//adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.li_bg_card_tv, cursor, new String[] {EntryType.NAME}, new int[] {android.R.id.text1});
		
    	adapter = new TitleAndDateAdapter(getActivity().getApplicationContext(), cursor, R.string.last_visited_on);
    	setListAdapter(adapter);
    	database.close();
    	
    	if (is_dual_pane) {
        	FragmentTransaction ft = fm.beginTransaction();
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        	
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
			case R.id.sort_alpha:
				PrefUtils.setHouseholderSort(getActivity(), MinistryDatabase.SORT_BY_ASC);
				sortList(MinistryDatabase.SORT_BY_ASC);
				return true;
			case R.id.sort_alpha_desc:
				PrefUtils.setHouseholderSort(getActivity(), MinistryDatabase.SORT_BY_DESC);
				sortList(MinistryDatabase.SORT_BY_DESC);
				return true;
			case R.id.sort_last_visit:
				PrefUtils.setHouseholderSort(getActivity(), MinistryDatabase.SORT_BY_DATE);
				sortList(MinistryDatabase.SORT_BY_DATE);
				return true;
			case R.id.sort_last_visit_desc:
				PrefUtils.setHouseholderSort(getActivity(), MinistryDatabase.SORT_BY_DATE_DESC);
				sortList(MinistryDatabase.SORT_BY_DATE_DESC);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void updateHouseholderList() {
		database.openWritable();
		adapter.loadNewData(database.fetchAllHouseholdersWithActivityDates());
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
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				
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
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			
			Fragment frag = fm.findFragmentById(LAYOUT_ID);
			HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(id);
			
			if(frag != null)
        		ft.remove(frag);
			
			ft.add(LAYOUT_ID, f);
        	ft.addToBackStack(null);
        	
        	ft.commit();
		}
	}
	
	public void sortList(int how_to_sort) {
		if(!database.isOpen())
			database.openWritable();
		
		if(how_to_sort == MinistryDatabase.SORT_BY_ASC)
			cursor = database.fetchAllHouseholders("ASC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_DESC)
			cursor = database.fetchAllHouseholders("DESC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_DATE)
			cursor = database.fetchAllHouseholdersWithActivityDates("DESC");
		else if(how_to_sort == MinistryDatabase.SORT_BY_DATE_DESC)
			cursor = database.fetchAllHouseholdersWithActivityDates("ASC");
		
		int count = 0;
		ContentValues values = new ContentValues();
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
			count++;
			values.put(Householder.SORT_ORDER, count);
			database.saveHouseholder(cursor.getLong(cursor.getColumnIndex(Householder._ID)), values);
		}
		
		reloadCursor();
	}
	
	private void loadCursor() {
		if(!database.isOpen())
			database.openWritable();
		cursor = database.fetchAllHouseholdersWithActivityDates();
	}
	
	public void reloadCursor() {
		loadCursor();
		adapter.loadNewData(cursor);
	}
}