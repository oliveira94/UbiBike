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
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.MsgSenderActivity;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class Chat extends Activity implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener{

    SQLiteDatabase db;
    DataBaseHelper helper = new DataBaseHelper(this);
    ExchangeMessages exchangeMessages = new ExchangeMessages();
    String user = "";
    String receiver = "";
    public static final String TAG = "msgsender";

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;
    private TextView mTextInput;
    private TextView mTextOutput ;
    private SimWifiP2pBroadcastReceiver mReceiver;

    String IP = "";
    SimWifiP2pDeviceList devices;
    SimWifiP2pInfo groupInfo;

    //list with the devices in the neighbor
    List listOfDevices = new ArrayList();
    List listOfIPs = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TextView ReceiverName = (TextView)findViewById(R.id.bookBikeText);
        ReceiverName.setText("joao");//TODO put where the name of the receiver

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            user = extras.getString("USER");
        }

        mTextInput = (TextView)findViewById(R.id.textEntryChat);

        mTextOutput = (TextView) findViewById(R.id.output);
        mTextOutput.setText("");

        StartWifi();



        updateMessages();

        Toast toast = Toast.makeText(Chat.this, user, Toast.LENGTH_SHORT);
        toast.show();

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);



    }

    public void sendClickedChat(View view) {

        // spawn the chat server background task
        new OutgoingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                mTextInput.getText().toString());

        new SendCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                mTextInput.getText().toString());//TODO change the variable

        LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
        LinearLayout chatHorizontalLayout = new LinearLayout(this);

        //Moving the text to the new text box
        TextView chatText = new TextView(this);
        EditText entryText = (EditText) findViewById(R.id.textEntryChat);
        String text = entryText.getText().toString();

        //update the exchangeMessages
        exchangeMessages.setSender(user);
        exchangeMessages.setMessage(text);
        exchangeMessages.setReceiver(receiver);

        //put the message in the database
        helper.sendNewMessage(exchangeMessages);

        chatText.setText(text);
        chatText.setTextSize(22);
        chatText.setTextColor(Color.BLACK);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        //Setting the parameters to the intended
        chatHorizontalLayout.setGravity(Gravity.RIGHT);

        //Adding the textView to the HorizontalLayout
        chatHorizontalLayout.addView(chatText, params);

        //Adding the whole HorizontalLayout to the VerticalLayout
        linearLayoutVertical.addView(chatHorizontalLayout);
    }

    public void UpdateOtherUserScreen(String message, String username){
        LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
        LinearLayout chatHorizontalLayout = new LinearLayout(this);

        //Moving the text to the new text box
        TextView chatText = new TextView(this);
        EditText entryText = (EditText) findViewById(R.id.textEntryChat);
        String text = entryText.getText().toString();

        //TODO where user is the message
        //update the exchangeMessages
        exchangeMessages.setSender(username);
        exchangeMessages.setMessage(message);
        exchangeMessages.setReceiver(user);
        Toast toast = Toast.makeText(Chat.this,"sender: " +  username, Toast.LENGTH_SHORT);
        toast.show();
        Toast toast1 = Toast.makeText(Chat.this,"message: " +  message, Toast.LENGTH_SHORT);
        toast1.show();
        Toast toast2 = Toast.makeText(Chat.this, "reveiver: " +  user, Toast.LENGTH_SHORT);
        toast2.show();

        //put the message in the database
        helper.sendNewMessage(exchangeMessages);

        chatText.setText(message);
        chatText.setTextSize(22);
        chatText.setTextColor(Color.BLACK);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        //Setting the parameters to the intended
        chatHorizontalLayout.setGravity(Gravity.LEFT);

        //Adding the textView to the HorizontalLayout
        chatHorizontalLayout.addView(chatText, params);

        //Adding the whole HorizontalLayout to the VerticalLayout
        linearLayoutVertical.addView(chatHorizontalLayout);
    }

    public void updateMessages(){//TODO update to receive user

        //Moving the text to the new text box
        //TextView chatText = new TextView(this);
        EditText entryText = (EditText) findViewById(R.id.textEntryChat);
        String text = entryText.getText().toString();

        //update the exchangeMessages
//        exchangeMessages.setSender(user);
//        exchangeMessages.setMessage(text);
//        exchangeMessages.setReceiver("joao");

        //put the message in the database
        //helper.sendNewMessage(exchangeMessages);

        db = helper.getReadableDatabase();
        String query1 = "select sender, receiver, message from mychat";
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String sender, message;

        if(cursor1.moveToFirst()){

            do{

                LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
                LinearLayout chatHorizontalLayout = new LinearLayout(this);

                sender = cursor1.getString(0);

                receiver = cursor1.getString(1);

                message = cursor1.getString(2);
                TextView chatText = new TextView(this);
                chatText.setText(message);
                chatText.setTextSize(22);
                chatText.setTextColor(Color.BLACK);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                String isSender = SenderOrReceiver(sender);
                if(isSender.equals("sender")){
                    chatHorizontalLayout.setGravity(Gravity.RIGHT);
                }else {//TODO where we need to put a else if where the receiver will be us, and if yes, will print
                    chatHorizontalLayout.setGravity(Gravity.LEFT);
                }
                chatHorizontalLayout.addView(chatText, params);
                linearLayoutVertical.addView(chatHorizontalLayout);
            }
            while (cursor1.moveToNext() && cursor1.getString(0) != "");
        }
        Toast toast = Toast.makeText(Chat.this, "finish", Toast.LENGTH_SHORT);
        toast.show();
    }

    public String SenderOrReceiver(String sender) {
        String WhoIs = null;
        if (user.equals(sender)) {
            WhoIs = "sender";
        }
        else{
            WhoIs ="";
        }
        return WhoIs;
    }

    public void UpdateButton(View v){
        if (mBound) {
            mManager.requestGroupInfo(mChannel, Chat.this);
        } else {
            Toast.makeText(v.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
        if (mBound) {
            mManager.requestPeers(mChannel, Chat.this);
        } else {
            Toast.makeText(v.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void StartWifi(){
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

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
            String[] result = values[0].split(":");

            //where i have the message that will be sent to the other user
            UpdateOtherUserScreen(result[1], result[0]);
        }
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {
        //
        @Override
        protected void onPreExecute() {
            mTextOutput.setText("Connecting...");

        }

        @Override
        protected String doInBackground(String... params) {
//            try {
//                mCliSocket = new SimWifiP2pSocket(params[0],
//                        10001);
//            } catch (UnknownHostException e) {
//                return "Unknown Host:" + e.getMessage();
//            } catch (IOException e) {
//                return "IO error:" + e.getMessage();
//            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //guiUpdateDisconnectedState();
                //updateMessages(result);//TODO see thisssss
                mTextOutput.setText(result);
            } else {
//                findViewById(R.id.idDisconnectButton).setEnabled(true);
//                findViewById(R.id.idConnectButton).setEnabled(false);
//                findViewById(R.id.idSendButton).setEnabled(true);
//                mTextInput.setHint("");
//                mTextInput.setText("");
//                mTextOutput.setText("");
            }
        }
    }

    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            try {
                mCliSocket = new SimWifiP2pSocket(IP, 10001);
                mCliSocket.getOutputStream().write((user + ":" + msg[0] + "\n").getBytes());
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
            //guiUpdateDisconnectedState();
            if (result != null) {
                //guiUpdateDisconnectedState();
                //updateMessages("fdfdfdfd");
                mTextOutput.setText("sfsdfsdfsdfsdfdsfs");//get message

            } else {
//                findViewById(R.id.idDisconnectButton).setEnabled(true);
//                findViewById(R.id.idConnectButton).setEnabled(false);
//                findViewById(R.id.idSendButton).setEnabled(true);
//                mTextInput.setHint("");
//                mTextInput.setText("");
//                mTextOutput.setText("");
            }
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {

            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
            AddDevicesNameToList(deviceName);
            AddDeviceIPToList(device.getVirtIp());
            GetDeviceIP(deviceName);
            GetName(IP);
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

    public void GetName(String ip){
        int positonInList = listOfIPs.indexOf(ip);
        receiver = String.valueOf(listOfDevices.get(positonInList));
    }

    public void AddDevicesNameToList(String device){

        if (!listOfDevices.contains(device)) {
            listOfDevices.add(device);
            Toast.makeText(this, listOfDevices.toString(),
                    Toast.LENGTH_SHORT).show();
        }
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
}
