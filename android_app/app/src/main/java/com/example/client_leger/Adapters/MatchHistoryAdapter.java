package com.example.client_leger.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.client_leger.R;
import com.example.client_leger.models.Game;


import java.util.ArrayList;


public class MatchHistoryAdapter extends BaseAdapter {


    private ArrayList<Game> mData = new ArrayList<Game>();
    private LayoutInflater mInflater;


    public MatchHistoryAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void addItem(final Game item) {
        mData.add(item);
        notifyDataSetChanged();
    }






    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Game getItem(int position) {
        return  mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view = null;
        int type = getItemViewType(position);
        view = new ViewHolder();
        convertView = mInflater.inflate(R.layout.matchhisotry_layout, null);

        view.gameStamp = (TextView)convertView.findViewById(R.id.gameStamp);
        view.players = convertView.findViewById(R.id.players);
        convertView.setTag(view);
        Game game = getItem(position);
        view.gameStamp.setText( game.getMode() + " " + game.getDate());

        view.players.setText(game.getPlayerlist());
        return convertView;
    }
    public class ViewHolder {
        TextView gameStamp;
        TextView players;
    }
}
