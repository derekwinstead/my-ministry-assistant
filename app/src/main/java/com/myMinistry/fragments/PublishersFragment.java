package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.myMinistry.R;
import com.myMinistry.adapters.TitleAndDateAdapterUpdated;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment.PublisherNewDialogFragmentListener;
import com.myMinistry.provider.MinistryService;

public class PublishersFragment extends ListFragment {
	private boolean is_dual_pane = false;

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

		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

		if (is_dual_pane) {
			fab.setVisibility(View.GONE);
		}

		database = new MinistryService(getActivity());

		database.openWritable();
        Cursor publishers = database.fetchAllPublishersWithActivityDates();
		adapter = new TitleAndDateAdapterUpdated(getActivity().getApplicationContext(), publishers, R.string.last_active_on);
		setListAdapter(adapter);
		database.close();

		if (is_dual_pane) {
			Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
			PublisherEditorFragment f = new PublisherEditorFragment().newInstance(PublisherEditorFragment.CREATE_ID);

			FragmentTransaction ft = fm.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

			if (frag != null)
				ft.remove(frag);

			ft.add(R.id.secondary_fragment_container, f);

			ft.commit();
		}
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
		int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

		if (is_dual_pane) {
			if (fm.findFragmentById(LAYOUT_ID) instanceof PublisherEditorFragment) {
				PublisherEditorFragment fragment = (PublisherEditorFragment) fm.findFragmentById(LAYOUT_ID);
				fragment.switchForm(id);
			} else {
				FragmentTransaction ft = fm.beginTransaction();
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

				Fragment frag = fm.findFragmentById(LAYOUT_ID);
				PublisherEditorFragment f = new PublisherEditorFragment().newInstance(id);

				if (frag != null)
					ft.remove(frag);

				ft.add(LAYOUT_ID, f);
				ft.addToBackStack(null);

				ft.commit();
			}
		} else {
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
}