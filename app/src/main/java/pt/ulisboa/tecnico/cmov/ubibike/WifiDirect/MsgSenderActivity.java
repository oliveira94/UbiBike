package pt.ulisboa.tecnico.cmov.ubibike.WifiDirect;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.ulisboa.tecnico.cmov.ubibike.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MsgSenderActivity extends Activity implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;
    private TextView mTextInput;
    private TextView mTextOutput;
    private SimWifiP2pBroadcastReceiver mReceiver;
    MsgSenderActivity msgSenderActivity;
    String IP = "";
    SimWifiP2pDeviceList devices;
    SimWifiP2pInfo groupInfo;

    //list with the devices in the neighbor
    List listOfDevices = new ArrayList();
    List listOfIPs = new ArrayList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the UI
        setContentView(R.layout.activity_chat);
        guiSetButtonListeners();
        guiUpdateInitState();

        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        // mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);
        StartWifi();

    }
    //test
    public void updateRange(View v){

        if (mBound) {
            mManager.requestGroupInfo(mChannel, MsgSenderActivity.this);
        } else {
            Toast.makeText(v.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
        if (mBound) {
            mManager.requestPeers(mChannel, MsgSenderActivity.this);
        } else {
            Toast.makeText(v.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }

        //devices.getDeviceList();

        //updateRangeAndNetwork( mChannel, groupInfo);

    }

    //TODO the problem is the arguments are null
    public void updateRangeAndNetwork(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo){
        //StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {

            SimWifiP2pDevice device = devices.getByName(deviceName);
            //String devstr = "" + deviceName + " (" +
            //        ((device == null)?"??":device.getVirtIp()) + ")\n";
            //peersStr.append(devstr);
            AddDevicesNameToList(deviceName);
            AddDeviceIPToList(device.getVirtIp());
            GetDeviceIP(deviceName);
        }
    }

    public void StartWifi(){
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);

        guiUpdateDisconnectedState();
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

	/*
	 * Listeners associated to buttons
	 */

    //not using now
    private View.OnClickListener listenerWifiOnButton = new View.OnClickListener() {
        public void onClick(View v){

            Intent intent = new Intent(v.getContext(), SimWifiP2pService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;

            // spawn the chat server background task
            new IncommingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
            guiUpdateDisconnectedState();
        }
    };


    private View.OnClickListener listenerWifiOffButton = new View.OnClickListener() {
        public void onClick(View v){
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
                guiUpdateInitState();
            }
        }
    };

    private View.OnClickListener listenerInRangeButton = new View.OnClickListener() {
        public void onClick(View v){
            if (mBound) {
                mManager.requestPeers(mChannel, MsgSenderActivity.this);
            } else {
                Toast.makeText(v.getContext(), "Service not bound",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener listenerInGroupButton = new View.OnClickListener() {
        public void onClick(View v){
            if (mBound) {
                mManager.requestGroupInfo(mChannel, MsgSenderActivity.this);
            } else {
                Toast.makeText(v.getContext(), "Service not bound",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

//    private View.OnClickListener listenerConnectButton = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            findViewById(R.id.idConnectButton).setEnabled(false);
//            new OutgoingCommTask().executeOnExecutor(
//                    AsyncTask.THREAD_POOL_EXECUTOR,
//                    mTextInput.getText().toString()
//                   // GetDeviceIP(mTextInput.getText().toString()).toString()
//            );
//        }
//    };

    private View.OnClickListener listenerSendButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findViewById(R.id.idSendButton).setEnabled(false);

            new SendCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    mTextInput.getText().toString());

        }
    };

    private View.OnClickListener listenerDisconnectButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findViewById(R.id.idDisconnectButton).setEnabled(false);
            if (mCliSocket != null) {
                try {
                    mCliSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCliSocket = null;
            guiUpdateDisconnectedState();
        }
    };

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

	/*
	 * Asynctasks implementing message exchange
	 */

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        10001);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    try {
                        BufferedReader sockIn = new BufferedReader(
                                new InputStreamReader(sock.getInputStream()));
                        String st = sockIn.readLine();
                        publishProgress(st);
                        sock.getOutputStream().write(("\n").getBytes());
                    } catch (IOException e) {
                        Log.d("Error reading socket:", e.getMessage());
                    } finally {
                        sock.close();
                    }
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mTextOutput.append(values[0] + "\n");
        }
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            mTextOutput.setText("Connecting...");
//        }
//
        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0],
                        10001);
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }
//
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                guiUpdateDisconnectedState();
                mTextOutput.setText(result);
            } else {
                findViewById(R.id.idDisconnectButton).setEnabled(true);
                findViewById(R.id.idConnectButton).setEnabled(false);
                findViewById(R.id.idSendButton).setEnabled(true);
                mTextInput.setHint("");
                mTextInput.setText("");
                mTextOutput.setText("");
            }
        }
    }

    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            try {

                mCliSocket = new SimWifiP2pSocket(IP, 10001);
                mCliSocket.getOutputStream().write((msg[0] + "\n").getBytes());
                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                sockIn.readLine();
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
           mTextInput.setText("");
            guiUpdateDisconnectedState();
            if (result != null) {
                guiUpdateDisconnectedState();
                mTextOutput.setText("sfsdfsdfsdfsdfdsfs");//get message
            } else {
                findViewById(R.id.idDisconnectButton).setEnabled(true);
                findViewById(R.id.idConnectButton).setEnabled(false);
                findViewById(R.id.idSendButton).setEnabled(true);
                mTextInput.setHint("");
                mTextInput.setText("");
                mTextOutput.setText("");
            }
        }
    }

	/*
	 * Listeners associated to Termite
	 */

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        StringBuilder peersStr = new StringBuilder();

        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = "" + device.deviceName + " (" + device.getVirtIp() + ")\n";
            peersStr.append(devstr);
        }

        // display list of devices in range
