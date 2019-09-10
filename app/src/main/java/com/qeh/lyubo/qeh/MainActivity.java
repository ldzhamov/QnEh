package com.qeh.lyubo.qeh;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    final int SAMPLE_RATE = 44100; // The sampling rate
    byte[] audioBuffer;
    byte[] audioBuffer1;
    boolean mShouldContinuePlay = false;
    boolean mShouldContinue = true;
    boolean discoverer = false;
    // buffer size in bytes
    int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    TextView myStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSIONS_RECORD_AUDIO);
        audioBuffer = new byte[bufferSize / 2];
        audioBuffer1 = new byte[bufferSize / 2];
        //recordAudio();
        //playAudio();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myStatusTextView = (TextView)findViewById(R.id.status_connectivity);
        myStatusTextView.setText("Initial state");
    }

    public void startSpeaking(View view) {

        //When permission is not granted by user, show them message why this permission is needed.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

            //Give user option to still opt-in the permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);

        } else {
            // Show user dialog to grant permission to record audio
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
        }
        //recordAudio();
        mShouldContinue = true;
        mShouldContinuePlay = false;
        Log.e("Main activity", "Start speakin1");
    }

    public void stopSpeaking(View view) {
        mShouldContinue = false;
        mShouldContinuePlay = true;
        //Intent stopServiceIntent = new Intent(MainActivity.this, AudioService.class);
        //stopService(stopServiceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Toast.makeText(this, "Permissions Granted to record audio", Toast.LENGTH_LONG).show();
                    //Intent startServiceIntent = new Intent(MainActivity.this, AudioService.class);
                    //startService(startServiceIntent);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }



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

//                AudioTrack audioTrack = new AudioTrack(
//                        AudioManager.STREAM_VOICE_CALL,
//                        SAMPLE_RATE,
//                        AudioFormat.CHANNEL_OUT_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT,
//                        bufferSize,
//                        AudioTrack.MODE_STREAM);
//
//                audioTrack.play();

                Log.v("Audio service", "Start recording");

                long shortsRead = 0;
                while (mShouldContinue) {
                    //myStatusTextView.setText("Sending bytes 1");
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                    Payload bytesPayload = Payload.fromBytes(audioBuffer);
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpointId, bytesPayload);
                    //int numberOfShortwrite = audioTrack.write(audioBuffer, 0, audioBuffer.length);

                    shortsRead += numberOfShort;
                    Log.e("Audio Service", "Shorts read "+String.valueOf(shortsRead));
                }

                record.stop();
                record.release();
            }
        }).start();
    }

    void playAudio() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();

                Log.v("Audio Service", "Audio streaming started");
                int shortsWrite = 0;
                while (mShouldContinuePlay) {

                    int numberOfShort = audioTrack.write(audioBuffer1, 0, audioBuffer1.length);
                    shortsWrite += numberOfShort;
                    Log.e("Audio Service", "Shorts write "+String.valueOf(shortsWrite));
                }

                if (!mShouldContinuePlay) {
                    audioTrack.release();
                }

                Log.v("Audio Service", "Audio streaming finished. Samples written: ");
            }
        }).start();
    }

    public void startAdvertising(View view) {
        EditText nameInput = (EditText) findViewById(R.id.nameInput);
        ((QnEhApplication) this.getApplication()).setUser(new QnEhUser(nameInput.getText().toString(), Constants.USER_ADVERTISER, null));
        Intent aIntent = new Intent(MainActivity.this, AdvertiserActivity.class);
        MainActivity.this.startActivity(aIntent);
    }

    public void startDiscovering(View view) {
        EditText nameInput = (EditText) findViewById(R.id.nameInput);
        ((QnEhApplication) this.getApplication()).setUser(new QnEhUser(nameInput.getText().toString(), Constants.USER_ADVERTISER, null));
        Intent aIntent = new Intent(MainActivity.this, DiscovererActivity.class);
        MainActivity.this.startActivity(aIntent);
//        discoverer = true;
//        Nearby.getConnectionsClient(this)
//                .startDiscovery(
//                        /* serviceId= */ "com.lyubo.qandeh",
//                        mEndpointDiscoveryCallback,
//                        new DiscoveryOptions(Strategy.P2P_CLUSTER));
//        myStatusTextView.setText("Started discovering");
    }

    EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            myStatusTextView.setText("Found endpoint: " + s);
            Nearby.getConnectionsClient(getApplicationContext())
                    .requestConnection(
                            /* endpointName= */ "Device B",
                            s,
                            mConnectionLifecycleCallback);
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            myStatusTextView.setText("Lost endpoint: " + s);
        }
    };

    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                    myStatusTextView.setText("Receiving bytes 1");
                    audioBuffer1 = payload.asBytes();

                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    // Payload progress has updated.
                }
            };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    myStatusTextView.setText("Connection initiated");
                    final Task<Void> voidTask = Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            myStatusTextView.setText("Status ok");
                            if (discoverer){
                                myStatusTextView.setText("Receiveing bytes");
                                playAudio();
                                mShouldContinuePlay = true;
                            }
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            myStatusTextView.setText("Rejected");
                            break;
                        default:
                            myStatusTextView.setText("Defaulted");
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    myStatusTextView.setText("Diconnected");
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                }
            };
    }

