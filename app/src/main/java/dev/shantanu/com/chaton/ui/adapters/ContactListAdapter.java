package dev.shantanu.com.chaton.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.entities.User;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.MyViewHolder> {

    private static ContactListItemListener contactListItemListener;
    private List<User> mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ContactListAdapter(List<User> myDataset, ContactListItemListener contactListItemListener) {
        mDataset = myDataset;
        this.contactListItemListener = contactListItemListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ContactListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
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
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.userName.setText(mDataset.get(position).getFirstName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface ContactListItemListener {
        public void onClick(int pos);
    }


    // Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView userName;

        public MyViewHolder(View v) {
            super(v);
            userName = v.findViewById(R.id.tv_chat_list_user_name);
        }

        @Override
        public void onClick(View v) {
            contactListItemListener.onClick(getAdapterPosition());
        }
    }
}
