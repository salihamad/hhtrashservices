package com.hhtrashservices.www.hhtrashservices;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MyActivity extends Activity implements
        DataApi.DataListener, GooglePlayServicesClient.OnConnectionFailedListener, GooglePlayServicesClient.ConnectionCallbacks, MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MyActivity.class.getSimpleName();

    //private TalkClient mTalkClient;
    //ListenerServiceMobile mListenerServiceMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //mListenerServiceMobile = new ListenerServiceMobile();


        //mTalkClient = new TalkClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mTalkClient.connectClient();
    }

    @Override
    protected void onDestroy() {
        //mTalkClient.disconnectClient();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Connected to Google Api Service");
        }
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onDisconnected()
    {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents)
    {

        Intent intent = new Intent(MyActivity.this, MyActivity.class);
        startActivity(intent);
        /*for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());
            }
        }*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent)
    {

        /*
        This method apparently runs in a background thread.
         */
        Intent intent = new Intent(MyActivity.this, MyActivity.class);
        startActivity(intent);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().endsWith("connected")){

                }
                else{
                }
            }
        });

        Log.v(TAG, "Message received on wear: " + messageEvent.getPath());
    }
}
