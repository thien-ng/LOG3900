package com.example.client_leger.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.client_leger.Models.GameCard;
import com.example.client_leger.Models.Lobby;
import com.example.client_leger.R;

import java.util.ArrayList;

public class GameCardRecyclerViewAdapter extends RecyclerView.Adapter<GameCardRecyclerViewAdapter.ViewHolder> {

    private ArrayList<GameCard> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private GameCard currentCard;
    // data is passed into the constructor
    public GameCardRecyclerViewAdapter(Context context, ArrayList<GameCard> data) {
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
        holder.myTextView.setText(mData.get(position).getGameName());
        holder.spinner.setAdapter(mData.get(position).getAdapter());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        Spinner spinner;
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.textView_gameName);
            spinner = itemView.findViewById(R.id.lobbies);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public GameCard getItem(int id) {
        return mData.get(id);
    }

    public int getItemPos(String gameID) {
        for (int i = 0; i < mData.size(); i++) {
            GameCard item = mData.get(i);
            if (item.getGameId().equals(gameID)) return i;
        }
        return -1;
    }

    public void addItems(ArrayList<GameCard> items) {
        for (int i = 0; i < items.size(); i++) {
            mData.add(items.get(i));
            notifyItemChanged(i);
        }
    }

    public void addLobbies(ArrayList<Lobby> lobbies, String gameID){
        int itempos = getItemPos(gameID);
        GameCard item = mData.get(itempos);
        item.setLobbies(lobbies);
        for (Lobby lobby:lobbies) {
            item.getAdapter().add(lobby.getLobbyName());
        }

        notifyItemChanged(itempos);
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