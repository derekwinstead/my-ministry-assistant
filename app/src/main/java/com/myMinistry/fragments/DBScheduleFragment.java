package com.myMinistry.fragments;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.myMinistry.R;
import com.myMinistry.dialogfragments.TimePickerDialogFragment;
import com.myMinistry.dialogfragments.TimePickerDialogFragment.TimePickerDialogFragmentListener;
import com.myMinistry.receivers.BootReceiver;
import com.myMinistry.util.HelpUtils;
import com.myMinistry.util.PrefUtils;
import com.squareup.phrase.Phrase;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class DBScheduleFragment extends Fragment {
	private FragmentManager fm;
	
	public final int SCHEDULE_DAILY = 0;
	public final int SCHEDULE_WEEKLY = 1;
	
	private Spinner s_weekday;
	private CheckBox cb_is_active_daily, cb_is_active_weekly;
	private TextView b_daily_time, b_weekly_time;
	private Calendar daily = Calendar.getInstance(Locale.getDefault());
	private Calendar weekly = Calendar.getInstance(Locale.getDefault());
	
	public DBScheduleFragment newInstance() {
		return new DBScheduleFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.db_schedule, menu);
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.scheduled_backups, container, false);

        setHasOptionsMenu(true);

		getActivity().setTitle(R.string.menu_schedule_backups);
		
		fm = getActivity().getSupportFragmentManager();
		
		s_weekday = (Spinner) root.findViewById(R.id.s_weekday);
		cb_is_active_daily = (CheckBox) root.findViewById(R.id.cb_is_active_daily);
		cb_is_active_weekly = (CheckBox) root.findViewById(R.id.cb_is_active_weekly);
		b_daily_time = (TextView) root.findViewById(R.id.b_daily_time);
		b_weekly_time = (TextView) root.findViewById(R.id.b_weekly_time);

		/*
		TextView t_daily_save = (TextView) root.findViewById(R.id.t_daily_save);
		TextView t_weekly_save = (TextView) root.findViewById(R.id.t_weekly_save);

		t_daily_save.setText(getActivity().getApplicationContext().getString(R.string.menu_save).toUpperCase(Locale.getDefault()));
		t_daily_save.setTextColor(getActivity().getApplicationContext().getResources().getColor(R.color.appbasetheme_color));
		t_daily_save.setTypeface(t_daily_save.getTypeface(), Typeface.BOLD);
		
		t_weekly_save.setText(getActivity().getApplicationContext().getString(R.string.menu_save).toUpperCase(Locale.getDefault()));
		t_weekly_save.setTextColor(getActivity().getApplicationContext().getResources().getColor(R.color.appbasetheme_color));
		t_weekly_save.setTypeface(t_weekly_save.getTypeface(), Typeface.BOLD);
		*/
		root.findViewById(R.id.t_daily_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PrefUtils.setDBBackupDaily(getActivity(), cb_is_active_daily.isChecked());
				PrefUtils.setDBBackupDailyTime(getActivity(), DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(daily.getTime()).toString());
				
				if(cb_is_active_daily.isChecked()) {
					HelpUtils.setDailyAlarm(getActivity().getApplicationContext());
					
			    	Toast.makeText(getActivity().getApplicationContext(),
			    			Phrase.from(getActivity().getApplicationContext(), R.string.toast_daily_backup_scheduled)
			    				.put("time", DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(daily.getTime()))
			    				.format()
			    			, Toast.LENGTH_LONG).show();
			    	
			    	enableBootReceiver();
				}
				else {
					HelpUtils.disableDailyAlarm(getActivity().getApplicationContext());
					
			    	Toast.makeText(getActivity().getApplicationContext(), getActivity().getResources().getString(R.string.toast_daily_backup_cancelled), Toast.LENGTH_LONG).show();
			    	
			    	if(PrefUtils.shouldDBBackupWeekly(getActivity())) {
			    		disableBootReceiver();
			    	}
				}
			}
		});
		
		root.findViewById(R.id.t_weekly_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PrefUtils.setDBBackupWeekly(getActivity(), cb_is_active_weekly.isChecked());
				PrefUtils.setDBBackupWeeklyTime(getActivity(), DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(weekly.getTime()));
				PrefUtils.setDBBackupWeeklyWeekday(getActivity(), weekly.get(Calendar.DAY_OF_WEEK));
				
				if(cb_is_active_weekly.isChecked()) {
		    		HelpUtils.setWeeklyAlarm(getActivity().getApplicationContext());

			    	Toast.makeText(getActivity().getApplicationContext(),
			    			Phrase.from(getActivity().getApplicationContext(), R.string.toast_weekly_backup_scheduled)
			    				.put("weekday", DateUtils.formatDateTime(getActivity().getApplicationContext(), weekly.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY))
			    				.put("time", DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(weekly.getTime()))
			    				.format()
			    			, Toast.LENGTH_LONG).show();
			    	
			    	enableBootReceiver();
				}
				else {
					HelpUtils.disableWeeklyAlarm(getActivity().getApplicationContext());
			    	Toast.makeText(getActivity().getApplicationContext(), getActivity().getResources().getString(R.string.toast_weekly_backup_cancelled), Toast.LENGTH_LONG).show();
			    	
			    	if(PrefUtils.shouldDBBackupDaily(getActivity())) {
			    		disableBootReceiver();
			    	}
				}
			}
		});
		
		b_daily_time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialogFragment frag = TimePickerDialogFragment.newInstance(daily);
				frag.setTimePickerDialogFragmentListener(new TimePickerDialogFragmentListener() {
					@Override
					public void TimePickerDialogFragmentListenerSet(int hourOfDay, int minute) {
						daily.set(Calendar.HOUR_OF_DAY, hourOfDay);
						daily.set(Calendar.MINUTE, minute);
						daily.set(Calendar.SECOND,0);
						updateDisplayTimes();
						
					}
				});
				frag.show(fm, "TimePickerDialogFragment");
			}
		});
		
		b_weekly_time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialogFragment frag = TimePickerDialogFragment.newInstance(weekly);
				frag.setTimePickerDialogFragmentListener(new TimePickerDialogFragmentListener() {
					@Override
					public void TimePickerDialogFragmentListenerSet(int hourOfDay, int minute) {
						weekly.set(Calendar.HOUR_OF_DAY, hourOfDay);
						weekly.set(Calendar.MINUTE, minute);
						weekly.set(Calendar.SECOND,0);
						updateDisplayTimes();
						
					}
				});
				frag.show(fm, "TimePickerDialogFragment");
			}
		});
		
		String[] weekdays = new DateFormatSymbols(Locale.getDefault()).getWeekdays();
		ArrayAdapter<?> adapter = new ArrayAdapter<Object>(getActivity().getApplicationContext(), R.layout.simple_spinner_item_holo_light, weekdays);
		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		s_weekday.setAdapter(adapter);
		s_weekday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				weekly.set(Calendar.DAY_OF_WEEK, (int)id);
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		fillData();
    	
    	return root;
    }
	
	private void fillData() {
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
		s_weekday.setSelection(weekly.get(Calendar.DAY_OF_WEEK));
		
		cb_is_active_daily.setChecked(PrefUtils.shouldDBBackupDaily(getActivity()));
		cb_is_active_weekly.setChecked(PrefUtils.shouldDBBackupWeekly(getActivity()));
		
		updateDisplayTimes();
	}
	
	private void updateDisplayTimes() {
		b_daily_time.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(daily.getTime()).toString());
		b_weekly_time.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(weekly.getTime()).toString());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_db_list:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                DBBackupsListFragment f = new DBBackupsListFragment().newInstance();

                if(frag != null)
                    ft.remove(frag);

                ft.add(R.id.primary_fragment_container, f);
                ft.addToBackStack(null);

                ft.commit();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}