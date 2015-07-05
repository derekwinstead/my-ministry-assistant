package com.myministry.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.myministry.FragmentActivityStatus;
import com.myministry.Helper;
import com.myministry.R;
import com.myministry.adapters.ItemAdapter;
import com.myministry.model.NavDrawerMenuItem;
import com.myministry.provider.MinistryDatabase;
import com.myministry.provider.MinistryService;
import com.myministry.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class DBBackupsListFragment extends ListFragment {
	private String[] fileList;
	private ItemAdapter adapter;
	
	private final int REF_RESTORE = 0;
	private final int REF_EMAIL = 1;
	private final int REF_DELETE = 2;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	public DBBackupsListFragment newInstance() {
		return new DBBackupsListFragment();
    }
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
		if(menu.findItem(R.id.create_bu) != null)
    		menu.findItem(R.id.create_bu).setVisible(!drawerOpen);
    	if(menu.findItem(R.id.cleanup_bu) != null)
    		menu.findItem(R.id.cleanup_bu).setVisible(!drawerOpen);
    	
    	super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.db_backups, menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.db_backups_list, container, false);
		
		setHasOptionsMenu(true);
        
		view.findViewById(R.id.tv_add_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createBackup();
			}
		});
        return view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	adapter = new ItemAdapter(getActivity().getApplicationContext());
    	loadAdapter();
    	setListAdapter(adapter);
	}
    
    @Override
	public void onListItemClick(ListView l, View v, final int position, long id) {
    	final File file = FileUtils.getExternalDBFile(getActivity(), fileList[position].toString());
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	builder.setTitle(fileList[position].toString());
    	
    	builder.setItems(getResources().getStringArray(R.array.db_backups_list_item_options), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if(item == REF_RESTORE) {
					MinistryService database = new MinistryService(getActivity().getApplicationContext());
					database.openWritable();
					
					try {
						if(database.importDatabase(file, getActivity().getApplicationContext().getDatabasePath(MinistryDatabase.DATABASE_NAME))) {
							Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_import_text), Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_import_text_error), Toast.LENGTH_SHORT).show();
						}
					} catch (IOException e) {
						Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
				else if(item == REF_EMAIL) {
			    	Intent emailIntent = new Intent(Intent.ACTION_SEND);
			    	emailIntent.setType("application/image"); 
			    	emailIntent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getApplicationContext().getResources().getString(R.string.app_name) + ": " + getActivity().getApplicationContext().getResources().getString(R.string.pref_backup_title));
			    	emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath().toString()));
			    	startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.menu_share)));
				}
				else if(item == REF_DELETE) {
					file.delete();
					reloadAdapter();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
    
    private void createBackup() {
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
				
				reloadAdapter();
				
				Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_export_text), Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_export_text_error), Toast.LENGTH_SHORT).show();
		}
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.create_bu:
				createBackup();
				
				return true;
			case R.id.cleanup_bu:
				int FLAG = Helper.clearBackups(getActivity().getApplicationContext());
				if(FLAG == 1)
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cleaned_backups), Toast.LENGTH_SHORT).show();
				else if(FLAG == 2)
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cleaned_backups_only_one), Toast.LENGTH_SHORT).show();
				else if(FLAG == 0)
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cleaned_backups_error), Toast.LENGTH_SHORT).show();
				
				reloadAdapter();
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private String[] loadFileList() {
    	return FileUtils.getExternalDBFile(getActivity().getApplicationContext(), "").list();
    }
	
	private void loadAdapter() {
		fileList = loadFileList();
		
		if(fileList != null)
    		Arrays.sort(fileList);
		else
    		fileList = new String[0];
    	
		for(String filename : fileList)
			adapter.addItem(new NavDrawerMenuItem(filename, R.drawable.ic_drawer_db, 1));
	}
	
	public void reloadAdapter() {
		adapter.clear();
		loadAdapter();
		adapter.notifyDataSetChanged();
	}
}