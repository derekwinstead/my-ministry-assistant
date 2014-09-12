package com.myMinistry.adapters;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.model.HouseholderForTime;
import com.myMinistry.model.QuickLiterature;

public class TimeEditorEntryAdapter extends ArrayAdapter<HouseholderForTime> {
	public static final int LAYOUT_ID = R.layout.li_time_editor_entry;
	private Context context;
	private ArrayList<HouseholderForTime> entries;
	private HouseholderForTime entry;
	private int padding;
	private LayoutParams lp1;
	private LayoutParams lp2;
	
	@TargetApi(Build.VERSION_CODES.FROYO)
	@SuppressWarnings("deprecation")
	public TimeEditorEntryAdapter(Context context, ArrayList<HouseholderForTime> entries) {
		super(context, LAYOUT_ID, entries);
		this.context = context;
		this.entries = entries;
		
		padding = Helper.dipsToPix(context, 5);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			lp1 = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		else
			lp1 = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		
		lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	}
	
	private class ViewHolder {
		LinearLayout linlay;
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;
		
		if(row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(LAYOUT_ID, parent, false);
			
			holder = new ViewHolder();
			holder.linlay = (LinearLayout)row.findViewById(R.id.linlay);
			
			row.setTag(holder);
		}
		else {
			holder = (ViewHolder)row.getTag();
			holder.linlay.removeAllViews();
		}
		
		entry = entries.get(position);
		
		TextView tv;
		
		/** Show if NOT a return visit */
		if(!entry.isCountedForReturnVisit()) {
			tv = new TextView(context);
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				tv.setBackground(context.getResources().getDrawable(R.drawable.alert_bg));
			else
				tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.alert_bg));
			
			tv.setText(R.string.menu_do_not_count_as_return_visit);
			tv.setTextAppearance(context, android.R.attr.textAppearanceMedium);
			tv.setTextColor(context.getResources().getColor(R.color.white));
			tv.setGravity(Gravity.CENTER_VERTICAL);
			tv.setLayoutParams(lp1);
			tv.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_action_warning), null, null, null);
			tv.setCompoundDrawablePadding(padding);
			holder.linlay.addView(tv);
		}
		
		/** Show Householder if exists */
		if(!TextUtils.isEmpty(entry.toString())) {
			tv = new TextView(context);
			tv.setText(entry.toString());
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
		for(QuickLiterature item : entry.getLit()) {
			tv = new TextView(context);
			tv.setTextAppearance(context, android.R.attr.textAppearanceMedium);
			tv.setTextColor(context.getResources().getColor(R.color.default_text));
			tv.setGravity(Gravity.CENTER_VERTICAL);
			tv.setText("(" + item.getCount() + ") " + item.toString());
			tv.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(Helper.getIconResIDByLitTypeID(item.getTypeID())), null, null, null);
			tv.setCompoundDrawablePadding(padding);
			tv.setLayoutParams(lp1);
			
			holder.linlay.addView(tv);
		}
		
		return row;
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