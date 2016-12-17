package com.example.shane.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainActivity extends Activity {

    public long millSecondsOnClock = 0;

    private CountDownTimer tmr;

    private Status status = Status.NEW;

    public static enum Status {
        NEW(false, false, false), RUNNING(true, true, false), STOPPED(true, false, true);

        private final boolean enableReset;
        private final boolean enableStop;
        private final boolean enableRestart;

        Status(boolean enableReset, boolean enableStop, boolean enableRestart) {
            this.enableReset = enableReset;
            this.enableStop = enableStop;
            this.enableRestart = enableRestart;
        }
    }

    private void allignGUIWithStatus(){
        if (status.enableReset) enableResetButton();
            else disableResetButton();
        if (status.enableStop) enableStopButton();
            else disableStopButton();
        if (status.enableRestart) showRestartButton();
            else showStopButton();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allignGUIWithStatus();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        bundle.putInt("Status", status.ordinal());
        bundle.putLong("MillSecondsOnClock", millSecondsOnClock);
        if (tmr != null) tmr.cancel();
    }

    public void onRestoreInstanceState(Bundle bundle) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(bundle);
        status = Status.values()[bundle.getInt("Status")];
        allignGUIWithStatus();
        long millSecondsOnClock = bundle.getLong("MillSecondsOnClock");
        this.millSecondsOnClock = millSecondsOnClock;
        updateCounter();
        if (status == Status.RUNNING) restartCounter(getWindow().getDecorView().findViewById(android.R.id.content));
    }

    private void updateCounter(){
        EditText counter = (EditText) findViewById(R.id.counter);
        counter.setText("" + millSecondsOnClock / 1000);
    }

    public void createTimer(long time){
        if (tmr != null) tmr.cancel();
        tmr = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                millSecondsOnClock = millisUntilFinished;
                updateCounter();
            }

            public void onFinish() {
                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(500);
                View view = getWindow().getDecorView().findViewById(android.R.id.content);
                resetCounter(view);
                showAlarm(view);

            }
        };
    }


    public void showAlarm(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setTitle("Timer");
        alertDialogBuilder
                .setMessage("Time Up!")
                .setCancelable(false)
                .setNeutralButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void add30Seconds(View view){
        millSecondsOnClock = millSecondsOnClock + 30000;
        createTimer(millSecondsOnClock);
        tmr.start();
        status = Status.RUNNING;
        allignGUIWithStatus();
    }

    public void stopCounter(View view){
        tmr.cancel();
        status = Status.STOPPED;
        allignGUIWithStatus();
    }

    public void resetCounter(View view){
        millSecondsOnClock = 0;
        createTimer(millSecondsOnClock);
        updateCounter();
        status = Status.NEW;
        allignGUIWithStatus();
    }

    public void restartCounter(View view) {
        createTimer(millSecondsOnClock);
        tmr.start();
        status = Status.RUNNING;
        allignGUIWithStatus();
    }


    public void showRestartButton(){
        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setVisibility(View.GONE);
        Button startButton = (Button) findViewById(R.id.restartButton);
        startButton.setVisibility(View.VISIBLE);
    }

    public void disableStopButton(){
        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setEnabled(false);
    }

    public void enableStopButton(){
        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setEnabled(true);
    }

    public void disableResetButton(){
        Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setEnabled(false);
    }

    public void enableResetButton(){
        Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setEnabled(true);
    }


    public void showStopButton(){
        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setVisibility(View.VISIBLE);
        Button startButton = (Button) findViewById(R.id.restartButton);
        startButton.setVisibility(View.GONE);
    }


    /**
     * A placeholder fragment containing a simple view. This fragment
     * would include your content.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
            return rootView;
        }
    }


    /**
     * This class makes the ad request and loads the ad.
     */
    public static class AdFragment extends Fragment {

        private AdView mAdView;

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
            // values/strings.xml.
            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }

    }

}
