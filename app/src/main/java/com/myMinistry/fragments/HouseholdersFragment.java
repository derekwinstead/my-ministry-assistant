package com.myMinistry.fragments;

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
import com.myMinistry.adapters.TitleAndDateAdapterUpdated;
import com.myMinistry.provider.MinistryService;

public class HouseholdersFragment extends ListFragment {
	private boolean is_dual_pane = false;

	private Cursor cursor;
	private MinistryService database;
	private TitleAndDateAdapterUpdated adapter;
	private FloatingActionButton fab;

	private FragmentManager fm;

	public HouseholdersFragment newInstance() {
		return new HouseholdersFragment();
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

		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

		database = new MinistryService(getActivity());

		if (is_dual_pane) {
			fab.setVisibility(View.GONE);
		} else {
			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openEditor(HouseholderEditorFragment.CREATE_ID);
				}
			});
		}

		loadCursor();

		adapter = new TitleAndDateAdapterUpdated(getActivity().getApplicationContext(), cursor, R.string.last_visited_on);
		setListAdapter(adapter);
		database.close();

		if (is_dual_pane) {
			HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(HouseholderEditorFragment.CREATE_ID);
			FragmentTransaction transaction = fm.beginTransaction();
			transaction.replace(R.id.secondary_fragment_container, f, "secondary");
			transaction.commit();
		}
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
		int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

		if (is_dual_pane) {
			if (fm.findFragmentById(LAYOUT_ID) instanceof HouseholderEditorFragment) {
				HouseholderEditorFragment fragment = (HouseholderEditorFragment) fm.findFragmentById(LAYOUT_ID);
				fragment.switchForm(id);
			} else {
                HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(id);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.secondary_fragment_container, f, "secondary");
                transaction.commit();
			}
		} else {
            HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(id);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.primary_fragment_container, f, "main");
            transaction.commit();
		}
	}

	private void loadCursor() {
		if (!database.isOpen())
			database.openWritable();
		cursor = database.fetchAllHouseholdersWithActivityDates();
	}
}