package com.myMinistry.fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.ListItemAdapter;
import com.myMinistry.dialogfragments.TimePickerDialogFragment;
import com.myMinistry.model.ListItem;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.receivers.BootReceiver;
import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.FileUtils;
import com.myMinistry.utils.HelpUtils;
import com.myMinistry.utils.PrefUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

public class DBBackupsListFragment extends ListFragment {
    private String[] fileList;
    private ListItemAdapter adapter;

    private final int REF_RESTORE = 0;
    private final int REF_EMAIL = 1;
    private final int REF_DELETE = 2;

    private FragmentManager fm;
    private CoordinatorLayout coordinatorLayout;

    public DBBackupsListFragment newInstance() {
        return new DBBackupsListFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.db_backups, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.db_backups_list, container, false);

        setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
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

        getActivity().setTitle(R.string.navdrawer_item_backups);

        adapter = new ListItemAdapter(getActivity().getApplicationContext());
        loadAdapter();
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        final File file = FileUtils.getExternalDBFile(getActivity(), fileList[position]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(fileList[position]);

        builder.setItems(getResources().getStringArray(R.array.db_backups_list_item_options), new DialogInterface.OnClickListener() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            public void onClick(DialogInterface dialog, int item) {
                if (item == REF_RESTORE) {
                    MinistryService database = new MinistryService(getActivity().getApplicationContext());
                    database.openWritable();

                    try {
                        if (database.importDatabase(file, getActivity().getApplicationContext().getDatabasePath(AppConstants.DATABASE_NAME))) {
                            Snackbar.make(coordinatorLayout, R.string.snackbar_import_text, Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(coordinatorLayout, R.string.snackbar_import_text_error, Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                } else if (item == REF_EMAIL) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("application/image");
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getApplicationContext().getResources().getString(R.string.app_name) + ": " + getActivity().getApplicationContext().getResources().getString(R.string.pref_backup_title));
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
                    startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.menu_share)));
                } else if (item == REF_DELETE) {
                    file.delete();
                    Snackbar.make(coordinatorLayout, R.string.toast_deleted, Snackbar.LENGTH_SHORT).show();
                    reloadAdapter();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createBackup() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-aaa", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        String date = dateFormatter.format(now.getTime());
        File intDB = getActivity().getApplicationContext().getDatabasePath(AppConstants.DATABASE_NAME);
        File extDB = FileUtils.getExternalDBFile(getActivity().getApplicationContext(), date + ".db");

        try {
            if (extDB != null) {
                if (!extDB.exists())
                    extDB.createNewFile();

                FileUtils.copyFile(intDB, extDB);

                reloadAdapter();

                Snackbar.make(coordinatorLayout, R.string.snackbar_export_text, Snackbar.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cleanup_bu:
                int FLAG = Helper.clearBackups(getActivity().getApplicationContext());
                if (FLAG == 1)
                    Snackbar.make(coordinatorLayout, R.string.snackbar_cleaned_backups, Snackbar.LENGTH_SHORT).show();
                else if (FLAG == 2)
                    Snackbar.make(coordinatorLayout, R.string.snackbar_cleaned_backups_only_one, Snackbar.LENGTH_SHORT).show();
                else if (FLAG == 0)
                    Snackbar.make(coordinatorLayout, R.string.snackbar_cleaned_backups_error, Snackbar.LENGTH_SHORT).show();

                reloadAdapter();

                return true;
            case R.id.view_db_schedule:
                showDailyScheduleDialog();

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

        if (fileList != null)
            Arrays.sort(fileList);
        else
            fileList = new String[0];

        for (String filename : fileList)
            adapter.addItem(new ListItem(0, R.drawable.ic_action_database_themed, filename, ""));
    }

    public void reloadAdapter() {
        adapter.clear();
        loadAdapter();
        adapter.notifyDataSetChanged();
    }

    private void showDailyScheduleDialog() {
        final Calendar daily = Calendar.getInstance(Locale.getDefault());
        final Calendar weekly = Calendar.getInstance(Locale.getDefault());

        try {
            daily.setTime(DateFormat.getTimeFormat(getActivity().getApplicationContext()).parse(PrefUtils.getDBBackupDailyTime(getActivity())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            weekly.setTime(DateFormat.getTimeFormat(getActivity().getApplicationContext()).parse(PrefUtils.getDBBackupWeeklyTime(getActivity())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        weekly.set(Calendar.DAY_OF_WEEK, PrefUtils.getDBBackupWeeklyWeekday(getActivity()));

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(DBBackupsListFragment.this.getActivity()).inflate(R.layout.d_schedule_backups, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(DBBackupsListFragment.this.getActivity());
        final Switch cb_is_active = view.findViewById(R.id.cb_is_active);
        final Spinner s_weekday = view.findViewById(R.id.s_weekday);
        final TextView b_daily_time = view.findViewById(R.id.b_daily_time);
        final TextView b_weekly_time = view.findViewById(R.id.b_weekly_time);

        b_daily_time.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(daily.getTime()));
        b_weekly_time.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(weekly.getTime()));

        String[] weekdays = new DateFormatSymbols(Locale.getDefault()).getWeekdays();
        weekdays[0] = getActivity().getApplicationContext().getString(R.string.form_select_a_day);
        ArrayAdapter<?> adapter = new ArrayAdapter<Object>(getActivity().getApplicationContext(), R.layout.simple_spinner_item_holo_light, weekdays);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        s_weekday.setAdapter(adapter);
        s_weekday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weekly.set(Calendar.DAY_OF_WEEK, (int) id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        s_weekday.setSelection(weekly.get(Calendar.DAY_OF_WEEK));
        cb_is_active.setChecked(PrefUtils.shouldDBAutoBackup(getActivity()));

        b_daily_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialogFragment frag = TimePickerDialogFragment.newInstance(daily);
                frag.setTimePickerDialogFragmentListener(new TimePickerDialogFragment.TimePickerDialogFragmentListener() {
                    @Override
                    public void TimePickerDialogFragmentListenerSet(int hourOfDay, int minute) {
                        daily.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        daily.set(Calendar.MINUTE, minute);
                        daily.set(Calendar.SECOND, 0);
                        b_daily_time.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(daily.getTime()));
                    }
                });
                frag.show(fm, "TimePickerDialogFragment");
            }
        });

        b_weekly_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialogFragment frag = TimePickerDialogFragment.newInstance(weekly);
                frag.setTimePickerDialogFragmentListener(new TimePickerDialogFragment.TimePickerDialogFragmentListener() {
                    @Override
                    public void TimePickerDialogFragmentListenerSet(int hourOfDay, int minute) {
                        weekly.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        weekly.set(Calendar.MINUTE, minute);
                        weekly.set(Calendar.SECOND, 0);
                        b_weekly_time.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(weekly.getTime()));
                    }
                });
                frag.show(fm, "TimePickerDialogFragment");
            }
        });

        builder.setView(view);
        builder.setTitle(R.string.menu_schedule_backups);
        builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
        builder.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefUtils.setDBAutoBackup(getActivity(), cb_is_active.isChecked());
                // Daily
                PrefUtils.setDBBackupDailyTime(getActivity(), DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(daily.getTime()));
                // Weekly
                PrefUtils.setDBBackupWeeklyTime(getActivity(), DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(weekly.getTime()));
                PrefUtils.setDBBackupWeeklyWeekday(getActivity(), weekly.get(Calendar.DAY_OF_WEEK));

                if (cb_is_active.isChecked()) {
                    HelpUtils.setDailyAlarm(getActivity().getApplicationContext());
                    HelpUtils.setWeeklyAlarm(getActivity().getApplicationContext());

                    Snackbar.make(coordinatorLayout, R.string.snackbar_backups_scheduled, Snackbar.LENGTH_LONG).show();

                    enableBootReceiver();
                } else {
                    HelpUtils.disableDailyAlarm(getActivity().getApplicationContext());
                    HelpUtils.disableWeeklyAlarm(getActivity().getApplicationContext());

                    Snackbar.make(coordinatorLayout, R.string.snackbar_backups_disabled, Snackbar.LENGTH_LONG).show();

                    disableBootReceiver();
                }
            }
        });
        builder.show();
    }

    private void enableBootReceiver() {
        ComponentName receiver = new ComponentName(getActivity().getApplicationContext(), BootReceiver.class);
        PackageManager pm = getActivity().getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void disableBootReceiver() {
        ComponentName receiver = new ComponentName(getActivity().getApplicationContext(), BootReceiver.class);
        PackageManager pm = getActivity().getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}