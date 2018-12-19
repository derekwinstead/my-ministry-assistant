package com.myMinistry.ui.backups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.ui.backups.model.Backup;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class BackupsAdapter extends RecyclerView.Adapter<BackupsAdapter.ViewHolder> {
    private ArrayList<Backup> mData;
    private LayoutInflater mInflater;
    //private ItemClickListener mClickListener;

    // data is passed into the constructor
    public BackupsAdapter(Context context, ArrayList<Backup> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.backups_li, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.myTextView.setText(mData.get(position).getName());
        //TODO do this for older api under < 23
        /*
        private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(getColor(color), PorterDuff.Mode.SRC_IN));
            }
        }
    }
         */
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {//} implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.title);
        }
    }
}