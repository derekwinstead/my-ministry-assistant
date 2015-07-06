package com.myMinistry.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.adapters.HouseholderRecentActivityAdapter;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

import java.util.Locale;

public class HouseholderEditorFragment extends ListFragment {
	public static final String ARG_HOUSEHOLDER_ID = "householder_id";
	
	private boolean is_dual_pane = false;
	
	/** Display vars */
	private EditText et_name, et_address, et_phone_mobile, et_phone_home, et_phone_work, et_phone_other;
	private CheckBox cb_is_active;
	private TextView recent_activity_text;
	/** Internal vars */
	static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
	private long householderID = CREATE_ID;
	
	private MinistryService database;
	private Cursor activity;
	
	private FragmentManager fm;
	
	private HouseholderRecentActivityAdapter adapter;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	public HouseholderEditorFragment newInstance() {
		return new HouseholderEditorFragment();
    }
	
	public HouseholderEditorFragment newInstance(long _householderID) {
		HouseholderEditorFragment f = new HouseholderEditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_HOUSEHOLDER_ID, _householderID);
        f.setArguments(args);
        return f;
    }
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
		if(menu.findItem(R.id.menu_discard) != null)
    		menu.findItem(R.id.menu_discard).setVisible(!drawerOpen);
    	if(menu.findItem(R.id.menu_save) != null)
    		menu.findItem(R.id.menu_save).setVisible(!drawerOpen);
    	if(menu.findItem(R.id.menu_cancel) != null)
    		menu.findItem(R.id.menu_cancel).setVisible(!drawerOpen);
    	
    	super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(householderID == CREATE_ID)
			inflater.inflate(R.menu.save_cancel, menu);
		else
			inflater.inflate(R.menu.save_cancel_discard, menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.householder_editor, container, false);
		Bundle args = getArguments();
		if(args != null)
			setHouseholder(args.getLong(ARG_HOUSEHOLDER_ID));
		
		setHasOptionsMenu(true);
		
		fm = getActivity().getSupportFragmentManager();
		
		et_name = (EditText) root.findViewById(R.id.et_name);
		et_address = (EditText) root.findViewById(R.id.et_address);
		et_phone_mobile = (EditText) root.findViewById(R.id.et_phone_mobile);
		et_phone_home = (EditText) root.findViewById(R.id.et_phone_home);
		et_phone_work = (EditText) root.findViewById(R.id.et_phone_work);
		et_phone_other = (EditText) root.findViewById(R.id.et_phone_other);
		cb_is_active = (CheckBox) root.findViewById(R.id.cb_is_active);
		recent_activity_text = (TextView) root.findViewById(R.id.recent_activity_text);
		
		adapter = new HouseholderRecentActivityAdapter(getActivity().getApplicationContext(), activity, (int)householderID);
		setListAdapter(adapter);
    	
        database = new MinistryService(getActivity().getApplicationContext());
        
        return root;
	}
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	if(!is_dual_pane)
    		getActivity().setTitle(R.string.title_householder_edit);
    	
    	fillForm();
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				if(et_name.getText().toString().trim().length() > 0) {
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						et_phone_mobile.setText(PhoneNumberUtils.formatNumber(et_phone_mobile.getText().toString(), Locale.getDefault().getISO3Country()));
						et_phone_home.setText(PhoneNumberUtils.formatNumber(et_phone_home.getText().toString(), Locale.getDefault().getISO3Country()));
						et_phone_work.setText(PhoneNumberUtils.formatNumber(et_phone_work.getText().toString(), Locale.getDefault().getISO3Country()));
						et_phone_other.setText(PhoneNumberUtils.formatNumber(et_phone_other.getText().toString(), Locale.getDefault().getISO3Country()));
					} else {
						et_phone_mobile.setText(PhoneNumberUtils.formatNumber(et_phone_mobile.getText().toString()));
						et_phone_home.setText(PhoneNumberUtils.formatNumber(et_phone_home.getText().toString()));
						et_phone_work.setText(PhoneNumberUtils.formatNumber(et_phone_work.getText().toString()));
						et_phone_other.setText(PhoneNumberUtils.formatNumber(et_phone_other.getText().toString()));
					}
					
					
					ContentValues values = new ContentValues();
    				values.put(Householder.NAME, et_name.getText().toString().trim());
					values.put(Householder.ACTIVE, (cb_is_active.isChecked()) ? 1 : 0);
    				values.put(Householder.ADDR, et_address.getText().toString().trim());
    				values.put(Householder.MOBILE_PHONE, et_phone_mobile.getText().toString().trim());
    				values.put(Householder.HOME_PHONE, et_phone_home.getText().toString().trim());
    				values.put(Householder.WORK_PHONE, et_phone_work.getText().toString().trim());
    				values.put(Householder.OTHER_PHONE, et_phone_other.getText().toString().trim());
    				
    				database.openWritable();
					if(householderID > 0) {
						if(database.saveHouseholder(householderID, values) > 0) {
							Toast.makeText(getActivity()
									, Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_problem_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
					}
        			else {
    					if(database.createHouseholder(values) > 0) {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
    					else {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_problem_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
						}
        			}
					database.close();

					if(is_dual_pane) {
						HouseholdersFragment f = (HouseholdersFragment) fm.findFragmentById(R.id.primary_fragment_container);
			        	f.updateHouseholderList();
					}
					else {
						HouseholdersFragment newFragment = new HouseholdersFragment().newInstance();
			        	Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
			        	FragmentTransaction transaction = fm.beginTransaction();
			        	
			        	if(replaceFrag != null) {
			        		transaction.remove(replaceFrag);
			        		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			        	}
			        	
			        	transaction.add(R.id.primary_fragment_container, newFragment);
			        	transaction.commit();
					}
    			}
    			else {
    				et_name.setError(getActivity().getApplicationContext().getString(R.string.toast_provide_name));
    				et_name.setFocusable(true);
    				et_name.requestFocus();
    			}
				
				return true;
			case R.id.menu_cancel:
				if(is_dual_pane)
					switchForm(CREATE_ID);
				else {
					HouseholdersFragment newFragment = new HouseholdersFragment().newInstance();
		        	Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
		        	FragmentTransaction transaction = fm.beginTransaction();
		        	
		        	if(replaceFrag != null) {
		        		transaction.remove(replaceFrag);
		        		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        	}
		        	
		        	transaction.add(R.id.primary_fragment_container, newFragment);
		        	transaction.commit();
				}
				return true;
			case R.id.menu_discard:
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:							
							database.openWritable();
							database.deleteHouseholderByID((int)householderID);
							database.close();
							
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_deleted_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
							
							if(is_dual_pane) {
								HouseholdersFragment f = (HouseholdersFragment) fm.findFragmentById(R.id.primary_fragment_container);
								f.updateHouseholderList();
								switchForm(CREATE_ID);
							}
							else {
								HouseholdersFragment newFragment = new HouseholdersFragment().newInstance();
					        	Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
					        	FragmentTransaction transaction = fm.beginTransaction();
					        	
					        	if(replaceFrag != null) {
					        		transaction.remove(replaceFrag);
					        		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					        	}
					        	
					        	transaction.add(R.id.primary_fragment_container, newFragment);
					        	transaction.commit();
							}
				        	
				        	
				        	break;
				        }
				    }
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.confirm_deletion)
					.setPositiveButton(R.string.menu_delete, dialogClickListener)
					.setNegativeButton(R.string.menu_cancel, dialogClickListener)
					.show();
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void setHouseholder(long _id) {
    	householderID = _id;
    }
    
    public void switchForm(long _id) {
    	ActivityCompat.invalidateOptionsMenu(getActivity());
    	setHouseholder(_id);
    	fillForm();
    }
    
    public void fillForm() {
    	et_name.setError(null);
    	if(householderID == CREATE_ID) {
    		et_name.setText("");
    		cb_is_active.setChecked(true);
    		et_address.setText("");
    		et_phone_mobile.setText("");
    		et_phone_home.setText("");
    		et_phone_work.setText("");
    		et_phone_other.setText("");
    		
    		recent_activity_text.setVisibility(View.GONE);
    		getListView().setVisibility(View.GONE);
    		getListView().getEmptyView().setVisibility(View.GONE);
    	}
    	else {
    		recent_activity_text.setVisibility(View.VISIBLE);
    		getListView().setVisibility(View.VISIBLE);
    		getListView().getEmptyView().setVisibility(View.VISIBLE);
    		
	    	database.openWritable();
	    	Cursor householder = database.fetchHouseholder((int) householderID);
	    	if(householder.moveToFirst()) {
	    		et_name.setText(householder.getString(householder.getColumnIndex(Householder.NAME)));
	    		cb_is_active.setChecked((householder.getInt(householder.getColumnIndex(Householder.ACTIVE)) == 1) ? true : false);
	    		et_address.setText(householder.getString(householder.getColumnIndex(Householder.ADDR)));
	    		et_phone_mobile.setText(householder.getString(householder.getColumnIndex(Householder.MOBILE_PHONE)));
	    		et_phone_home.setText(householder.getString(householder.getColumnIndex(Householder.HOME_PHONE)));
	    		et_phone_work.setText(householder.getString(householder.getColumnIndex(Householder.WORK_PHONE)));
	    		et_phone_other.setText(householder.getString(householder.getColumnIndex(Householder.OTHER_PHONE)));
	    	}
	    	else {
	    		et_name.setText("");
	    		cb_is_active.setChecked(true);
	    		et_address.setText("");
	    		et_phone_mobile.setText("");
	    		et_phone_home.setText("");
	    		et_phone_work.setText("");
	    		et_phone_other.setText("");
	    	}
	    	
	    	householder.close();
	    	activity = database.fetchActivityForHouseholder((int)householderID);
	    	adapter.setHouseholderID((int)householderID);
	    	adapter.changeCursor(activity);
	    	database.close();
    	}
    }
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
    	
    	FragmentTransaction ft = fm.beginTransaction();
    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	
    	Fragment frag = fm.findFragmentById(LAYOUT_ID);
    	TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id);
    	
    	if(frag != null)
    		ft.remove(frag);
    	
    	ft.add(LAYOUT_ID, f);
    	ft.addToBackStack(null);
    	
    	ft.commit();
	}
}