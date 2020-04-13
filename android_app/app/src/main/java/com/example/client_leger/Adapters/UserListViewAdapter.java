package com.example.client_leger.Adapters;

import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.client_leger.Fragments.LobbyFragment;
import com.example.client_leger.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class UserListViewAdapter extends BaseAdapter {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;
    private static final int TYPE_MAX_COUNT = TYPE_BOT + 1;

    private ArrayList mData = new ArrayList();
    private LayoutInflater mInflater;
    private LobbyFragment lobbyFragment;
    private Boolean isMaster;

    public UserListViewAdapter(LobbyFragment lobbyFragment, boolean isMaster) {
        mInflater = LayoutInflater.from(lobbyFragment.getContext());
        this.lobbyFragment = lobbyFragment;
        this.isMaster = isMaster;
    }

    public void addUser(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addBot(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).startsWith("bot:")? TYPE_BOT : TYPE_USER;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return (String) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        int type = getItemViewType(position);
        view = new ViewHolder();

        String name = getItem(position);

        if (convertView == null) {
            switch (type) {
                case TYPE_USER:
                    convertView = mInflater.inflate(R.layout.lobbyuser_item, null);
                    view.myTextView = convertView.findViewById(R.id.lobbyuser_username);
                    if (isMaster) {
                       view.deleteIcon = convertView.findViewById(R.id.deleteIcon);
                    } else {
                        convertView.findViewById(R.id.deleteIcon).setVisibility(View.GONE);
                    }
                    break;
                case TYPE_BOT:
                    convertView = mInflater.inflate(R.layout.lobbybot_item, null);
                    view.myTextView = convertView.findViewById(R.id.lobbybot_username);
                    if (isMaster) {
                        view.deleteIcon = convertView.findViewById(R.id.deleteIcon);
                    } else {
                        convertView.findViewById(R.id.deleteIcon).setVisibility(View.GONE);
                    }
                    break;
            }
            convertView.setTag(view);
        }else {
            view = (ViewHolder)convertView.getTag();
        }

        view.myTextView.setText( name);
        if (isMaster) {
            view.deleteIcon = convertView.findViewById(R.id.deleteIcon);
            if(name.equals(lobbyFragment.username)){
                view.deleteIcon.setVisibility(View.GONE);
            }
            else {
                view.deleteIcon.setVisibility(View.VISIBLE);
                view.deleteIcon.setOnClickListener(v -> lobbyFragment.removePlayer(name));
            }
        }
        return convertView;
    }

    public void addUsers(@NotNull ArrayList<String> usernames) {
        for (int i = 0; i < usernames.size(); i++) {
            mData.add(usernames.get(i));
        }
        notifyDataSetChanged();
    }

    public void removePlayer(String name, String clientName){
        mData.remove(name);
        isMaster = lobbyFragment.checkIsMaster();
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        TextView myTextView;
        AppCompatImageView deleteIcon;
    }
}
