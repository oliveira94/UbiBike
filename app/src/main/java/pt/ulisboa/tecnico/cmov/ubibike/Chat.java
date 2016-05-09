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
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class Chat extends Activity{

    SQLiteDatabase db;
    DataBaseHelper helper = new DataBaseHelper(this);
    ExchangeMessages exchangeMessages = new ExchangeMessages();
    String user = "";
    String receiver = "";
    String IP = "";
    int port = 10001;
    String receiverFromMessages = "";

    //false if is message, true if is points
    boolean MessageOrPoints = false;

    public static final String TAG = "msgsender";
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;
    private TextView mTextInput;
    private TextView pointInput;
    private SimWifiP2pBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get the username
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            user = extras.getString("USER");
            receiverFromMessages = extras.getString("RECEIVER");
        }

        TextView ReceiverName = (TextView)findViewById(R.id.bookBikeText);
        ReceiverName.setText(receiverFromMessages);

        mTextInput = (TextView)findViewById(R.id.textEntryChat);
        pointInput = (TextView)findViewById(R.id.entrypoints);
        //mTextOutput = (TextView) findViewById(R.id.output);
        //mTextOutput.setText("");

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

        //print all messages that are in the database
        updateMessages();

        IP = UserData.IP;

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void sendClickedChat(View view) {

        MessageOrPoints = false;

        // spawn the chat server background task
        new OutgoingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                mTextInput.getText().toString());

        new SendCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                mTextInput.getText().toString());

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

        //update the exchangeMessages
        exchangeMessages.setSender(username);
        exchangeMessages.setMessage(message);
        exchangeMessages.setReceiver(user);

        Toast toast = Toast.makeText(Chat.this, "sender: " + username, Toast.LENGTH_SHORT);
        toast.show();
        Toast toast1 = Toast.makeText(Chat.this,"message: " +  message, Toast.LENGTH_SHORT);
        toast1.show();
        Toast toast2 = Toast.makeText(Chat.this, "receiver: " +  user, Toast.LENGTH_SHORT);
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

    public void updateMessages() {

        db = helper.getReadableDatabase();
        String query1 = "select sender, receiver, message from mychat";
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String sender, message;


        if (cursor1.moveToFirst()) {
            do {
                sender = cursor1.getString(0);
                receiver = cursor1.getString(1);
                message = cursor1.getString(2);

                if (sender.equals(receiverFromMessages)) {
                    LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
                    LinearLayout chatHorizontalLayout = new LinearLayout(this);

                    TextView chatText = new TextView(this);
                    chatText.setText(message);
                    chatText.setTextSize(22);
                    chatText.setTextColor(Color.BLACK);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                    chatHorizontalLayout.setGravity(Gravity.LEFT);
                    chatHorizontalLayout.addView(chatText, params);
                    linearLayoutVertical.addView(chatHorizontalLayout);
                } else if (receiver.equals(user)) {

                    LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
                    LinearLayout chatHorizontalLayout = new LinearLayout(this);

                    TextView chatText = new TextView(this);
                    chatText.setText(message);
                    chatText.setTextSize(22);
                    chatText.setTextColor(Color.BLACK);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                    chatHorizontalLayout.setGravity(Gravity.RIGHT);
                    chatHorizontalLayout.addView(chatText, params);
                    linearLayoutVertical.addView(chatHorizontalLayout);
                }
            }
            while (cursor1.moveToNext() && cursor1.getString(0) != "");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void sendPointsClicked(View view) {

        MessageOrPoints = true;
        //Moving the text to the new text box
        TextView chatText = new TextView(this);
        EditText entryText = (EditText) findViewById(R.id.entrypoints);
        String pointstext = entryText.getText().toString();

        int points = 0;
        if(isNumber(pointstext)){
            points = Integer.parseInt(pointstext);
        }



        if(UserData.points >= points && isNumber(pointstext)){
            UserData.points -= points;
            helper.ChangePoints(user, -points);
            System.out.println(UserData.points);

            // spawn the chat server background task
            new OutgoingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    pointInput.getText().toString());

            new SendCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    pointInput.getText().toString());
        }
        else{
            Toast toast = Toast.makeText(Chat.this, "Invalid Input", Toast.LENGTH_SHORT);
            toast.show();
        }
        pointInput.setText("");
    }

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");
            try
            {
                port--;
                mSrvSocket = new SimWifiP2pSocketServer(port);

            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //if the socket is null, associate to a new port
                    if(mSrvSocket == null){
                        port--;
                        mSrvSocket = new SimWifiP2pSocketServer(
                                port);
                    }
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
                        mSrvSocket.close();
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

            String[] result = values[0].split(":");
            if(result.length > 1){
                if(isNumber(result[1]) && MessageOrPoints)
                {
                    UserData.points += Integer.parseInt(result[1]);
                    helper.ChangePoints(receiver, Integer.parseInt(result[1]));
                    System.out.println("passed where2");
                }
                else if(isNumber(result[1]) && !MessageOrPoints)
                {
                    Toast.makeText(Chat.this, "Invalid Input!", Toast.LENGTH_SHORT).show();
                }
                else if(!isNumber(result[1]) && !MessageOrPoints)
                {
                    UpdateOtherUserScreen(result[1], result[0]);
                    System.out.println("passed where1");
                    Toast.makeText(Chat.this, "update screen", Toast.LENGTH_SHORT).show();
                }
                else if(!isNumber(result[1]) && MessageOrPoints)
                {
                    Toast.makeText(Chat.this, "Invalid Input!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // spawn the chat server background task
            new IncommingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static boolean isNumber(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        int i = 0;
        if (string.charAt(0) == '-') {
            if (string.length() > 1) {
                i++;
            } else {
                return false;
            }
        }
        for (; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

                    try
                    {
                        port--;
                        mCliSocket = new SimWifiP2pSocket(params[0], port);
                    }
                    catch (UnknownHostException e)
                    {
                        return "Unknown Host:" + e.getMessage();
                    }
                    catch (IOException e)
                    {
                        return "IO error:" + e.getMessage();
                    }
            return null;
        }
    }

    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {

                    try
                    {
                        mCliSocket = new SimWifiP2pSocket(IP, 10001);
                        mCliSocket.getOutputStream().write((user + ":" + msg[0] + "\n").getBytes());
                        BufferedReader sockIn = new BufferedReader(new InputStreamReader(mCliSocket.getInputStream()));
                        sockIn.readLine();
                        mCliSocket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mTextInput.setText("");
        }
    }
}