//        new AlertDialog.Builder(this)
//                .setTitle("Devices in WiFi Range")
//                .setMessage(peersStr.toString())
//                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                })
//                .show();
    }

    public void AddDeviceIPToList(String IP){
        if (!listOfIPs.contains(IP)) {
            listOfIPs.add(IP);
            Toast.makeText(this, listOfIPs.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void GetDeviceIP(String devicename){
        int positonInList = listOfDevices.indexOf(devicename);
        IP = String.valueOf(listOfIPs.get(positonInList));
    }


    public void AddDevicesNameToList(String device){

        if (!listOfDevices.contains(device)) {
            listOfDevices.add(device);
            Toast.makeText(this, listOfDevices.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {
        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {

            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
            AddDevicesNameToList(deviceName);
            AddDeviceIPToList(device.getVirtIp());
            GetDeviceIP(deviceName);
        }

        // display list of network members
//        new AlertDialog.Builder(this)
//                .setTitle("Devices in WiFi Network")
//                .setMessage(peersStr.toString())
//                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                })
//                .show();
    }

	/*
	 * Helper methods for updating the interface
	 */

    private void guiSetButtonListeners() {

        //findViewById(R.id.idConnectButton).setOnClickListener(listenerConnectButton);
        findViewById(R.id.idDisconnectButton).setOnClickListener(listenerDisconnectButton);
        findViewById(R.id.idSendButton).setOnClickListener(listenerSendButton);
       // findViewById(R.id.idWifiOnButton).setOnClickListener(listenerWifiOnButton);
        findViewById(R.id.idWifiOffButton).setOnClickListener(listenerWifiOffButton);
        findViewById(R.id.idInRangeButton).setOnClickListener(listenerInRangeButton);
        findViewById(R.id.idInGroupButton).setOnClickListener(listenerInGroupButton);
    }

    private void guiUpdateInitState() {

        mTextInput = (TextView) findViewById(R.id.input);
        mTextInput.setHint("type remote virtual IP (192.168.0.0/16)");
        mTextInput.setEnabled(false);

        mTextOutput = (TextView) findViewById(R.id.message);
        mTextOutput.setEnabled(false);
        mTextOutput.setText("");

        findViewById(R.id.idConnectButton).setEnabled(false);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
        findViewById(R.id.idSendButton).setEnabled(true);
        findViewById(R.id.idWifiOnButton).setEnabled(true);
        findViewById(R.id.idWifiOffButton).setEnabled(false);
        findViewById(R.id.idInRangeButton).setEnabled(false);
        findViewById(R.id.idInGroupButton).setEnabled(false);
    }

    private void guiUpdateDisconnectedState() {

        mTextInput.setEnabled(true);
        mTextInput.setHint("type remote virtual IP (192.168.0.0/16)");
        mTextOutput.setEnabled(true);
        mTextOutput.setText("");

        findViewById(R.id.idSendButton).setEnabled(true);
        findViewById(R.id.idConnectButton).setEnabled(true);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
        findViewById(R.id.idWifiOnButton).setEnabled(false);
        findViewById(R.id.idWifiOffButton).setEnabled(true);
        findViewById(R.id.idInRangeButton).setEnabled(true);
        findViewById(R.id.idInGroupButton).setEnabled(true);
    }
}
