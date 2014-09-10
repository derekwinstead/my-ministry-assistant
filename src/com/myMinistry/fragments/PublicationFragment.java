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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.model.TitleAndDateAdapter;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.HelpUtils;
import com.myMinistry.util.PrefUtils;

public class PublicationFragment extends ListFragment {
    public static String ARG_PUBLICATION_ID = "publication_id";
    
    private boolean is_dual_pane = false;
    
	private Cursor literature;
	private MinistryService database;
	private Spinner myspinner;
	private TitleAndDateAdapter adapter;
	private Cursor cursor;
	private int literatureTypeId = 0;
	private FragmentManager fm;
	
	private NavDrawerMenuItemAdapter sadapter;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	public PublicationFragment newInstance() {
		return new PublicationFragment();
    }
	
	public PublicationFragment newInstance(int literatureTypeId) {
		PublicationFragment f = new PublicationFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PUBLICATION_ID, literatureTypeId);
		f.setArguments(args);
		return f;
    }
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
		if(menu.findItem(R.id.literature_create) != null)
    		menu.findItem(R.id.literature_create).setVisible(!drawerOpen);

        if(menu.findItem(R.id.sort_container) != null)
    		menu.findItem(R.id.sort_container).setVisible(!drawerOpen);
		
    	super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.literature, menu);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		
		View root = inflater.inflate(R.layout.literature, container, false);
		Bundle args = getArguments();
		
		if(args != null && args.containsKey(ARG_PUBLICATION_ID))
			literatureTypeId = args.getInt(ARG_PUBLICATION_ID);
		
		database = new MinistryService(getActivity());
		myspinner = (Spinner) root.findViewById(R.id.myspinner);
		
		fm = getActivity().getSupportFragmentManager();
		
		sadapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());
		
		
		
		database.openWritable();
		cursor = database.fetchActiveTypesOfLiterature();
		
		while(cursor.moveToNext())
			sadapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))), cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))));
		
		//sadapter.setDropDownViewResource(R.layout.li_spinner_item);
		myspinner.setAdapter(sadapter);
		myspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				updateLiteratureList(sadapter.getItem(position).getID());
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		database.close();
		
		if(literatureTypeId > 0) {
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
				if(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)) == literatureTypeId) {
					myspinner.setSelection(cursor.getPosition());
					break;
				}
			}
		}
		
		root.findViewById(R.id.btn_add_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openEditor(PublicationEditorFragment.CREATE_ID);
			}
		});
		
		return root;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	getActivity().setTitle(R.string.navdrawer_item_publications);
    	
    	database.openWritable();
    	
    	adapter = new TitleAndDateAdapter(getActivity().getApplicationContext(), literature, R.string.last_placed_on, false);
    	setListAdapter(adapter);
    	database.close();
    	
    	if (is_dual_pane) {
    		Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
    		PublicationEditorFragment f = new PublicationEditorFragment().newInstance();
        	
    		FragmentTransaction ft = fm.beginTransaction();
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			
			if(frag != null)
				ft.remove(frag);
        	
        	ft.add(R.id.secondary_fragment_container, f);
        	
        	ft.commit();
    	}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.literature_create:
				openEditor(PublicationEditorFragment.CREATE_ID);
				return true;
			case R.id.sort_alpha:
				PrefUtils.setPublicationSort(getActivity(), MinistryDatabase.SORT_BY_ASC);
				HelpUtils.sortPublications(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_ASC);
				updateLiteratureList(literatureTypeId);
				return true;
			case R.id.sort_alpha_desc:
				PrefUtils.setPublicationSort(getActivity(), MinistryDatabase.SORT_BY_DESC);
				HelpUtils.sortPublications(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_DESC);
				updateLiteratureList(literatureTypeId);
				return true;
			case R.id.sort_most_placed:
				PrefUtils.setPublicationSort(getActivity(), MinistryDatabase.SORT_BY_POPULAR);
				HelpUtils.sortPublications(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_POPULAR);
				updateLiteratureList(literatureTypeId);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void updateLiteratureList(int typeID) {
		if(literatureTypeId != typeID) {
			literatureTypeId = typeID;
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
				if(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)) == literatureTypeId) {
					myspinner.setSelection(cursor.getPosition());
					break;
				}
			}
		}
		
		database.openWritable();
		literature = database.fetchLiteratureByTypeWithActivityDates(literatureTypeId);
		adapter.changeCursor(literature);
		adapter.resetInactiveFlag();
		adapter.notifyDataSetChanged();
		database.close();
    }
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		openEditor(id);
    }
	
	public void openEditor(long id) {
		int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
		
		if (is_dual_pane) {
			if(fm.findFragmentById(LAYOUT_ID) instanceof PublicationEditorFragment) {
				PublicationEditorFragment fragment = (PublicationEditorFragment) fm.findFragmentById(LAYOUT_ID);
				fragment.switchForm(id);
			}
			else {
				FragmentTransaction ft = fm.beginTransaction();
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				
				Fragment frag = fm.findFragmentById(LAYOUT_ID);
				PublicationEditorFragment f = new PublicationEditorFragment().newInstance(id);
				
				if(frag != null)
					ft.remove(frag);
	        	
	        	ft.add(LAYOUT_ID, f);
	        	ft.addToBackStack(null);
	        	
	        	ft.commit();
			}
    	}
		else {
			Fragment frag = fm.findFragmentById(LAYOUT_ID);
			PublicationEditorFragment f = new PublicationEditorFragment().newInstance(id);
        	
			FragmentTransaction ft = fm.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			
			if(frag != null)
				ft.remove(frag);
			
			ft.add(LAYOUT_ID, f);
        	ft.addToBackStack(null);
        	
        	ft.commit();
		}
	}
}