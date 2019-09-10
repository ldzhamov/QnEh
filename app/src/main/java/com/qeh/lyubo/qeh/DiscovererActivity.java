package com.qeh.lyubo.qeh;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

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

public class DiscovererActivity extends AppCompatActivity implements AdvertiserAdapter.OnUserListener{

    ArrayList<QnEhUser> mUsers;
    QnEhUser cUser;
    HashMap<String, QnEhUser> mAdvertisers;
    AdvertiserAdapter adapter;
    QnEhUser clicked_user;
    private ConnectionsClient mConnectionsClient;
    private AudioPlayer mAudioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discoverer);
        mConnectionsClient = Nearby.getConnectionsClient(this);
        initUsers();
    }

    private void initUsers(){
        Log.e("Discovery activity: ", "Init users");
        cUser = ((QnEhApplication) this.getApplication()).getUser();
        mAdvertisers = cUser.getAdvertisers();
        mAdvertisers.put("12345", new QnEhUser("User 1", Constants.USER_ADVERTISER, "12345"));
        mAdvertisers.put("12346", new QnEhUser("User 2", Constants.USER_ADVERTISER, "12346"));
        //mAdvertisers.put("12347", new QnEhUser("User 3", Constants.USER_ADVERTISER, "12347"));
        //mAdvertisers.put("12348", new QnEhUser("User 4", Constants.USER_ADVERTISER, "12348"));
        //mAdvertisers.put("12349", new QnEhUser("User 5", Constants.USER_ADVERTISER, "12349"));

        cUser.setAdvertises(mAdvertisers);

        initAdvertisersView();
    }

    private void initAdvertisersView(){
        mUsers = new ArrayList<QnEhUser>(cUser.getAdvertisers().values());

        RecyclerView advertisersView = findViewById(R.id.advertisers_view);
        adapter = new AdvertiserAdapter(this, mUsers, this);
        advertisersView.setAdapter(adapter);
        advertisersView.setLayoutManager(new LinearLayoutManager(this));
        startDiscovering();
    }

    private void removeAdvertiserEndpoint(String endpoint){
        for (int i = 0; i<mUsers.size(); i++){
            Log.e("Discovery activity: ", "Endpoint list: "+mUsers.get(i).getEndpoint());
            Log.e("Discovery activity: ", "Endpoint list2: "+endpoint);
            if (mUsers.get(i).getEndpoint().equals(endpoint)){
                Log.e("Discovery activity: ", "Will remove: "+mUsers.get(i).getEndpoint());
                mUsers.remove(i);
                adapter.notifyItemRemoved(i);
            }
        }
        mAdvertisers = cUser.getAdvertisers();
        mAdvertisers.remove(endpoint);
        cUser.setAdvertises(mAdvertisers);
        //ArrayList<QnEhUser> mUsers = new ArrayList<QnEhUser>(cUser.getAdvertisers().values());
    }

    private void addAdvertiser(QnEhUser user){
        mUsers.add(user);
        adapter.notifyItemInserted(mUsers.size()-1);
        mAdvertisers = cUser.getAdvertisers();
        mAdvertisers.put(user.getEndpoint(), user);
        cUser.setAdvertises(mAdvertisers);
    }

    @Override
    public void onUserClick(int position) {
        clicked_user = mUsers.get(position);
        Log.e("Discovery activity ", "Trying to connect to: "+clicked_user.getEndpoint());
        connectToEndpoint(clicked_user.getEndpoint());
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
                            Log.e("Discovery activity: ", "Endpoint found "+endpointId);
                        }
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        Log.e("Discovery activity: ", "Endpoint lost "+endpointId);
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
                                Log.e("Discovery activity ", "Failed to connect");
                            }
                        });
    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                Log.e("Discovery activity ", "Initiated connection");
                final Task<Void> voidTask = mConnectionsClient.acceptConnection(endpointId, mPayloadCallback);
            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e("Discovery activity ", "Res not success");
                    return;
                }
                Log.e("Discovery activity", "Connected to endpoint "+endpointId);
            }

            @Override
            public void onDisconnected(String endpointId) {
                Log.e("Discovery activity ", "Disconnected from "+endpointId);
            }
        };

    private final PayloadCallback mPayloadCallback =
        new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                Log.e("Discovery activity", "Receiveing data");
                onReceivePayload(endpointId, payload);
                //myStatusTextView.setText("Payload receiving");
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
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