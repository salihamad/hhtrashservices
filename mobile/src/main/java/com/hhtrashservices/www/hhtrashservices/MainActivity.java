package com.hhtrashservices.www.hhtrashservices;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.parse.ParseUser;


public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener,
        GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */

    SectionsPagerAdapter mSectionsPagerAdapter;
    private GoogleApiClient mGoogleApiClient;
    private int mShortAnimationDuration;
    private View progress;
    private View onOffView;
    private Node peerNode;  //  There's normally only one.
    //ListenerServiceMobile mListenerServiceMobile;

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    //private TalkClient mTalkClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //mTalkClient = new TalkClient(this);

        //  Is needed for communication between the wearable and the device.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        ParseUser currentUser = ParseUser.getCurrentUser();

        if(currentUser == null) {
            navigateToLogin();
        }
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mSectionsPagerAdapter = new SectionsPagerAdapter(this,getFragmentManager());
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "connected to Google Play Services on Wear!");
        Wearable.MessageApi.addListener(mGoogleApiClient, this).setResultCallback(resultCallback);
    }

    /**
     * Not needed, but here to show capabilities. This callback occurs after the MessageApi
     * listener is added to the Google API Client.
     */
    private ResultCallback<Status> resultCallback =  new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            Log.v(TAG, "Status: " + status.getStatus().isSuccess());
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    sendStartMessage();
                    return null;
                }
            }.execute();
        }
    };

    protected void sendStartMessage(){

        NodeApi.GetConnectedNodesResult rawNodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (final Node node : rawNodes.getNodes()) {
            Log.v(TAG, "Node: " + node.getId());


            PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                    mGoogleApiClient,
                    node.getId(),
                    "/start",
                    null
            );

            result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    //  The message is done sending.
                    //  This doesn't mean it worked, though.
                    Log.v(TAG, "Our callback is done.");
                    peerNode = node;    //  Save the node that worked so we don't have to loop again.
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

        /*
        This method apparently runs in a background thread.
         */

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

    private void navigateToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if(itemId == R.id.action_logout){
            ParseUser.logOut();
            navigateToLogin();
        }
        else if(itemId  == R.id.action_apartment){
            Intent intent = new Intent(this, SelectApartmentActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }


    }

}
