package com.qeh.lyubo.qeh;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class DiscovererActivity extends AppCompatActivity implements AdvertiserAdapter.OnUserListener{

    private ArrayList<QnEhUser> mUsers = new ArrayList<QnEhUser>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discoverer);

        initUsers();
    }

    private void initUsers(){

        mUsers.add(new QnEhUser("User 1", Constants.USER_ADVERTISER, null));
        mUsers.add(new QnEhUser("User 2", Constants.USER_ADVERTISER, null));
        mUsers.add(new QnEhUser("User 3", Constants.USER_ADVERTISER, null));
        mUsers.add(new QnEhUser("User 4", Constants.USER_ADVERTISER, null));
        mUsers.add(new QnEhUser("User 5", Constants.USER_ADVERTISER, null));
        mUsers.add(new QnEhUser("User 6", Constants.USER_ADVERTISER, null));
        mUsers.add(new QnEhUser("User 7", Constants.USER_ADVERTISER, null));
        mUsers.add(new QnEhUser("User 8", Constants.USER_ADVERTISER, null));

        initAdvertisersView();
    }

    private void initAdvertisersView(){
        RecyclerView advertisersView = findViewById(R.id.advertisers_view);
        AdvertiserAdapter adapter = new AdvertiserAdapter(this, mUsers, this);
        advertisersView.setAdapter(adapter);
        advertisersView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onUserClick(int position) {
        QnEhUser clicked_user = mUsers.get(position);
        Log.d("Discoverer activity ", "clicked user: "+clicked_user.getName());
    }
}
