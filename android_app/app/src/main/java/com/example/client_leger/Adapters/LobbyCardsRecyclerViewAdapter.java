package com.example.client_leger.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.client_leger.models.Lobby;
import com.example.client_leger.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class LobbyCardsRecyclerViewAdapter extends RecyclerView.Adapter<LobbyCardsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Lobby> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public LobbyCardsRecyclerViewAdapter(Context context, ArrayList<Lobby> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.lobbycard_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lobby currLobby = mData.get(position);
        holder.myTextView.setText(currLobby.getLobbyName());
        holder.usersListView.setAdapter(currLobby.getAdapter());
        holder.lobbySize.setText(currLobby.getUsernames().size() + "/" + currLobby.getSize() + " players");
        if (currLobby.getPrivate()) {
            holder.joinPrivateButton.setEnabled(true);
            holder.joinPrivateButton.setVisibility(View.VISIBLE);
        } else {
            holder.joinButton.setEnabled(true);
            holder.joinButton.setVisibility(View.VISIBLE);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        TextView lobbySize;
        View expandUsers;
        ListView usersListView;
        Button joinButton;
        Button joinPrivateButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.listView_users).setVisibility(View.GONE);

            myTextView = itemView.findViewById(R.id.textView_gameName);
            expandUsers = itemView.findViewById(R.id.view_expandUsers);
            lobbySize = itemView.findViewById(R.id.textView_lobbySize);
            usersListView = itemView.findViewById(R.id.listView_users);
            joinButton = itemView.findViewById(R.id.button_join);
            joinPrivateButton = itemView.findViewById(R.id.button_joinPrivate);

            joinButton.setOnClickListener(this);
            joinPrivateButton.setOnClickListener(this);
            expandUsers.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
                if (view.getId() == joinButton.getId()) {
                    mClickListener.onJoinClick(view, getAdapterPosition());
                } else if (view.getId() == expandUsers.getId()) {
                    mClickListener.onUsersDropClick(usersListView, getAdapterPosition());
                } else if (view.getId() == joinPrivateButton.getId()) {
                    mClickListener.onJoinPrivateClick(view, getAdapterPosition());
                }

            }
        }
    }

    // convenience method for getting data at click position
    public Lobby getItem(int id) {
        return mData.get(id);
    }

    public int getItemId(String lobbyName) {
        for (int index = 0; index < mData.size(); index++)
            if (lobbyName.equals(mData.get(index).getLobbyName())) {
                return index;
            }
        return -1;
    }

    public void removeItem(@Nullable Lobby lobby) {
        int id = getItemId(lobby.getLobbyName());
        if (id > 0) {
            mData.remove(id);
            notifyItemRemoved(id);
        }
    }

    public void addItem(Lobby item) {
        mData.add(item);
        notifyItemChanged(mData.size() - 1);
    }

    public void setItems(ArrayList<Lobby> items) {
        mData.clear();
        notifyDataSetChanged();
        for (int i = 0; i < items.size(); i++) {
            mData.add(items.get(i));
            notifyItemChanged(i);
        }
    }

    public void updateUser(@NotNull String lobbyName, @NotNull String username) {
        int id = getItemId(lobbyName);
        if (id > 0) {
            getItem(id).getUsernames().add(username);
            notifyItemChanged(id);
        }
    }

    // allows clicks events to be ca
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onJoinClick(View view, int position);

        void onUsersDropClick(View view, int position);

        void onJoinPrivateClick(View view, int adapterPosition);
    }
}