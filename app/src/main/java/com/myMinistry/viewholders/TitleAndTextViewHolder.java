package com.myMinistry.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.model.ItemWithDate;

public class TitleAndTextViewHolder extends RecyclerView.ViewHolder {
    TextView textTitle;
    TextView textSub;
    View textContainer;

    ItemWithDate item;
    TitleAndTextListener listener;

    public interface TitleAndTextListener {
        void onItemClicked(ItemWithDate item);
    }

    public TitleAndTextViewHolder(View itemView) {
        super(itemView);
        //this.listener = listener;
        textTitle = itemView.findViewById(R.id.text1);
        textSub = itemView.findViewById(R.id.text1);
        textContainer = itemView.findViewById(R.id.text_householder); // TODO This needs to be the container for all the views to have an onclick :)

        textContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(item);
            }
        });
    }

    public void setItem(ItemWithDate item) {
        this.item = item;
        textTitle.setText(item.toString());
        textSub.setText(getFormattedDate(item));
        //textLocation.setText(item.getLocationName());
        //textDate.setText(getFormattedDate(item));
    }

    private String getFormattedDate(ItemWithDate item) {
        //String date = item.getDate();
        //String date = item.getYear() + " " + item.getEra();
        return item.getDate();
    }
}