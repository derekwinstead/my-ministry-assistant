package com.myMinistry.dialogfragments;

import static com.myMinistry.util.LogUtils.makeLogTag;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerDialogFragment extends DialogFragment {
	public static String ARG_YEAR = "year";
	public static String ARG_MONTH = "month";
	public static String ARG_DAY = "day";
	
	private DatePickerDialogFragmentListener sListener;
	public static final String TAG = makeLogTag(DatePickerDialogFragment.class);
	
	public static DatePickerDialogFragment newInstance(Calendar _date) {
		DatePickerDialogFragment frag = new DatePickerDialogFragment();
		Bundle args = new Bundle();
        args.putInt(ARG_YEAR, _date.get(Calendar.YEAR));
        args.putInt(ARG_MONTH, _date.get(Calendar.MONTH));
        args.putInt(ARG_DAY, _date.get(Calendar.DAY_OF_MONTH));
        frag.setArguments(args);
        return frag; 
    }
	
	public interface DatePickerDialogFragmentListener {
	    public void DatePickerDialogFragmentListenerSet(int selectedYear, int selectedMonth, int selectedDay);
	}
	
	public void setDatePickerDialogFragmentListener(DatePickerDialogFragmentListener listener){
		sListener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		DatePickerDialog builder = new DatePickerDialog(this.getActivity(), new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
				sListener.DatePickerDialogFragmentListenerSet(selectedYear, selectedMonth, selectedDay);
			}
		}, getArguments().getInt(ARG_YEAR), getArguments().getInt(ARG_MONTH), getArguments().getInt(ARG_DAY));
	    return builder;
    }
}