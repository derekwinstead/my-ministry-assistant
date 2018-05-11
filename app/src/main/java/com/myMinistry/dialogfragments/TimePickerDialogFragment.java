package com.myMinistry.dialogfragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.myMinistry.utils.AppConstants;

import java.util.Calendar;

public class TimePickerDialogFragment extends DialogFragment {
	private TimePickerDialogFragmentListener sListener;
		
	public static TimePickerDialogFragment newInstance(Calendar _date) {
		TimePickerDialogFragment frag = new TimePickerDialogFragment();
		Bundle args = new Bundle();
        args.putInt(AppConstants.ARG_HOUR_OF_DAY, _date.get(Calendar.HOUR_OF_DAY));
        args.putInt(AppConstants.ARG_MINUTES, _date.get(Calendar.MINUTE));
        frag.setArguments(args);
        return frag; 
    }
	
	public interface TimePickerDialogFragmentListener {
	    void TimePickerDialogFragmentListenerSet(int hourOfDay, int minute);
	}
	
	public void setTimePickerDialogFragmentListener(TimePickerDialogFragmentListener listener){
		sListener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new TimePickerDialog(this.getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker arg0, int hourOfDay, int minute) {
				sListener.TimePickerDialogFragmentListenerSet(hourOfDay, minute);
			}
		}, getArguments().getInt(AppConstants.ARG_HOUR_OF_DAY), getArguments().getInt(AppConstants.ARG_MINUTES), false);
		/*
		TimePickerDialog builder = new TimePickerDialog(this.getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker arg0, int hourOfDay, int minute) {
				sListener.TimePickerDialogFragmentListenerSet(hourOfDay, minute);
			}
		}, getArguments().getInt(ARG_HOUR_OF_DAY), getArguments().getInt(ARG_MINUTES), false);
	    return builder;
	    */
    }
}