package pt.ulisboa.tecnico.cmov.ubibike.Activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class MainActivity extends AppCompatActivity{

    private SimWifiP2pBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());
    }

    public void SignInClicked(View view) {
        Intent i = new Intent(this, LogIn.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void SignUpClicked(View view) {
        Intent i = new Intent(this, CreateAccount.class);
        startActivity(i);
    }

}
