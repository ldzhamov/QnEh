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
    Context mContext;

    public AdvertiserAdapter(Context mContext, ArrayList<QnEhUser> users) {
        this.mUsers = users;
        this.mContext = mContext;
    }

    @Override
    public AdvertiserHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.advertiser_item, parent, false);
        AdvertiserHolder holder = new AdvertiserHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdvertiserHolder advertiserHolder, int position) {
            advertiserHolder.username.setText(mUsers.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class AdvertiserHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView username;
        RelativeLayout advertiserItem;

        public AdvertiserHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            username = itemView.findViewById(R.id.username);
            advertiserItem = itemView.findViewById(R.id.advertiser_item);
        }
    }
}
