package dev.shantanu.com.chaton.ui.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Iterator;
import java.util.List;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.entities.Conversation;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {
    private static ChatListItemListener chatListItemListener;
    private List<Conversation> mDataset;
    private String loggedInUserId;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatListAdapter(List<Conversation> myDataset, ChatListItemListener chatListItemListener, String loggedInUserId) {
        mDataset = myDataset;
        this.chatListItemListener = chatListItemListener;
        this.loggedInUserId = loggedInUserId;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_row, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        view.setOnClickListener(vh);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //find other participant id from hashmap
        String userId = "No name";
        Iterator iterator = mDataset.get(position).getParticipants().keySet().iterator();
        while (iterator.hasNext()) {
            String id = (String) iterator.next();
            if (!id.equals(loggedInUserId))
                userId = id;
        }
        holder.userName.setText(mDataset.get(position).getParticipants().get(userId));
        holder.setReceiverUserId(userId);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface ChatListItemListener {
        public void onClick(int pos,String receiverUserId);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView userName;
        private String receiverUserId;

        public MyViewHolder(View v) {
            super(v);
            userName = v.findViewById(R.id.tv_chat_list_user_name);
        }
        public String getReceiverUserId() {
            return receiverUserId;
        }

        public void setReceiverUserId(String receiverUserId) {
            this.receiverUserId = receiverUserId;
        }

        @Override
        public void onClick(View v) {
            chatListItemListener.onClick(getAdapterPosition(),receiverUserId);
        }
    }
}