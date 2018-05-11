package com.myMinistry.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.model.ItemWithIcon;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.utils.AppConstants;

public class ItemWithIconAdapter extends ArrayAdapter<ItemWithIcon> {
    private static final int LAYOUT_CONTENT_ID = R.layout.li_item_spinner_item_3;
    private static final int LAYOUT_SEPARATOR_ID = R.layout.li_separator_item_spinner_item;

    private int separatorId = AppConstants.CREATE_ID;
    private Context context;

    private static final int ITEM_VIEW_TYPE_RECORD = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public static final int TYPE_PUBLICATION = 1;
    public static final int TYPE_ENTRY_TYPE = 2;

    private int ADAPTER_TYPE = TYPE_PUBLICATION;

    public ItemWithIconAdapter(Context context, int type) {
        super(context, 0);
        ADAPTER_TYPE = type;
        this.context = context;
    }

    private void addSeparatorItem(String title, int count) {
        addItem(new ItemWithIcon(separatorId, title, count));
    }


    public void addItem(String title, int icon) {
        add(new ItemWithIcon(title, icon));
    }

    public void addItem(ItemWithIcon itemModel) {
        add(itemModel);
    }

    public void setTitle(String _title, int _position) {
        ItemWithIcon thisItem = this.getItem(_position);
        thisItem.setTitle(_title);
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position).getID() == separatorId) ? ITEM_VIEW_TYPE_SEPARATOR : ITEM_VIEW_TYPE_RECORD;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR; // A separator cannot be clicked !
    }

    public static class ViewHolder {
        final TextView textHolder1;
        final TextView textHolder2;
        final ImageView imgHolder;
        final TextView textCount;

        public ViewHolder(TextView text1, TextView text2, ImageView img1, TextView count) {
            this.textHolder1 = text1;
            this.textHolder2 = text2;
            this.imgHolder = img1;
            this.textCount = count;
        }
    }

    @SuppressWarnings("deprecation")
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);
        ItemWithIcon item = getItem(position);
        ViewHolder holder = null;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(type == ITEM_VIEW_TYPE_SEPARATOR ? LAYOUT_SEPARATOR_ID : LAYOUT_CONTENT_ID, parent, false);
            TextView text1 = view.findViewById(R.id.menurow_title);
            TextView text2 = view.findViewById(R.id.menurow_subtitle);
            ImageView img1 = view.findViewById(R.id.menurow_img);
            TextView tvCount = view.findViewById(R.id.menurow_count);
            view.setTag(new ViewHolder(text1, text2, img1, tvCount));
        }

        if (holder == null && view != null) {
            Object tag = view.getTag();
            if (tag instanceof ViewHolder)
                holder = (ViewHolder) tag;
        }

        holder.textHolder1.setText(item.title);
        if (type == ITEM_VIEW_TYPE_SEPARATOR) {
            holder.textCount.setText(getItem(position).getCount());
        } else {
            holder.textHolder2.setText((item.is_default == 1) ? getContext().getResources().getString(R.string.is_default) : "");

            Drawable img = ContextCompat.getDrawable(getContext(), item.iconRes);
            holder.imgHolder.setImageDrawable(img);
        }

        return view;
    }

    public void loadNewData(Cursor cursor) {
        this.clear();
        int activeId = -1;
        int firstActivePosition = -1;
        int firstInActivePosition = -1;
        int activeCount = 0;
        int inactiveCount = 0;
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)) == AppConstants.ACTIVE)
                    activeCount++;
                else
                    inactiveCount++;

                if (cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)) != activeId) {
                    if (cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)) == AppConstants.ACTIVE)
                        firstActivePosition = cursor.getPosition();
                    else
                        firstInActivePosition = cursor.getPosition();

                    activeId = cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE));
                    addSeparatorItem(activeId == AppConstants.INACTIVE ? context.getResources().getString(R.string.inactive) : context.getResources().getString(R.string.active), cursor.getCount() - cursor.getPosition());
                }

                addItem(new ItemWithIcon(cursor.getString(cursor.getColumnIndex(MinistryContract.LiteratureType.NAME))
                        , (ADAPTER_TYPE == TYPE_PUBLICATION) ? Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType._ID))) : R.drawable.ic_drawer_entry_types_new
                        , cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType._ID))
                        , cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE))
                        , cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.DEFAULT)))
                );
            }

            if (firstActivePosition != -1) {
                getItem(firstActivePosition).setCount(activeCount);
            }

            if (firstInActivePosition != -1) {
                getItem(firstInActivePosition).setCount(inactiveCount);
            }
        }
    }
}