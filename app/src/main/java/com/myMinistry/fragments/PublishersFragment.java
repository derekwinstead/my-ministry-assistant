package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.myMinistry.R;
import com.myMinistry.adapters.TitleAndDateAdapterUpdated;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment.PublisherNewDialogFragmentListener;
import com.myMinistry.provider.MinistryService;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

public class PublishersFragment extends ListFragment {
	private MinistryService database;
	private TitleAndDateAdapterUpdated adapter;
	private FloatingActionButton fab;

	private FragmentManager fm;

	public PublishersFragment newInstance() {
		return new PublishersFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.publishers, container, false);

		fm = getActivity().getSupportFragmentManager();

		fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublisherNewDialogFragment f = PublisherNewDialogFragment.newInstance();
                f.setPositiveButton(new PublisherNewDialogFragmentListener() {
                    @Override
                    public void setPositiveButton(int _ID, String _name) {
                        updatePublisherList();
                    }
                });
                f.show(fm, PublisherNewDialogFragment.class.getName());
            }
        });

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		database = new MinistryService(getActivity());

		database.openWritable();
        Cursor publishers = database.fetchAllPublishersWithActivityDates();
		adapter = new TitleAndDateAdapterUpdated(getActivity().getApplicationContext(), publishers, R.string.last_active_on);
		setListAdapter(adapter);
		database.close();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		openEditor(id);
	}

	public void updatePublisherList() {
		database.openWritable();
		adapter.loadNewData(database.fetchAllPublishersWithActivityDates());
		database.close();
	}

	public void openEditor(long id) {
		int LAYOUT_ID = R.id.primary_fragment_container;

		Fragment frag = fm.findFragmentById(LAYOUT_ID);
		PublisherEditorFragment f = new PublisherEditorFragment().newInstance(id);

		FragmentTransaction ft = fm.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

		if (frag != null)
			ft.remove(frag);

		ft.add(LAYOUT_ID, f);
		ft.addToBackStack(null);

		ft.commit();
	}
}