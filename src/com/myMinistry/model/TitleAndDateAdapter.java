package com.myMinistry.model;

import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.provider.MinistryContract.Notes;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.squareup.phrase.Phrase;

public class TitleAndDateAdapter extends ResourceCursorAdapter {
	private static final int LAYOUT_ID = R.layout.li_title_and_date;
	private Calendar displayDate = Calendar.getInstance(Locale.getDefault());
	private int leadingTextID = 0;
	private boolean has_notes = false;
	private int inactivePosition = MinistryDatabase.CREATE_ID;
	
	public TitleAndDateAdapter(Context context, Cursor cursor, int _leadingTextID, boolean _has_notes) {
		super(context, LAYOUT_ID, cursor, ResourceCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		leadingTextID = _leadingTextID;
		has_notes = _has_notes;
	}
	
	private class ViewHolder {
		TextView section_header;
		TextView title;
		TextView titleHeader;
		TextView activity;
	}
	
	public void resetInactiveFlag() {
		inactivePosition = MinistryDatabase.CREATE_ID;
	}
	
	@Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(LAYOUT_ID, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.section_header = (TextView) view.findViewById(R.id.section_header);
		holder.title = (TextView) view.findViewById(R.id.title);
		holder.titleHeader = (TextView) view.findViewById(R.id.titleHeader);
		holder.activity = (TextView) view.findViewById(R.id.activity);
		view.setTag(holder);
		return view;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		
		if(inactivePosition == MinistryDatabase.CREATE_ID && cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef.ACTIVE)) == MinistryService.INACTIVE)
			inactivePosition = cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef._ID));
		
		
		holder.title.setText(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.TITLE)));
		
		/** Show/Hide publication name text */
		if(cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef.TYPE_ID)) == MinistryDatabase.ID_UNION_TYPE_PUBLICATION) {
			holder.titleHeader.setVisibility(View.VISIBLE);
			holder.titleHeader.setText(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.NAME)));
		}
		else
			holder.titleHeader.setVisibility(View.GONE);
		
		/** Show/Hide inactive text */
		if(inactivePosition == cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef._ID))) {
			holder.section_header.setVisibility(View.VISIBLE);
		}
		else
			holder.section_header.setVisibility(View.GONE);
		
		/** Display activity text */
		if(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE)) != null && cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE)).length() > 0) {
			String[] thedate = cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE)).split("-");
    		if(thedate.length == 3) {
    			/** We have the three numbers to make the date. Subtract 1 for zero based months. */ 
    			displayDate.set(Integer.valueOf(thedate[0]),Integer.valueOf(thedate[1])-1,Integer.valueOf(thedate[2]));
    			String date = DateUtils.formatDateTime(context, displayDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY);
    			String displayText = "";
    			
    			if(leadingTextID != 0)
    				displayText = Phrase.from(mContext, leadingTextID).put("date", date).format().toString();
    			else
    				displayText = date;
    			
    			if(has_notes && cursor.getString(cursor.getColumnIndex(Notes.NOTES)) != null && cursor.getString(cursor.getColumnIndex(Notes.NOTES)).length() > 0 && cursor.getString(cursor.getColumnIndex(Notes.NOTES)) != "0")
    				displayText = displayText + "\n\n" + context.getString(R.string.form_notes) + ": " + cursor.getString(cursor.getColumnIndex(Notes.NOTES));
    			
    			holder.activity.setText(displayText);
    		}
    		else
    			holder.activity.setText(R.string.no_activity);
		}
		else
			holder.activity.setText(R.string.no_activity);
	}
}