package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.myMinistry.R;
import com.myMinistry.adapters.TimeEntryAdapter;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.utils.AppConstants;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

public class PublicationActivityFragment extends ListFragment {
	private FragmentManager fm;

	static final long CREATE_ID = (long) AppConstants.CREATE_ID;
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
		args.putLong(AppConstants.ARG_PUBLICATION_ID, _literatureID);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.publication_activity, container, false);
		Bundle args = getArguments();
		if (args != null && args.containsKey(AppConstants.ARG_PUBLICATION_ID))
			setLiterature(args.getLong(AppConstants.ARG_PUBLICATION_ID));

		fm = getActivity().getSupportFragmentManager();

		adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), activity);
		setListAdapter(adapter);

		database = new MinistryService(getActivity().getApplicationContext());

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

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
		int LAYOUT_ID = R.id.contentFrame;

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