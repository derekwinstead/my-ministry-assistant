package com.myMinistry.fragments;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
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
import android.widget.Button;
import android.widget.CheckBox;
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

    private CheckBox cb_is_active;
    private Button view_activity;
    private TextInputLayout nameWrapper, addressWrapper, mobileWrapper, homeWrapper, workWrapper, otherWrapper;
    private Button save, cancel;

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
        if (householderID != CREATE_ID)
            inflater.inflate(R.menu.discard, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.householder_editor, container, false);
        Bundle args = getArguments();
        if (args != null)
            setHouseholder(args.getLong(ARG_HOUSEHOLDER_ID));

        setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        nameWrapper = (TextInputLayout) root.findViewById(R.id.nameWrapper);
        nameWrapper.setHint(getActivity().getString(R.string.form_name));

        addressWrapper = (TextInputLayout) root.findViewById(R.id.addressWrapper);
        addressWrapper.setHint(getActivity().getString(R.string.form_address));

        mobileWrapper = (TextInputLayout) root.findViewById(R.id.mobileWrapper);
        mobileWrapper.setHint(getActivity().getString(R.string.form_phone_mobile));

        homeWrapper = (TextInputLayout) root.findViewById(R.id.homeWrapper);
        homeWrapper.setHint(getActivity().getString(R.string.form_phone_home));

        workWrapper = (TextInputLayout) root.findViewById(R.id.workWrapper);
        workWrapper.setHint(getActivity().getString(R.string.form_phone_work));

        otherWrapper = (TextInputLayout) root.findViewById(R.id.otherWrapper);
        otherWrapper.setHint(getActivity().getString(R.string.form_phone_other));

        cb_is_active = (CheckBox) root.findViewById(R.id.cb_is_active);
        view_activity = (Button) root.findViewById(R.id.view_activity);
        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        save = (Button) root.findViewById(R.id.save);
        cancel = (Button) root.findViewById(R.id.cancel);

        view_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int LAYOUT_ID = is_dual_pane ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

                HouseholderActivityFragment newFragment = new HouseholderActivityFragment().newInstance(householderID);
                Fragment replaceFrag = fm.findFragmentById(LAYOUT_ID);
                FragmentTransaction transaction = fm.beginTransaction();

                if (replaceFrag != null)
                    transaction.remove(replaceFrag);

                transaction.add(LAYOUT_ID, newFragment);
                transaction.commit();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameWrapper.getEditText().getText().toString().trim().length() > 0) {
                    nameWrapper.setErrorEnabled(false);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mobileWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(mobileWrapper.getEditText().getText().toString(), Locale.getDefault().getISO3Country()));
                        homeWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(homeWrapper.getEditText().getText().toString(), Locale.getDefault().getISO3Country()));
                        workWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(workWrapper.getEditText().getText().toString(), Locale.getDefault().getISO3Country()));
                        otherWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(otherWrapper.getEditText().getText().toString(), Locale.getDefault().getISO3Country()));
                    } else {
                        mobileWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(mobileWrapper.getEditText().getText().toString()));
                        homeWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(homeWrapper.getEditText().getText().toString()));
                        workWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(workWrapper.getEditText().getText().toString()));
                        otherWrapper.getEditText().setText(PhoneNumberUtils.formatNumber(otherWrapper.getEditText().getText().toString()));
                    }

                    ContentValues values = new ContentValues();
                    values.put(Householder.NAME, nameWrapper.getEditText().getText().toString().trim());
                    values.put(Householder.ACTIVE, cb_is_active.isChecked() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
                    values.put(Householder.ADDR, addressWrapper.getEditText().getText().toString().trim());
                    values.put(Householder.MOBILE_PHONE, mobileWrapper.getEditText().getText().toString().trim());
                    values.put(Householder.HOME_PHONE, homeWrapper.getEditText().getText().toString().trim());
                    values.put(Householder.WORK_PHONE, workWrapper.getEditText().getText().toString().trim());
                    values.put(Householder.OTHER_PHONE, otherWrapper.getEditText().getText().toString().trim());

                    database.openWritable();
                    if (householderID > 0) {
                        if (database.saveHouseholder(householderID, values) > 0) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_problem_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (database.createHouseholder(values) > 0) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_problem_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                    database.close();

                    if (is_dual_pane) {
                        HouseholdersFragment f = (HouseholdersFragment) fm.findFragmentById(R.id.primary_fragment_container);
                        f.updateHouseholderList();
                    } else {
                        HouseholdersFragment f = new HouseholdersFragment().newInstance();
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.replace(R.id.primary_fragment_container, f, "main");
                        transaction.commit();
                    }
                } else {
                    nameWrapper.setError(getActivity().getApplicationContext().getString(R.string.toast_provide_name));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HouseholdersFragment f = new HouseholdersFragment().newInstance();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.primary_fragment_container, f, "main");
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
                                                .put("name", nameWrapper.getEditText().getText().toString().trim())
                                                .format()
                                        , Toast.LENGTH_SHORT).show();

                                if (is_dual_pane) {
                                    HouseholdersFragment f = (HouseholdersFragment) fm.findFragmentById(R.id.primary_fragment_container);
                                    f.updateHouseholderList();
                                    switchForm(CREATE_ID);
                                } else {
                                    HouseholdersFragment f = new HouseholdersFragment().newInstance();
                                    FragmentTransaction transaction = fm.beginTransaction();
                                    transaction.replace(R.id.primary_fragment_container, f, "main");
                                    transaction.commit();
                                }

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LinearLayout layout = new LinearLayout(getContext());
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setLayoutParams(parms);

                TextView tv = new TextView(getContext());
                tv.setText(R.string.confirm_deletion_message_householders);
                tv.setPadding(Helper.dipsToPix(getContext(), 25), Helper.dipsToPix(getContext(), 25), Helper.dipsToPix(getContext(), 25), Helper.dipsToPix(getContext(), 25));

                LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.addView(tv, tv1Params);


                builder.setTitle(R.string.confirm_deletion)
                        .setView(layout)
                        //.setMessage(R.string.confirm_deletaion_message_householders)
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
        nameWrapper.getEditText().setError(null);
        if (householderID == CREATE_ID) {
            nameWrapper.getEditText().setText("");
            cb_is_active.setChecked(true);
            addressWrapper.getEditText().setText("");
            mobileWrapper.getEditText().setText("");
            homeWrapper.getEditText().setText("");
            workWrapper.getEditText().setText("");
            otherWrapper.getEditText().setText("");
            view_activity.setVisibility(View.GONE);

            if (is_dual_pane)
                fab.setVisibility(View.GONE);
        } else {
            view_activity.setVisibility(View.VISIBLE);

            database.openWritable();
            Cursor householder = database.fetchHouseholder((int) householderID);
            if (householder.moveToFirst()) {
                nameWrapper.getEditText().setText(householder.getString(householder.getColumnIndex(Householder.NAME)));
                cb_is_active.setChecked(householder.getInt(householder.getColumnIndex(Householder.ACTIVE)) == MinistryService.ACTIVE);
                addressWrapper.getEditText().setText(householder.getString(householder.getColumnIndex(Householder.ADDR)));
                mobileWrapper.getEditText().setText(householder.getString(householder.getColumnIndex(Householder.MOBILE_PHONE)));
                homeWrapper.getEditText().setText(householder.getString(householder.getColumnIndex(Householder.HOME_PHONE)));
                workWrapper.getEditText().setText(householder.getString(householder.getColumnIndex(Householder.WORK_PHONE)));
                otherWrapper.getEditText().setText(householder.getString(householder.getColumnIndex(Householder.OTHER_PHONE)));
            } else {
                nameWrapper.getEditText().setText("");
                cb_is_active.setChecked(true);
                addressWrapper.getEditText().setText("");
                mobileWrapper.getEditText().setText("");
                homeWrapper.getEditText().setText("");
                workWrapper.getEditText().setText("");
                otherWrapper.getEditText().setText("");
            }

            householder.close();
            database.close();

            if (is_dual_pane)
                fab.setVisibility(View.VISIBLE);
        }
    }
}