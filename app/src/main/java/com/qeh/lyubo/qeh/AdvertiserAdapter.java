package com.qeh.lyubo.qeh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class AdvertiserAdapter extends RecyclerView.Adapter<AdvertiserAdapter.AdvertiserHolder>{

    private ArrayList<QnEhUser> mUsers = new ArrayList<QnEhUser>();
    private OnUserListener mOnUserListener;
    Context mContext;

    public AdvertiserAdapter(Context mContext, ArrayList<QnEhUser> users, OnUserListener onUserListener) {
        this.mUsers = users;
        this.mContext = mContext;
        this.mOnUserListener = onUserListener;
    }

    @Override
    public AdvertiserHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.advertiser_item, parent, false);
        AdvertiserHolder holder = new AdvertiserHolder(view, mOnUserListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdvertiserHolder advertiserHolder, int position) {
            advertiserHolder.username.setText(mUsers.get(position).getName());
            if (mUsers.get(position).getStatus() == Constants.STATUS_CONNECTED) {
                advertiserHolder.image.setImageResource(R.drawable.speaker_online);
            }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class AdvertiserHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView image;
        TextView username;
        RelativeLayout advertiserItem;
        OnUserListener onUserListener;

        public AdvertiserHolder(View itemView, OnUserListener onUserListener) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            username = itemView.findViewById(R.id.username);
            advertiserItem = itemView.findViewById(R.id.advertiser_item);
            this.onUserListener = onUserListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onUserListener.onUserClick(getAdapterPosition());
        }
    }

    public interface OnUserListener{
        void onUserClick(int position);
    }
}