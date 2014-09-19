package com.myMinistry.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteraturePlaced;
import com.myMinistry.provider.MinistryContract.Notes;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryContract.TimeHouseholder;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.myMinistry.util.TimeUtils;

public class PublisherRecentActivityAdapter extends ResourceCursorAdapter {
	public static final int LAYOUT_ID = R.layout.li_publisher_recent_activity;
	private Calendar displayDateStart = Calendar.getInstance(Locale.getDefault());
	private Calendar displayDateEnd = Calendar.getInstance(Locale.getDefault());
	private MinistryService database;
	private Entry entry = new Entry();
	private ArrayList<Entry> entries;
	private int padding;
	private LayoutParams lp1;
	private LayoutParams lp2;
	
	@TargetApi(Build.VERSION_CODES.FROYO)
	@SuppressWarnings("deprecation")
	public PublisherRecentActivityAdapter(Context context, Cursor cursor) {
		super(context, LAYOUT_ID, cursor, ResourceCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		database = new MinistryService(context);
		padding = Helper.dipsToPix(context, 5);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			lp1 = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		else
			lp1 = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		
		lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	}
	
	private class ViewHolder {
		LinearLayout linlay;
		TextView title;
		TextView hours;
		TextView thistime;
	}
	
	@Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(LAYOUT_ID, parent, false);
		ViewHolder holder = new ViewHolder();
		
		holder.linlay = (LinearLayout) view.findViewById(R.id.linlay);
		holder.title = (TextView) view.findViewById(R.id.title);
		holder.hours = (TextView) view.findViewById(R.id.hours);
		holder.thistime = (TextView) view.findViewById(R.id.time);
		
		view.setTag(holder);
		return view;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		
		entries = new ArrayList<Entry>();
		entry = new Entry();
		

		/** Set the date for the view */
		String[] splits = cursor.getString(cursor.getColumnIndex(Time.DATE_START)).split("-");
		/** We have the three numbers to make the date. */
		displayDateStart.set(Calendar.YEAR, Integer.valueOf(splits[0]));
		/** Subtract 1 for zero based months. */
		displayDateStart.set(Calendar.MONTH, Integer.valueOf(splits[1])-1);
		displayDateStart.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splits[2]));
		
		try {
			splits = cursor.getString(cursor.getColumnIndex(Time.DATE_END)).split("-");
		}
		catch (Exception e) {
			splits = null;
		}
		if(splits != null && splits.length == 3) {
			/** We have the three numbers to make the date. */
			displayDateEnd.set(Calendar.YEAR, Integer.valueOf(splits[0]));
			/** Subtract 1 for zero based months. */
			displayDateEnd.set(Calendar.MONTH, Integer.valueOf(splits[1])-1);
			displayDateEnd.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splits[2]));
		}
		else {
			displayDateEnd.set(Calendar.YEAR, displayDateStart.get(Calendar.YEAR));
			displayDateEnd.set(Calendar.MONTH, displayDateStart.get(Calendar.MONTH));
			displayDateEnd.set(Calendar.DAY_OF_MONTH, displayDateStart.get(Calendar.DAY_OF_MONTH));
		}
		
		/** Set the time of the entry */
		splits = cursor.getString(cursor.getColumnIndex(Time.TIME_START)).split(":");
		displayDateStart.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splits[0]));
		displayDateStart.set(Calendar.MINUTE, Integer.valueOf(splits[1]));
		displayDateStart.set(Calendar.MILLISECOND, 0);

		splits = cursor.getString(cursor.getColumnIndex(Time.TIME_END)).split(":");
		displayDateEnd.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splits[0]));
		displayDateEnd.set(Calendar.MINUTE, Integer.valueOf(splits[1]));
		displayDateEnd.set(Calendar.MILLISECOND, 0);
		
		/** Set the display in the view as  Ddd, Mmm dd, H:MMTT - H:MMTT */
		holder.thistime.setText(Helper.buildTimeEntryDateAndTime(context, displayDateStart, displayDateEnd));
		
		/** Set the display in the view as Xh Ym */
		holder.hours.setText(TimeUtils.getTimeLength(displayDateStart, displayDateEnd, context.getString(R.string.hours_label), context.getString(R.string.minutes_label)));
		
		/** Set the display in the view for the header */
		holder.title.setText(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.ENTRY_TYPE_NAME)));
		
		if(!database.isOpen())
			database.openWritable();
		
		Cursor timeHouseholders = database.fetchTimeHouseholdersForTimeByID(cursor.getInt(cursor.getColumnIndex(Time._ID)));
		Cursor publications;
		
		/** Loop over the householders */
		for(timeHouseholders.moveToFirst();!timeHouseholders.isAfterLast();timeHouseholders.moveToNext()) {
			entry = new Entry();
			entry.setHouseholder(timeHouseholders.getString(timeHouseholders.getColumnIndex(Householder.NAME)));
			entry.setNotes(timeHouseholders.getString(timeHouseholders.getColumnIndex(Notes.NOTES)));
			
			publications = database.fetchPlacedLitByTimeAndHouseholderID(cursor.getInt(cursor.getColumnIndex(Time._ID)), timeHouseholders.getInt(timeHouseholders.getColumnIndex(TimeHouseholder.HOUSEHOLDER_ID)));
			for(publications.moveToFirst();!publications.isAfterLast();publications.moveToNext()) {
				entry.addPublication(new PublicationItem(publications.getString(publications.getColumnIndex(Literature.NAME)), publications.getInt(publications.getColumnIndex(Literature.TYPE_OF_LIERATURE_ID)), publications.getInt(publications.getColumnIndex(LiteraturePlaced.COUNT))));
			}
			publications.close();
			
			entries.add(entry);
		}
		
		timeHouseholders.close();
		
		database.close();
		
		holder.linlay.removeAllViews();
		
		for(Entry entry : entries) {
			View v = new View(context);
			@SuppressWarnings("deprecation")
			LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()));
			lp3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics()));
			v.setLayoutParams(lp3);
			v.setBackgroundColor(context.getResources().getColor(R.color.navdrawer_divider));
			holder.linlay.addView(v);
			
			TextView tv;
			
			/** Show Householder if exists */
			if(!TextUtils.isEmpty(entry.getHouseholder())) {
				tv = new TextView(context);
				tv.setText(entry.getHouseholder());
				tv.setTextAppearance(context, android.R.attr.textAppearanceLarge);
				tv.setTextColor(context.getResources().getColor(R.color.bg_card_title_text_holo_light));
				tv.setGravity(Gravity.CENTER_VERTICAL);
				tv.setLayoutParams(lp1);
				tv.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_drawer_householder), null, null, null);
				tv.setCompoundDrawablePadding(padding);
				holder.linlay.addView(tv);
			}
			
			/** Show Notes if exists */
			if(!TextUtils.isEmpty(entry.getNotes())) {
				ImageView iv = new ImageView(context);
				iv.setPadding(0, 0, padding, 0);
				iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_chat));
				iv.setLayoutParams(lp2);
				iv.setContentDescription(context.getResources().getString(R.string.form_notes));
				
				tv = new TextView(context);
				tv.setText(entry.getNotes());
				tv.setTextAppearance(context, android.R.attr.textAppearanceMedium);
				tv.setTextColor(context.getResources().getColor(R.color.default_text));
				tv.setGravity(Gravity.CENTER_VERTICAL);
				tv.setLayoutParams(lp2);
				tv.setPadding(0, padding, 0, 0);
				
				LinearLayout linlay = new LinearLayout(context);
				linlay.setLayoutParams(lp1);
				linlay.setOrientation(LinearLayout.HORIZONTAL);
				linlay.addView(iv);
				linlay.addView(tv);
				
				holder.linlay.addView(linlay);
			}
			
			/** Load the publications for the entry */
			for(PublicationItem item : entry.pubs) {
				tv = new TextView(context);
				tv.setTextAppearance(context, android.R.attr.textAppearanceMedium);
				tv.setTextColor(context.getResources().getColor(R.color.default_text));
				tv.setGravity(Gravity.CENTER_VERTICAL);
				tv.setText(item.toString());
				tv.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(item.iconRes), null, null, null);
				tv.setCompoundDrawablePadding(padding);
				tv.setLayoutParams(lp1);
				
				holder.linlay.addView(tv);
			}
		}
	}
	
	public class PublicationItem {
		public String title;
		public int iconRes;
		public Drawable icon;
		public int count;
		public int litTypeID;
		
		public PublicationItem(String _title, int _litTypeID, int _count) {
			title = _title;
			litTypeID = _litTypeID;
			count = _count;
			iconRes = Helper.getIconResIDByLitTypeID(_litTypeID);
		};
		
		public String toString() {
			return "(" + count + ") " + title;
		}
		
		public int getIconResID() {
			return iconRes;
		}
	}
	
	public class Entry {
		public ArrayList<PublicationItem> pubs;
		public String householder;
		public String notes;
		
		public Entry() {
			pubs = new ArrayList<PublicationItem>();
			householder = "";
			notes = "";
		};
		
		public void setHouseholder(String string) {
			householder = string;
		}
		
		public String getHouseholder() {
			return householder;
		}
		
		public void setNotes(String string) {
			notes = string;
		}
		
		public String getNotes() {
			return notes;
		}
		
		public void addPublication(PublicationItem _pub) {
			pubs.add(_pub);
		}
		
		public ArrayList<PublicationItem> getPublications() {
			return pubs;
		}
	}
}