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
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_CODE = 1240;
    private String[] appPermissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exitApp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_CODE){
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for (int i=0; i<grantResults.length; i++){
                if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }
            if (deniedCount > 0){
                exitApp();
            }
        }
    }

    public void checkAndRequestPermissions(){
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm:appPermissions){
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(perm);
            }
        }
        if (!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    MY_PERMISSIONS_CODE);
        }
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
    }

    public void exitApp () {
        finishAffinity();
        System.exit(0);
    }
}

