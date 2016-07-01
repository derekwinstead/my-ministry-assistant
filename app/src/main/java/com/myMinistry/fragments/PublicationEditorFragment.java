package com.myMinistry.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

public class PublicationEditorFragment extends Fragment {
	public static String ARG_PUBLICATION_ID = "publication_id";
	
	private boolean is_dual_pane = false;
	
	private EditText et_name;
	private Spinner s_publicationTypes;
	private CheckBox cb_is_active;
	private CheckBox cb_is_pair;
	
	private FragmentManager fm;
	
	static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
	private long publicationId = CREATE_ID;
	
	private long publicationTypeId = 0;
	private MinistryService database;
	private Cursor cursor;
	private NavDrawerMenuItemAdapter sadapter;
	
	public PublicationEditorFragment newInstance() {
    	return new PublicationEditorFragment();
    }
	
	public PublicationEditorFragment newInstance(long _literatureID) {
    	PublicationEditorFragment f = new PublicationEditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PUBLICATION_ID, _literatureID);
        f.setArguments(args);
        return f;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(publicationId == CREATE_ID)
			inflater.inflate(R.menu.save_cancel, menu);
		else
			inflater.inflate(R.menu.save_cancel_discard, menu);
	}
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.literature_editor, container, false);
		Bundle args = getArguments();
		if(args != null && args.containsKey(ARG_PUBLICATION_ID))
			setLiterature(args.getLong(ARG_PUBLICATION_ID));
		
		setHasOptionsMenu(true);
		
		fm = getActivity().getSupportFragmentManager();
		
		sadapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());
		
		et_name = (EditText) root.findViewById(R.id.et_name);
		s_publicationTypes = (Spinner) root.findViewById(R.id.literatureTypes);
    	cb_is_active = (CheckBox) root.findViewById(R.id.cb_is_active);
    	cb_is_pair = (CheckBox) root.findViewById(R.id.cb_is_pair);
		
    	database = new MinistryService(getActivity().getApplicationContext());
        database.openWritable();

		cursor = database.fetchActiveTypesOfLiterature();
		int default_position = 0;
        while(cursor.moveToNext()) {
			sadapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))), cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))));

			if(cursor.getInt(cursor.getColumnIndex(LiteratureType.DEFAULT)) == MinistryService.ACTIVE)
				default_position = cursor.getPosition();
		}
		
		sadapter.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
		s_publicationTypes.setAdapter(sadapter);
		s_publicationTypes.setSelection(default_position);
		s_publicationTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				publicationTypeId = sadapter.getItem(position).getID();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		database.close();

		root.findViewById(R.id.view_activity).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PublicationActivityFragment newFragment = new PublicationActivityFragment().newInstance(publicationId);
				Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
				FragmentTransaction transaction = fm.beginTransaction();

				if(replaceFrag != null)
					transaction.remove(replaceFrag);

				transaction.add(R.id.primary_fragment_container, newFragment);
				transaction.commit();
			}
		});
    	
    	return root;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	if(!is_dual_pane)
    		getActivity().setTitle(R.string.title_publication_edit);
    	
    	fillForm();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentTransaction ft = fm.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		
		switch (item.getItemId()) {
			case R.id.menu_save:
				if(et_name.getText().toString().trim().length() > 0) {
	    			ContentValues values = new ContentValues();
					values.put(Literature.NAME, et_name.getText().toString().trim());
					values.put(Literature.ACTIVE, (cb_is_active.isChecked()) ? 1 : 0);
					values.put(Literature.TYPE_OF_LIERATURE_ID, publicationTypeId);
					values.put(Literature.WEIGHT, (cb_is_pair.isChecked()) ? 2 : 1);
					
					database.openWritable();
					if(publicationId > 0) {
						if(database.saveLiterature(publicationId, values) > 0) {
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_with_space)
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
    					if(database.createLiterature(values) > 0) {
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
						PublicationFragment f = (PublicationFragment) fm.findFragmentById(R.id.primary_fragment_container);
						f.updateLiteratureList((int)publicationTypeId);
					}
					else {
						Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
						PublicationFragment f = new PublicationFragment().newInstance((int)publicationTypeId);
						
						if(frag != null)
							ft.remove(frag);
						
						ft.add(R.id.primary_fragment_container, f);
			        	ft.addToBackStack(null);
			        	
			        	ft.commit();
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
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
					PublicationFragment f = new PublicationFragment().newInstance((int)s_publicationTypes.getSelectedItemId());
					
					if(frag != null)
						ft.remove(frag);
					
					ft.add(R.id.primary_fragment_container, f);
		        	ft.addToBackStack(null);
		        	
		        	ft.commit();
				}
				return true;
			case R.id.menu_discard:
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				        	database.openWritable();
							database.deleteLiteratureByID((int)publicationId);
							database.close();
							
							Toast.makeText(getActivity()
									,Phrase.from(getActivity().getApplicationContext(), R.string.toast_deleted_with_space)
						    				.put("name", et_name.getText().toString().trim())
						    				.format()
									, Toast.LENGTH_SHORT).show();
							
							if(is_dual_pane) {
					        	PublicationFragment f = (PublicationFragment)fm.findFragmentById(R.id.primary_fragment_container);
					        	f.updateLiteratureList((int)publicationTypeId);
								switchForm(CREATE_ID);
							}
							else {
								Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
								PublicationFragment f = new PublicationFragment().newInstance((int)s_publicationTypes.getSelectedItemId());
								FragmentTransaction ft = fm.beginTransaction();
								
								if(frag != null)
									ft.remove(frag);
								
								ft.add(R.id.primary_fragment_container, f);
					        	ft.addToBackStack(null);
					        	
					        	ft.commit();
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
	
	public void setLiterature(long _id) {
		publicationId = _id;
    }
    
    public void switchForm(long _id) {
    	ActivityCompat.invalidateOptionsMenu(getActivity());
    	setLiterature(_id);
    	fillForm();
    }
    
    public void fillForm() {
    	et_name.setError(null);
    	if(publicationId == CREATE_ID) {
    		et_name.setText("");
    		cb_is_active.setChecked(true);
    		cb_is_pair.setChecked(false);
    	}
    	else {
	    	database.openWritable();
	    	Cursor literature = database.fetchLiteratureByID((int)publicationId);
	    	if(literature.moveToFirst()) {
	    		et_name.setText(literature.getString(literature.getColumnIndex(Literature.NAME)));
	    		cb_is_active.setChecked((literature.getInt(literature.getColumnIndex(Literature.ACTIVE)) == MinistryService.ACTIVE) ? true : false);
	    		cb_is_pair.setChecked((literature.getInt(literature.getColumnIndex(Literature.WEIGHT)) != 1) ? true : false);

	    		if(cursor.moveToFirst()) {
	    			int position = -1;
	    			do {
	    				position++;
	    				if(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)) == literature.getInt(literature.getColumnIndex(Literature.TYPE_OF_LIERATURE_ID))) {
	    					s_publicationTypes.setSelection(position);
	    					break;
	    				}
	    			} while(cursor.moveToNext());
	    		}
	    	}
	    	else {
	    		et_name.setText("");
	    		s_publicationTypes.setSelection(0);
	    		cb_is_active.setChecked(true);
	    		cb_is_pair.setChecked(false);
	    	}
	    	
	    	literature.close();
	    	database.close();
    	}
    }
}