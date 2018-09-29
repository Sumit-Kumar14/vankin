package com.infinity.dev.vankin;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infinity.dev.vankin.Model.Points;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private Points[] mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    GridAdapter(Context context, Points[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.myTextView.setText(String.valueOf(mData[position].getScore()));
        switch (mData[position].getGridType()) {
            case OPEN:
                holder.myTextView.setBackgroundColor(Color.parseColor("#FFCC80"));
                holder.myTextView.setTextColor(Color.parseColor("#333333"));
                break;
            case CLOSED:
                holder.myTextView.setBackgroundColor(Color.parseColor("#FFE0B2"));
                holder.myTextView.setTextColor(Color.parseColor("#333333"));
                break;
            case SELECTED:
                holder.myTextView.setBackgroundColor(Color.parseColor("#90CAF9"));
                holder.myTextView.setTextColor(Color.parseColor("#333333"));
                break;
            case GAME_ACTUAL:
                holder.myTextView.setBackgroundColor(Color.parseColor("#80CBC4"));
                holder.myTextView.setTextColor(Color.parseColor("#333333"));
                break;
            case PROBABLE:
                holder.myTextView.setBackgroundColor(Color.parseColor("#33691E"));
                holder.myTextView.setTextColor(Color.WHITE);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tv_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return String.valueOf(mData[id].getScore());
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}