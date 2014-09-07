package com.myMinistry.model;

import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.LiteraturePlaced;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.squareup.phrase.Phrase;

public class PublicationRecentActivityAdapter extends ResourceCursorAdapter {
	private static final int LAYOUT_ID = R.layout.li_publication_recent_activity;
	private Calendar displayDate = Calendar.getInstance(Locale.getDefault());
	
	public PublicationRecentActivityAdapter(Context context, Cursor cursor) {
		super(context, LAYOUT_ID, cursor, ResourceCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	}
	
	private class ViewHolder {
		TextView activity_title;
		TextView activity_date;
	}
	
	@Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(LAYOUT_ID, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.activity_title = (TextView) view.findViewById(R.id.activity_title);
		holder.activity_date = (TextView) view.findViewById(R.id.activity_date);
		view.setTag(holder);
		return view;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		
		// Not assigned to a householder
		if(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.HOUSEHOLDER_NAME)))) {
			holder.activity_title.setText(Phrase.from(context, R.string.activity_for_publication)
					.put("count", cursor.getString(cursor.getColumnIndex(LiteraturePlaced.COUNT)))
					.put("service", cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.ENTRY_TYPE_NAME)))
					.format().toString());
		}
		// Assigned to a householder
		else {
			holder.activity_title.setText(Phrase.from(context, R.string.activity_for_publication_with_householder)
					.put("count", cursor.getString(cursor.getColumnIndex(LiteraturePlaced.COUNT)))
					.put("householder", cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.HOUSEHOLDER_NAME)))
					.put("service", cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.ENTRY_TYPE_NAME)))
					.format().toString());
		}
		
		/** Get the date */
		if(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE)) != null && cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE)).length() > 0) {
			String[] thedate = cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE)).split("-");
    		if(thedate.length == 3) {
    			/** We have the three numbers to make the date. Subtract 1 for zero based months. */ 
    			displayDate.set(Integer.valueOf(thedate[0]),Integer.valueOf(thedate[1])-1,Integer.valueOf(thedate[2]));
    			String date = DateUtils.formatDateTime(context, displayDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY);
    			
    			holder.activity_date.setText(Phrase.from(context, R.string.activity_date_by_publisher)
    					.put("date", date)
    					.put("publisher", cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.PUBLISHER_NAME)))
    					.format().toString());
    		}
    		else
    			holder.activity_date.setText(R.string.no_activity);
		}
		else
			holder.activity_date.setText(R.string.no_activity);
	}
}