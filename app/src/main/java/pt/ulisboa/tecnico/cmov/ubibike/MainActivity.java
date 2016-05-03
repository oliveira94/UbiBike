package pt.ulisboa.tecnico.cmov.ubibike;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
//import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.MsgSenderActivity;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class MainActivity extends AppCompatActivity implements SimWifiP2pManager.GroupInfoListener, SimWifiP2pManager.PeerListListener {

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    public boolean mBound = false;
    private SimWifiP2pBroadcastReceiver mReceiver;

    List listOfDevices = new ArrayList();
    List listOfIPs = new ArrayList();
    String IP = "";
    String receiver = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    public void SignInClicked(View view) {
        Intent i = new Intent(this, LogIn.class);
        //Intent intent = new Intent(view.getContext(), SimWifiP2pService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
//        Intent intent = new Intent(this, SimWifiP2pBroadcastReceiver.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(mReceiver);
    }

    public void SignUpClicked(View view) {
        Intent i = new Intent(this, CreateAccount.class);
//        Intent intent = new Intent(view.getContext(), SimWifiP2pService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startActivity(i);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };

    public void OnlineMode(View view) {
        if (mBound) {
            mManager.requestGroupInfo(mChannel, MainActivity.this);
        } else {
            Toast.makeText(view.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
        if (mBound) {
            mManager.requestPeers(mChannel, MainActivity.this);
        } else {
            Toast.makeText(view.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {

            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null) ? "??" : device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
            ((UserData) this.getApplication()).AddDevicesNameToList(deviceName);
            //AddDevicesNameToList(deviceName);//TODO
            ((UserData) this.getApplication()).AddDeviceIPToList(device.getVirtIp());
            //AddDeviceIPToList(device.getVirtIp());
            ((UserData) this.getApplication()).GetDeviceIP(deviceName);
            //GetDeviceIP(deviceName);
            ((UserData) this.getApplication()).GetName(((UserData) this.getApplication()).getIP());
           //GetName(IP);
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        StringBuilder peersStr = new StringBuilder();

        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = "" + device.deviceName + " (" + device.getVirtIp() + ")\n";
            peersStr.append(devstr);
        }
    }

//    //add a IP to a list of the IP in the network
//    public void AddDeviceIPToList(String IP){
//        if (!listOfIPs.contains(IP)) {
//            listOfIPs.add(IP);
//            Toast.makeText(this, listOfIPs.toString(),
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void AddDevicesNameToList(String device){
//
//        if (!listOfDevices.contains(device)) {
//            listOfDevices.add(device);
//            Toast.makeText(this, listOfDevices.toString(),
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    //get Ip through device name
//    public void GetDeviceIP(String devicename){
//        int positonInList = listOfDevices.indexOf(devicename);
//        IP = String.valueOf(listOfIPs.get(positonInList));
//    }
//
//    //get name device through ip
//    public void GetName(String ip){
//        int positonInList = listOfIPs.indexOf(ip);
//        receiver = String.valueOf(listOfDevices.get(positonInList));
//    }
}
