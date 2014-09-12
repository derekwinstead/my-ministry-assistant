package com.myMinistry.adapters;

import com.myMinistry.R;
import com.myMinistry.model.NavDrawerMenuItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ItemAdapter extends ArrayAdapter<NavDrawerMenuItem> {
	private int LAYOUT_VIEW_ID = R.layout.li_item_spinner_item;
	
	public ItemAdapter(Context context) {
		super(context, 0);
	}
	
	public void addItem(String title, int icon) {
		add(new NavDrawerMenuItem(title, icon));
	}
	
	public void addItem(NavDrawerMenuItem itemModel) {
		add(itemModel);
	}
	
	public void setTitle(String _title, int _position) {
		NavDrawerMenuItem thisItem = this.getItem(_position);
		thisItem.setTitle(_title);
	}
	
	public NavDrawerMenuItem getItemByID(int _id) {
		for (int i = 0; i < this.getCount(); i++) {
			NavDrawerMenuItem item = this.getItem(i);
			if (item.getID() == _id)
				return item;
		}
		return null;
	}
	
	public int getPositionByID(int _id) {
		for (int i = 0; i < this.getCount(); i++) {
			NavDrawerMenuItem item = this.getItem(i);
			if (item.getID() == _id)
				return i;
		}
		return -1;
	}
	
	public static class ViewHolder {
		public final TextView textHolder;
		
		public ViewHolder(TextView text1) {
			this.textHolder = text1;
		}
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		NavDrawerMenuItem item = getItem(position);
		ViewHolder holder = null;
		View view = convertView;
		
		if(view == null) {
			view = LayoutInflater.from(getContext()).inflate(LAYOUT_VIEW_ID, parent, false);
			
			TextView text1 = (TextView) view.findViewById(R.id.menurow_title);
			view.setTag(new ViewHolder(text1));
		}
		
		if(holder == null && view != null) {
			Object tag = view.getTag();
			if (tag instanceof ViewHolder)
				holder = (ViewHolder) tag;
		}
		
		Drawable img = getContext().getResources().getDrawable( item.iconRes );
		holder.textHolder.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
		holder.textHolder.setText(item.title);
		
		return view;
	}
}