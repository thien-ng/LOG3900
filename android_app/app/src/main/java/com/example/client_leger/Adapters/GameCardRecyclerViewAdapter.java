package com.example.client_leger.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.client_leger.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameCardRecyclerViewAdapter extends RecyclerView.Adapter<GameCardRecyclerViewAdapter.ViewHolder> {

    private JSONArray mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public GameCardRecyclerViewAdapter(Context context, JSONArray data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.gamecard_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.myTextView.setText(mData.getJSONObject(position).getString("gameName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.textView_gameName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public JSONObject getItem(int id) throws JSONException {
        return mData.getJSONObject(id);
    }

    public void addItems(JSONArray items) throws JSONException {
        for (int i = 0; i < items.length(); i++) {
            mData.put(items.getJSONObject(i));
            notifyItemChanged(i);
        }

    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}