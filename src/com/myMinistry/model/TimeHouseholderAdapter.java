package com.myMinistry.model;

import java.util.ArrayList;

import com.myMinistry.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeHouseholderAdapter extends ArrayAdapter<HouseholderForTime> {
	private static final int LAYOUT_ID = R.layout.li_time_editor;
	private Context context;
	private ArrayList<HouseholderForTime> mylist;
	private StringBuilder mystring;
	private int emptyHHPosition = -1;
	
	public TimeHouseholderAdapter(Context context, ArrayList<HouseholderForTime> _mylist) {
		super(context, LAYOUT_ID, _mylist);
		this.context = context;
		mylist = _mylist;
	}
	
	class ViewHolder {
		TextView text_householder;
		TextView text_publications;
		TextView text_notes;
		LinearLayout linlay;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;
		mystring = new StringBuilder();
		
		if(row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(LAYOUT_ID, parent, false);
			
			holder = new ViewHolder();
			holder.linlay = (LinearLayout)row.findViewById(R.id.linlay);
			holder.text_householder = (TextView)row.findViewById(R.id.text_householder);
			holder.text_publications = (TextView)row.findViewById(R.id.text_publications);
			holder.text_notes = (TextView)row.findViewById(R.id.text_notes);
			
			row.setTag(holder);
		}
		else {
			holder = (ViewHolder)row.getTag();
		}
		
		HouseholderForTime item = mylist.get(position);
		
		/** Set householder name to be displayed */
		if(item.toString() != null && item.toString().length() > 0) {
			holder.text_householder.setText(item.toString());
			holder.text_householder.setVisibility(View.VISIBLE);
		}
		else
			holder.text_householder.setVisibility(View.GONE);
		
		/** Set the publications selected to be displayed */
		if(item.getID() != 0 || emptyHHPosition == -1 || emptyHHPosition == position) {
			if(item.getID() == 0)
				emptyHHPosition = position;
				
			ArrayList<QuickLiterature> litList = mylist.get(position).getLit();
			if(litList != null) {
				for(int j = 0;j < litList.size();j++) {
					if(j > 0)
						mystring.append("\n");
					mystring.append("(").append(litList.get(j).getCount()).append(") ").append(litList.get(j).toString());
				}
			}
		}
		
		if(mystring.length() > 0) {
			holder.text_publications.setText(mystring.toString());
			holder.text_publications.setVisibility(View.VISIBLE);
			mystring = new StringBuilder();
		}
		else
			holder.text_publications.setVisibility(View.GONE);
		
		/** Set the notes to be displayed */
		if(item.getNotes() != null && item.getNotes().length() > 0 && (item.getID() != 0 || emptyHHPosition == -1 || emptyHHPosition == position)) {
			mystring.append(context.getResources().getString(R.string.form_notes) + ": " + item.getNotes());

			holder.text_notes.setText(mystring.toString());
			holder.text_notes.setVisibility(View.VISIBLE);
		}
		else
			holder.text_notes.setVisibility(View.GONE);
		
		return row;
	}
}