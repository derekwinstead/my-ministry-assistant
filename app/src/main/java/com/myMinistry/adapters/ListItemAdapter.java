package com.myMinistry.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.model.ListItem;

public class ListItemAdapter extends ArrayAdapter<ListItem> {
    private int LAYOUT_VIEW_ID = R.layout.li_item_spinner_listitem;

    public ListItemAdapter(Context context) {
        super(context, 0);
    }

    public void addItem(int id, int icon, String title, String subtitle) {
        add(new ListItem(id, icon, title, subtitle));
    }

    public void addItem(ListItem itemModel) {
        add(itemModel);
    }

    public void setTitle(String _title, int _position) {
        ListItem thisItem = this.getItem(_position);
        thisItem.setTitle(_title);
    }

    public ListItem getItemByID(int _id) {
        for (int i = 0; i < this.getCount(); i++) {
            ListItem item = this.getItem(i);
            if (item.getID() == _id)
                return item;
        }
        return null;
    }

    public int getPositionByID(int _id) {
        for (int i = 0; i < this.getCount(); i++) {
            ListItem item = this.getItem(i);
            if (item.getID() == _id)
                return i;
        }
        return -1;
    }

    public static class ViewHolder {
        public final TextView menurow_title;
        public final TextView menurow_subtitle;
        public final ImageView menurow_img;

        public ViewHolder(TextView title, TextView subtitle, ImageView img) {
            this.menurow_title = title;
            this.menurow_subtitle = subtitle;
            this.menurow_img = img;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = getItem(position);
        ViewHolder holder = null;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(LAYOUT_VIEW_ID, parent, false);
            TextView title = (TextView) view.findViewById(R.id.menurow_title);
            TextView subtitle = (TextView) view.findViewById(R.id.menurow_subtitle);
            ImageView img = (ImageView) view.findViewById(R.id.menurow_img);
            view.setTag(new ViewHolder(title, subtitle, img));
        }

        if (holder == null && view != null) {
            Object tag = view.getTag();
            if (tag instanceof ViewHolder)
                holder = (ViewHolder) tag;
        }

        Drawable img = ContextCompat.getDrawable(getContext(), item.iconRes);
        holder.menurow_img.setImageDrawable(img);
        holder.menurow_title.setText(item.title);

        if (item.subtitle.length() == 0) {
            holder.menurow_subtitle.setVisibility(View.GONE);
        } else {
            holder.menurow_subtitle.setVisibility(View.VISIBLE);
            holder.menurow_subtitle.setText(item.subtitle);
        }

        return view;
    }
}