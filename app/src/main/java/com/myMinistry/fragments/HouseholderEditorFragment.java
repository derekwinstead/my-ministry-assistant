package com.myMinistry.fragments;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.myMinistry.R;
import com.myMinistry.bean.Householder;
import com.myMinistry.db.HouseholderDAO;
import com.myMinistry.provider.MinistryDatabase;

public class HouseholderEditorFragment extends Fragment {
    public static final String ARG_HOUSEHOLDER_ID = "householder_id";

    private boolean is_dual_pane = false;

    private CheckBox cb_is_active;
    private Button view_activity;
    private TextInputLayout nameWrapper, addressWrapper, mobileWrapper, homeWrapper, workWrapper, otherWrapper;

    static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
    private long householderID = CREATE_ID;

    private HouseholderDAO householderDAO;
    private Householder householder;

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

        householderDAO = new HouseholderDAO(getActivity().getApplicationContext());

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
        Button save = (Button) root.findViewById(R.id.save);
        Button cancel = (Button) root.findViewById(R.id.cancel);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchForm(CREATE_ID);
            }
        });

        view_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HouseholderActivityFragment f = new HouseholderActivityFragment().newInstance(householderID);
                FragmentTransaction transaction = fm.beginTransaction();
                if (is_dual_pane) {
                    transaction.replace(R.id.secondary_fragment_container, f, "secondary");
                } else {
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                }
                transaction.commit();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                if (nameWrapper.getEditText().getText().toString().trim().length() > 0) {
                    nameWrapper.setErrorEnabled(false);

                    householder.setName(nameWrapper.getEditText().getText().toString().trim());
                    householder.setAddress(addressWrapper.getEditText().getText().toString().trim());
                    householder.setPhoneMobile(mobileWrapper.getEditText().getText().toString().trim());
                    householder.setPhoneHome(homeWrapper.getEditText().getText().toString().trim());
                    householder.setPhoneWork(workWrapper.getEditText().getText().toString().trim());
                    householder.setPhoneOther(otherWrapper.getEditText().getText().toString().trim());
                    householder.setIsActive(cb_is_active.isChecked());

                    if (householder.getId() == CREATE_ID) {
                        householderID = householderDAO.create(householder);
                        householder.setId(householderID);
                    } else {
                        householderDAO.update(householder);
                    }

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
                if (is_dual_pane) {
                    switchForm(CREATE_ID);
                } else {
                    HouseholdersFragment f = new HouseholdersFragment().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                }
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        if (!is_dual_pane) {
            getActivity().setTitle(R.string.title_householder_edit);
            fab.setVisibility(View.GONE);
        }

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
                                householderDAO.deleteHouseholder(householder);

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
                builder.setTitle(R.string.confirm_deletion)
                        .setMessage(R.string.confirm_deletion_message_householders)
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
        nameWrapper.setErrorEnabled(false);
        nameWrapper.getEditText().setError(null);

        householder = householderDAO.getHouseholder((int) householderID);

        nameWrapper.getEditText().setText(householder.getName());
        cb_is_active.setChecked(householder.isActive());
        addressWrapper.getEditText().setText(householder.getAddress());
        mobileWrapper.getEditText().setText(householder.getPhoneMobile());
        homeWrapper.getEditText().setText(householder.getPhoneHome());
        workWrapper.getEditText().setText(householder.getPhoneWork());
        otherWrapper.getEditText().setText(householder.getPhoneOther());

        if (householder.getId() == CREATE_ID) {
            view_activity.setVisibility(View.GONE);
        } else {
            view_activity.setVisibility(View.VISIBLE);
        }

        if (is_dual_pane) {
            if (householder.getId() == CREATE_ID) {
                fab.setVisibility(View.GONE);
            } else {
                fab.setVisibility(View.VISIBLE);
            }
        }
    }
}