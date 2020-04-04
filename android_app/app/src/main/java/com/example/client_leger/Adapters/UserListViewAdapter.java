package com.example.client_leger.Adapters;

import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.client_leger.Fragments.LobbyFragment;
import com.example.client_leger.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.TreeSet;


public class UserListViewAdapter extends BaseAdapter {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;
    private static final int TYPE_MAX_COUNT = TYPE_BOT + 1;

    private ArrayList mData = new ArrayList();
    private LayoutInflater mInflater;
    private LobbyFragment lobbyFragment;
    private TreeSet mSeparatorsSet = new TreeSet();

    public UserListViewAdapter(LobbyFragment lobbyFragment) {
        mInflater = LayoutInflater.from(lobbyFragment.getContext());
        this.lobbyFragment = lobbyFragment;
    }

    public void addUser(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addBot(final String item) {
        mData.add(item);
        // save separator position
        mSeparatorsSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mSeparatorsSet.contains(position) ? TYPE_BOT : TYPE_USER;
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
        if (convertView == null) {
            switch (type) {
                case TYPE_USER:
                    convertView = mInflater.inflate(R.layout.lobbyuser_item, null);
                    view.myTextView = (TextView)convertView.findViewById(R.id.lobbyuser_username);
                    break;
                case TYPE_BOT:
                    convertView = mInflater.inflate(R.layout.lobbybot_item, null);
                    view.myTextView = (TextView)convertView.findViewById(R.id.lobbybot_username);
                    view.deleteIcon = convertView.findViewById(R.id.deleteIcon);
                    view.deleteIcon.setOnClickListener(v -> {
                        lobbyFragment.removeBot(getItem(position));
                        removeBot(position);
                    });
                    break;
            }
            convertView.setTag(view);
        }else {
            view = (ViewHolder)convertView.getTag();
        }
        view.myTextView.setText( getItem(position));
        return convertView;
    }

    public void addUsers(@NotNull ArrayList<String> usernames) {
        for (int i = 0; i < usernames.size(); i++) {
            mData.add(usernames.get(i));
        }
        notifyDataSetChanged();
    }
    public void removeBot(int position){
        mData.remove(position);
        // save separator position
        mSeparatorsSet.remove(position);
        notifyDataSetChanged();
    }

    public void removeUser(String username){
        mData.remove(username);
        notifyDataSetChanged();
    }
    public class ViewHolder {
        TextView myTextView;
        AppCompatImageView deleteIcon;
    }
}
