package com.qeh.lyubo.qeh;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.Task;

public class AdvertiserActivity extends AppCompatActivity {
    QnEhUser cUser;
    boolean mShouldContinue = true;
    final int SAMPLE_RATE = 44100; // The sampling rate
    byte[] audioBuffer;
    // buffer size in bytes
    int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    TextView myStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertiser);

        myStatusTextView = (TextView)findViewById(R.id.statusView);
        myStatusTextView.setText("Waiting to be connected ...");

        audioBuffer = new byte[bufferSize / 2];
        cUser = ((QnEhApplication) this.getApplication()).getUser();
        startAdvertising();
    }

    public void startAdvertising(){
        Nearby.getConnectionsClient(this)
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
                    final Task<Void> voidTask = Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            myStatusTextView.setText("You are now connected!");
                            if (cUser.getUserType() == Constants.USER_ADVERTISER){
                                recordAudio(endpointId);
                                //mShouldContinue = true;
                            }
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
    void recordAudio(final String endpointId) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e("Audio Service", "Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                while (mShouldContinue) {
                    //myStatusTextView.setText("Sending bytes 1");
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                    Payload bytesPayload = Payload.fromBytes(audioBuffer);
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpointId, bytesPayload);
                    //int numberOfShortwrite = audioTrack.write(audioBuffer, 0, audioBuffer.length);

                }
                record.stop();
                record.release();
            }
        }).start();
    }

}
