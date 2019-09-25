package com.qeh.lyubo.qeh;

import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.HorizontalScrollView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.support.v7.widget.RecyclerView.HORIZONTAL;

public class DiscovererActivity extends AppCompatActivity implements AdvertiserAdapter.OnUserListener{

    ArrayList<QnEhUser> mUsers;
    QnEhUser cUser;
    HashMap<String, QnEhUser> mAdvertisers;
    AdvertiserAdapter adapter;
    QnEhUser clicked_user;
    private ConnectionsClient mConnectionsClient;
    private AudioPlayer mAudioPlayer;
    Toolbar discovererToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discoverer);
        discovererToolbar = findViewById(R.id.toolbar);
        discovererToolbar.setTitle("Users list:");
        mConnectionsClient = Nearby.getConnectionsClient(this);
        initUsers();
    }

    @Override
    public void onBackPressed() {
        cleanup();
        Intent nIntent = new Intent(DiscovererActivity.this, MainActivity.class);
        startActivity(nIntent);
    }

    private void initUsers(){
        cUser = ((QnEhApplication) this.getApplication()).getUser();
        mAdvertisers = cUser.getAdvertisers();
        mAdvertisers.put("12345", new QnEhUser("User 1", Constants.USER_ADVERTISER, "12345"));
        mAdvertisers.put("12346", new QnEhUser("User 2", Constants.USER_ADVERTISER, "12346"));

        cUser.setAdvertisers(mAdvertisers);

        initAdvertisersView();
    }

    private void initAdvertisersView(){
        mUsers = new ArrayList<QnEhUser>(cUser.getAdvertisers().values());
        adapter = new AdvertiserAdapter(this, mUsers, this);


        RecyclerView advertisersView = findViewById(R.id.advertisers_view);
        advertisersView.setAdapter(adapter);
        advertisersView.setLayoutManager(new LinearLayoutManager(this));
        advertisersView.setLayoutManager(new LinearLayoutManager(this));
        advertisersView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.notifyDataSetChanged();
        startDiscovering();
    }

    private void removeAdvertiserEndpoint(String endpoint){
        for (int i = 0; i<mUsers.size(); i++){
            if (mUsers.get(i).getEndpoint().equals(endpoint)){
                mUsers.remove(i);
                adapter.notifyItemRemoved(i);
            }
        }
        mAdvertisers = cUser.getAdvertisers();
        mAdvertisers.remove(endpoint);
        cUser.setAdvertisers(mAdvertisers);
        ArrayList<QnEhUser> mUsers = new ArrayList<QnEhUser>(cUser.getAdvertisers().values());
    }

    private void addAdvertiser(QnEhUser user){
        mUsers.add(user);
        adapter.notifyItemInserted(mUsers.size()-1);
        mAdvertisers = cUser.getAdvertisers();
        mAdvertisers.put(user.getEndpoint(), user);
        cUser.setAdvertisers(mAdvertisers);
    }

    @Override
    public void onUserClick(int position) {
        cleanup();
        clicked_user = mUsers.get(position);
        clicked_user.setStatus(Constants.STATUS_CONNECTING);
        cUser.setStatusAdvertiser(clicked_user, Constants.STATUS_CONNECTING);
        adapter.notifyDataSetChanged();
        connectToEndpoint(clicked_user.getEndpoint());
    }

    private void cleanup() {
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
        for (QnEhUser user : cUser.getAdvertisers().values()) {
            mConnectionsClient.disconnectFromEndpoint(user.getEndpoint());
            user.setStatus(Constants.STATUS_DISCONNECTED);
        }
        adapter.notifyDataSetChanged();
    }

    protected void startDiscovering() {
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
        discoveryOptions.setStrategy(Strategy.P2P_CLUSTER);
        mConnectionsClient
            .startDiscovery(
                Constants.ServiceId,
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        if (Constants.ServiceId.equals(info.getServiceId())) {
                            addAdvertiser(new QnEhUser(info.getEndpointName(), Constants.USER_ADVERTISER, endpointId));
                        }
                    }
                    @Override
                    public void onEndpointLost(String endpointId) {
                        removeAdvertiserEndpoint(endpointId);
                    }
                },
                discoveryOptions.build())
            .addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    protected void connectToEndpoint(final String endpointId) {
        // Ask to connect
        mConnectionsClient
                .requestConnection(cUser.getName(), endpointId, mConnectionLifecycleCallback)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                final Task<Void> voidTask = mConnectionsClient.acceptConnection(endpointId, mPayloadCallback);
            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                if (!result.getStatus().isSuccess()) {
                    return;
                }
                cUser.getAdvertiserByEndpoint(endpointId).setStatus(Constants.STATUS_CONNECTED);
                ArrayList<QnEhUser> mUsers = new ArrayList<QnEhUser>(cUser.getAdvertisers().values());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onDisconnected(String endpointId) {
            }
        };

    private final PayloadCallback mPayloadCallback =
        new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                onReceivePayload(endpointId, payload);
            }

            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                // Payload progress has updated.
            }
        };

    protected void onReceivePayload(String endpointId, Payload payload) {
        if (payload.getType() == Payload.Type.STREAM) {
            if (mAudioPlayer != null) {
                mAudioPlayer.stop();
                mAudioPlayer = null;
            }

            AudioPlayer player =
                    new AudioPlayer(payload.asStream().asInputStream()) {
                        @WorkerThread
                        @Override
                        protected void onFinish() {
                            runOnUiThread(
                                    new Runnable() {
                                        @UiThread
                                        @Override
                                        public void run() {
                                            mAudioPlayer = null;
                                        }
                                    });
                        }
                    };
            mAudioPlayer = player;
            player.start();
        }
    }
}