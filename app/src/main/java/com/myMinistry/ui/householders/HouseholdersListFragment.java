package com.myMinistry.ui.householders;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.utils.AppConstants;

public class HouseholdersListFragment extends ListFragment {
	private Cursor cursor;
	private MinistryService database;
	private HouseholdersListAdapter adapter;
	private FloatingActionButton fab;

	private FragmentManager fm;

	public HouseholdersListFragment newInstance() {
		return new HouseholdersListFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.householders, container, false);

		fm = getActivity().getSupportFragmentManager();

		fab = view.findViewById(R.id.fab);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		database = new MinistryService(getActivity());

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openEditor(AppConstants.CREATE_ID);
			}
		});

		loadCursor();

		adapter = new HouseholdersListAdapter(getActivity().getApplicationContext(), cursor, R.string.last_visited_on);
		setListAdapter(adapter);
		database.close();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		openEditor(id);
	}

	public void updateHouseholderList() {
		database.openWritable();
		adapter.loadNewData(database.fetchAllHouseholdersWithActivityDates());
		database.close();
	}

	public void openEditor(long id) {
		HouseholderEditFragment f = new HouseholderEditFragment().newInstance(id);
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.primary_fragment_container, f, "main");
		transaction.commit();
	}

	private void loadCursor() {
		if (!database.isOpen())
			database.openWritable();
		cursor = database.fetchAllHouseholdersWithActivityDates();
	}
}