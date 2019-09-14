package com.qeh.lyubo.qeh;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

public class AdvertiserActivity extends AppCompatActivity {
    QnEhUser cUser;
    boolean mShouldContinue = true;
    byte[] audioBuffer;
    private ConnectionsClient mConnectionsClient;
    private AudioRecorder mRecorder;

    TextView myStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertiser);

        myStatusTextView = (TextView)findViewById(R.id.statusView);
        myStatusTextView.setText("Waiting to be connected ...");

        cUser = ((QnEhApplication) this.getApplication()).getUser();

        mConnectionsClient = Nearby.getConnectionsClient(this);

        startAdvertising();
    }

    @Override
    public void onBackPressed() {
        mConnectionsClient.stopAdvertising();
        Intent nIntent = new Intent(AdvertiserActivity.this, MainActivity.class);
        startActivity(nIntent);
    }

    public void startAdvertising(){
        mConnectionsClient
                .startAdvertising(
                        /* endpointName= */ cUser.getName(),
                        /* serviceId= */ Constants.ServiceId,
                        mConnectionLifecycleCallback,
                        new AdvertisingOptions(Strategy.P2P_CLUSTER));
    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                final Task<Void> voidTask = mConnectionsClient.acceptConnection(endpointId, mPayloadCallback);
            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                switch (result.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        myStatusTextView.setText("You are now connected!");
                        startRecording(endpointId);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onDisconnected(String endpointId) {
                myStatusTextView.setText("Disconnected!");
            }
        };

    private void startRecording(String endpointId) {
        try {
            ParcelFileDescriptor[] payloadPipe = ParcelFileDescriptor.createPipe();

            // Send the first half of the payload (the read side) to Nearby Connections.
            send(Payload.fromStream(payloadPipe[0]), endpointId);

            // Use the second half of the payload (the write side) in AudioRecorder.
            mRecorder = new AudioRecorder(payloadPipe[1]);
            mRecorder.start();
        } catch (IOException e) {
            Log.e("Advertiser activity ", "Exception pipe");
        }
    }

    private void send(Payload payload, String endpoint) {
        mConnectionsClient
            .sendPayload(endpoint, payload)
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {

                    }
                });
        myStatusTextView.setText("recording!!!");
    }

    private final PayloadCallback mPayloadCallback =
        new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                //myStatusTextView.setText("Payload receiving");
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            }

            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                // Payload progress has updated.
            }
        };
}
