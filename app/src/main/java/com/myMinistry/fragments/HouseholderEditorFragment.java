package com.myMinistry.fragments;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

import java.util.Locale;

public class HouseholderEditorFragment extends Fragment {
    public static final String ARG_HOUSEHOLDER_ID = "householder_id";

    private boolean is_dual_pane = false;

    private EditText et_name, et_address, et_phone_mobile, et_phone_home, et_phone_work, et_phone_other;
    private CheckBox cb_is_active;
    private TextView view_activity;

    static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
    private long householderID = CREATE_ID;

    private MinistryService database;

    private FragmentManager fm;
    private FloatingActionButton fab;

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (householderID == CREATE_ID)
            inflater.inflate(R.menu.save, menu);
        else
            inflater.inflate(R.menu.save_discard, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.householder_editor, container, false);
        Bundle args = getArguments();
        if (args != null)
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
        view_activity = (TextView) root.findViewById(R.id.view_activity);
        fab = (FloatingActionButton) root.findViewById(R.id.fab);

        view_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HouseholderActivityFragment newFragment = new HouseholderActivityFragment().newInstance(householderID);
                Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
                FragmentTransaction transaction = fm.beginTransaction();

                if (replaceFrag != null)
                    transaction.remove(replaceFrag);

                transaction.add(R.id.primary_fragment_container, newFragment);
                transaction.commit();
            }
        });

        database = new MinistryService(getActivity().getApplicationContext());

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        if (!is_dual_pane) {
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchForm(CREATE_ID);
            }
        });

        if (!is_dual_pane)
            getActivity().setTitle(R.string.title_householder_edit);

        fillForm();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                if (et_name.getText().toString().trim().length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    if (householderID > 0) {
                        if (database.saveHouseholder(householderID, values) > 0) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_with_space)
                                            .put("name", et_name.getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_problem_with_space)
                                            .put("name", et_name.getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (database.createHouseholder(values) > 0) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_with_space)
                                            .put("name", et_name.getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_problem_with_space)
                                            .put("name", et_name.getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                    database.close();

                    if (is_dual_pane) {
                        HouseholdersFragment f = (HouseholdersFragment) fm.findFragmentById(R.id.primary_fragment_container);
                        f.updateHouseholderList();
                    } else {
                        HouseholdersFragment newFragment = new HouseholdersFragment().newInstance();
                        Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
                        FragmentTransaction transaction = fm.beginTransaction();

                        if (replaceFrag != null) {
                            transaction.remove(replaceFrag);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        }

                        transaction.add(R.id.primary_fragment_container, newFragment);
                        transaction.commit();
                    }
                } else {
                    et_name.setError(getActivity().getApplicationContext().getString(R.string.toast_provide_name));
                    et_name.setFocusable(true);
                    et_name.requestFocus();
                }

                return true;
			/*
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
			*/
            case R.id.menu_discard:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                database.openWritable();
                                database.deleteHouseholderByID((int) householderID);
                                database.close();

                                Toast.makeText(getActivity()
                                        , Phrase.from(getActivity().getApplicationContext(), R.string.toast_deleted_with_space)
                                                .put("name", et_name.getText().toString().trim())
                                                .format()
                                        , Toast.LENGTH_SHORT).show();

                                if (is_dual_pane) {
                                    HouseholdersFragment f = (HouseholdersFragment) fm.findFragmentById(R.id.primary_fragment_container);
                                    f.updateHouseholderList();
                                    switchForm(CREATE_ID);
                                } else {
                                    HouseholdersFragment newFragment = new HouseholdersFragment().newInstance();
                                    Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
                                    FragmentTransaction transaction = fm.beginTransaction();

                                    if (replaceFrag != null) {
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
                LinearLayout layout = new LinearLayout(getContext());
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                //layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(parms);

                TextView tv = new TextView(getContext());
                tv.setText(R.string.confirm_deletion_message_householders);
                tv.setPadding(Helper.dipsToPix(getContext(), 25), Helper.dipsToPix(getContext(), 25), Helper.dipsToPix(getContext(), 25), Helper.dipsToPix(getContext(), 25));
                //tv.setPadding(40, 40, 40, 40);
                //tv.setGravity(Gravity.CENTER);
                //tv.setTextSize(20);

                LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                //tv1Params.bottomMargin = 5;
                layout.addView(tv, tv1Params);


                builder.setTitle(R.string.confirm_deletion)
                        .setView(layout)
                        //.setMessage(R.string.confirm_deletaion_message_householders)
                        .setPositiveButton(R.string.menu_delete, dialogClickListener)
                        .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                        .show();





                /*

                LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);

        TextView tv = new TextView(this);
        tv.setText("Text View title");
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        EditText et = new EditText(this);
        etStr = et.getText().toString();
        TextView tv1 = new TextView(this);
        tv1.setText("Input Student ID");

        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(tv1,tv1Params);
        layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle(title);
        // alertDialogBuilder.setMessage("Input Student ID");
        alertDialogBuilder.setCustomTitle(tv);

        if (isError)
            alertDialogBuilder.setIcon(R.drawable.icon_warning);
        // alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        // Setting Negative "Cancel" Button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        // Setting Positive "OK" Button
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isError)
                    finish();
                else {
                      Intent intent = new Intent(ChangeDeviceActivity.this,
                      MyPageActivity.class); startActivity(intent);
                }
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        try {
            alertDialog.show();
        } catch (Exception e) {
            // WindowManager$BadTokenException will be caught and the app would
            // not display the 'Force Close' message
            e.printStackTrace();
        }

                 */

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
        if (householderID == CREATE_ID) {
            et_name.setText("");
            cb_is_active.setChecked(true);
            et_address.setText("");
            et_phone_mobile.setText("");
            et_phone_home.setText("");
            et_phone_work.setText("");
            et_phone_other.setText("");
            view_activity.setVisibility(View.GONE);

            if (is_dual_pane)
                fab.setVisibility(View.GONE);
        } else {
            database.openWritable();
            Cursor householder = database.fetchHouseholder((int) householderID);
            if (householder.moveToFirst()) {
                et_name.setText(householder.getString(householder.getColumnIndex(Householder.NAME)));
                cb_is_active.setChecked(householder.getInt(householder.getColumnIndex(Householder.ACTIVE)) == 1);
                et_address.setText(householder.getString(householder.getColumnIndex(Householder.ADDR)));
                et_phone_mobile.setText(householder.getString(householder.getColumnIndex(Householder.MOBILE_PHONE)));
                et_phone_home.setText(householder.getString(householder.getColumnIndex(Householder.HOME_PHONE)));
                et_phone_work.setText(householder.getString(householder.getColumnIndex(Householder.WORK_PHONE)));
                et_phone_other.setText(householder.getString(householder.getColumnIndex(Householder.OTHER_PHONE)));
            } else {
                et_name.setText("");
                cb_is_active.setChecked(true);
                et_address.setText("");
                et_phone_mobile.setText("");
                et_phone_home.setText("");
                et_phone_work.setText("");
                et_phone_other.setText("");
            }

            householder.close();
            database.close();

            if (is_dual_pane)
                fab.setVisibility(View.VISIBLE);
        }
    }
}