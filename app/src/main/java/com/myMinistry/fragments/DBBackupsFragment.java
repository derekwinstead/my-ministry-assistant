package com.myMinistry.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DBBackupsFragment extends Fragment {
	private boolean is_dual_pane = false;
	
	private FragmentManager fm;
	
	public DBBackupsFragment newInstance() {
		return new DBBackupsFragment();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.db_backups, container, false);
		
		fm = getActivity().getSupportFragmentManager();
		
		view.findViewById(R.id.create_bu).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-aaa", Locale.getDefault());
				Calendar now = Calendar.getInstance();
				String date = dateFormatter.format(now.getTime()).toString();
				File intDB = getActivity().getApplicationContext().getDatabasePath(MinistryDatabase.DATABASE_NAME);
				File extDB = FileUtils.getExternalDBFile(getActivity().getApplicationContext(), date + ".db");
				
				try {
					if(extDB != null) {
						if(!extDB.exists())
							extDB.createNewFile();
						
						FileUtils.copyFile(intDB, extDB);
						
						if(is_dual_pane) {
							Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
							
							if(frag instanceof DBBackupsListFragment) {
								DBBackupsListFragment f = (DBBackupsListFragment) fm.findFragmentById(R.id.secondary_fragment_container);
					        	f.reloadAdapter();
							}
							else {
					    		DBBackupsListFragment f = new DBBackupsListFragment().newInstance();
					        	FragmentTransaction ft = fm.beginTransaction();
					        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					        	
					        	if(frag != null)
					        		ft.remove(frag);
					        	
					        	ft.add(R.id.secondary_fragment_container, f);
					        	ft.addToBackStack(null);
					        	
					        	ft.commit();
							}
						}
						
						Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_export_text), Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_export_text_error), Toast.LENGTH_SHORT).show();
				}
			}
		});
        
		view.findViewById(R.id.cleanup_bu).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				int FLAG = Helper.clearBackups(getActivity().getApplicationContext());
				if(FLAG == 1)
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cleaned_backups), Toast.LENGTH_SHORT).show();
				else if(FLAG == 2)
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cleaned_backups_only_one), Toast.LENGTH_SHORT).show();
				else if(FLAG == 0)
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cleaned_backups_error), Toast.LENGTH_SHORT).show();
				
				if(is_dual_pane) {
					Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
					
					if(frag instanceof DBBackupsListFragment) {
						DBBackupsListFragment f = (DBBackupsListFragment) fm.findFragmentById(R.id.secondary_fragment_container);
			        	f.reloadAdapter();
					}
					else {
			    		DBBackupsListFragment f = new DBBackupsListFragment().newInstance();
			        	FragmentTransaction ft = fm.beginTransaction();
			        	
			        	if(frag != null)
			        		ft.remove(frag);
			        	
			        	ft.add(R.id.secondary_fragment_container, f);
			        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			        	ft.addToBackStack(null);
			        	
			        	ft.commit();
					}
				}
		}});
		
        return view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	if (is_dual_pane) {
    		Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
    		DBBackupsListFragment f = new DBBackupsListFragment().newInstance();
        	FragmentTransaction ft = fm.beginTransaction();
        	
        	if(frag != null)
        		ft.remove(frag);
        	
        	ft.add(R.id.secondary_fragment_container, f);
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        	ft.addToBackStack(null);
        	
        	ft.commit();
    	}
	}
}