package com.example.client_leger.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.client_leger.R;

import java.util.ArrayList;
import java.util.TreeSet;


public class ConnexionListViewAdapter extends BaseAdapter {

    private static final int TYPE_IN = 0;
    private static final int TYPE_OUT = 1;
    private static final int TYPE_MAX_COUNT = TYPE_OUT + 1;

    private ArrayList mData = new ArrayList();
    private LayoutInflater mInflater;

    private TreeSet mSeparatorsSet = new TreeSet();

    public ConnexionListViewAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void addLoggedIn(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addLoggedOut(final String item) {
        mData.add(item);
        // save separator position
        mSeparatorsSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mSeparatorsSet.contains(position) ? TYPE_OUT : TYPE_IN;
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
        ViewHolder view = null;
        int type = getItemViewType(position);
        System.out.println("getView " + position + " " + convertView + " type = " + type);
        view = new ViewHolder();
        if (convertView == null) {
            switch (type) {
                case TYPE_IN:
                    convertView = mInflater.inflate(R.layout.connection_layout, null);
                    break;
                case TYPE_OUT:
                    convertView = mInflater.inflate(R.layout.disconnection_layout, null);
                    break;
            }
            view.myTextView = (TextView)convertView.findViewById(R.id.list_content);
            convertView.setTag(view);
        }else {
            view = (ViewHolder)convertView.getTag();
        }
        view.myTextView.setText( getItem(position));
        return convertView;
    }
    public class ViewHolder {
        TextView myTextView;
    }
}
