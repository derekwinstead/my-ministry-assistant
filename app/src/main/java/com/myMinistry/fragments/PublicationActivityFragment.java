package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.myMinistry.R;
import com.myMinistry.adapters.TimeEntryAdapter;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

public class PublicationActivityFragment extends ListFragment {
	public static String ARG_PUBLICATION_ID = "publication_id";

	private boolean is_dual_pane = false;

	private FragmentManager fm;

	static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
	private long publicationId = CREATE_ID;

	private MinistryService database;
	private Cursor activity;
	private TimeEntryAdapter adapter;

	public PublicationActivityFragment newInstance() {
		return new PublicationActivityFragment();
	}

	public PublicationActivityFragment newInstance(long _literatureID) {
		PublicationActivityFragment f = new PublicationActivityFragment();
		Bundle args = new Bundle();
		args.putLong(ARG_PUBLICATION_ID, _literatureID);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.publication_activity, container, false);
		Bundle args = getArguments();
		if (args != null && args.containsKey(ARG_PUBLICATION_ID))
			setLiterature(args.getLong(ARG_PUBLICATION_ID));

		fm = getActivity().getSupportFragmentManager();

		adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), activity);
		setListAdapter(adapter);

		database = new MinistryService(getActivity().getApplicationContext());

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

		if (!is_dual_pane)
			getActivity().setTitle(R.string.publication_activity);

		if (!database.isOpen())
			database.openWritable();

		activity = database.fetchActivityForLiterature((int) publicationId);
		adapter.changeCursor(activity);
		database.close();
	}

	public void setLiterature(long _id) {
		publicationId = _id;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

		FragmentTransaction ft = fm.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

		Fragment frag = fm.findFragmentById(LAYOUT_ID);
		TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id);

		if (frag != null)
			ft.remove(frag);

		ft.add(LAYOUT_ID, f);
		ft.addToBackStack(null);

		ft.commit();
	}
}