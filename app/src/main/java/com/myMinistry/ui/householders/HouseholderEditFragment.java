package com.myMinistry.ui.householders;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.myMinistry.R;
import com.myMinistry.bean.Householder;
import com.myMinistry.db.HouseholderDAO;
import com.myMinistry.fragments.HouseholderActivityFragment;
import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.HelpUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HouseholderEditFragment extends Fragment {
    private CheckBox cb_is_active;
    private Button view_activity;
    private TextInputLayout nameWrapper, addressWrapper, mobileWrapper, homeWrapper, workWrapper, otherWrapper;

    private long householderID = (long) AppConstants.CREATE_ID;

    private HouseholderDAO householderDAO;
    private Householder householder;

    private FragmentManager fm;
    private FloatingActionButton fab;

    public HouseholderEditFragment newInstance() {
        return new HouseholderEditFragment();
    }

    public HouseholderEditFragment newInstance(long _householderID) {
        HouseholderEditFragment f = new HouseholderEditFragment();
        Bundle args = new Bundle();
        args.putLong(AppConstants.ARG_HOUSEHOLDER_ID, _householderID);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!householder.isNew()) {
            inflater.inflate(R.menu.discard, menu);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.householder_editor, container, false);

        householderDAO = new HouseholderDAO(getActivity().getApplicationContext());
        householder = new Householder();

        Bundle args = getArguments();
        if (args != null) {
            setHouseholder(args.getLong(AppConstants.ARG_HOUSEHOLDER_ID));
        }

        setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        nameWrapper = root.findViewById(R.id.nameWrapper);
        nameWrapper.setHint(getActivity().getString(R.string.form_name));

        addressWrapper = root.findViewById(R.id.addressWrapper);
        addressWrapper.setHint(getActivity().getString(R.string.form_address));

        mobileWrapper = root.findViewById(R.id.mobileWrapper);
        mobileWrapper.setHint(getActivity().getString(R.string.form_phone_mobile));

        homeWrapper = root.findViewById(R.id.homeWrapper);
        homeWrapper.setHint(getActivity().getString(R.string.form_phone_home));

        workWrapper = root.findViewById(R.id.workWrapper);
        workWrapper.setHint(getActivity().getString(R.string.form_phone_work));

        otherWrapper = root.findViewById(R.id.otherWrapper);
        otherWrapper.setHint(getActivity().getString(R.string.form_phone_other));

        cb_is_active = root.findViewById(R.id.cb_is_active);
        view_activity = root.findViewById(R.id.view_activity);
        fab = root.findViewById(R.id.fab);
        Button save = root.findViewById(R.id.save);
        Button cancel = root.findViewById(R.id.cancel);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchForm((long) AppConstants.CREATE_ID);
            }
        });

        view_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HouseholderActivityFragment f = new HouseholderActivityFragment().newInstance(householderID);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.contentFrame, f, "main");
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
                    householder.setIsActive(HelpUtils.booleanConversionsToInt(cb_is_active.isChecked()));

                    if (householder.isNew()) {
                        householderID = householderDAO.create(householder);
                        householder.setId(householderID);
                    } else {
                        householderDAO.update(householder);
                    }

                    HouseholdersListFragment f = new HouseholdersListFragment().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.contentFrame, f, "main");
                    transaction.commit();
                } else {
                    nameWrapper.setError(getActivity().getApplicationContext().getString(R.string.toast_provide_name));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HouseholdersListFragment f = new HouseholdersListFragment().newInstance();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.contentFrame, f, "main");
                transaction.commit();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
                                householderDAO.deleteHouseholder(householder);

                                HouseholdersListFragment f = new HouseholdersListFragment().newInstance();
                                FragmentTransaction transaction = fm.beginTransaction();
                                transaction.replace(R.id.contentFrame, f, "main");
                                transaction.commit();

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
        householder = householderDAO.getHouseholder((int) _id);
    }

    public void switchForm(long _id) {
        ActivityCompat.invalidateOptionsMenu(getActivity());
        setHouseholder(_id);
        fillForm();
    }

    public void fillForm() {
        nameWrapper.setErrorEnabled(false);
        nameWrapper.getEditText().setError(null);

        nameWrapper.getEditText().setText(householder.getName());
        cb_is_active.setChecked(householder.isActive());
        addressWrapper.getEditText().setText(householder.getAddress());
        mobileWrapper.getEditText().setText(householder.getPhoneMobile());
        homeWrapper.getEditText().setText(householder.getPhoneHome());
        workWrapper.getEditText().setText(householder.getPhoneWork());
        otherWrapper.getEditText().setText(householder.getPhoneOther());

        if (householder.isNew()) {
            view_activity.setVisibility(View.GONE);
        } else {
            view_activity.setVisibility(View.VISIBLE);
        }

        fab.setVisibility(View.GONE);
    }
}