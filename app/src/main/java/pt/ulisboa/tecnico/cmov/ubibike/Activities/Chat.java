package pt.ulisboa.tecnico.cmov.ubibike.Activities;

import android.app.Activity;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.DataBaseHelper;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.ExchangeMessages;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.UserData;
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
    IncommingCommTask incommingCommTask;

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
        ReceiverName.setTextSize(32);

        mTextInput = (TextView)findViewById(R.id.textEntryChat);
        pointInput = (TextView)findViewById(R.id.entrypoints);
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

        incommingCommTask = new IncommingCommTask();
        incommingCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    //if you click in the button to send a message
    public void sendClickedChat(View view) {

        MessageOrPoints = false;

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

    //show up the message in the screen of the friend
    public void UpdateOtherUserScreen(String message, String username){
        LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
        LinearLayout chatHorizontalLayout = new LinearLayout(this);

        //Moving the text to the new text box
        TextView chatText = new TextView(this);

        //update the exchangeMessages
        exchangeMessages.setSender(username);
        exchangeMessages.setMessage(message);
        exchangeMessages.setReceiver(user);

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

    //after entry in the chat, print all messages in the screen
    public void updateMessages() {

        db = helper.getReadableDatabase();
        String query1 = "select sender, receiver, message from mychat";
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String sender, message;

        //travel all rows in the DB
        if (cursor1.moveToFirst()) {
            do {
                //get the sender, the receiver and the message of each row
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
                }
                else if (receiver.equals(user))
                {
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

    //if you click in the button to send points
    public void sendPointsClicked(View view) {

        MessageOrPoints = true;

        EditText entryText = (EditText) findViewById(R.id.entrypoints);
        String pointstext = entryText.getText().toString();

        int points = 0;
        if(isNumber(pointstext)){
            points = Integer.parseInt(pointstext);
        }

        //check if the number of points sent is lower than the number of the points that i have
        if(UserData.points >= points && isNumber(pointstext)){
            UserData.points -= points;
            helper.ChangePoints(user, -points);
            System.out.println(UserData.points);

            new OutgoingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    pointInput.getText().toString());

            new SendCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    pointInput.getText().toString());
        }
        else
        {
            Toast toast = Toast.makeText(Chat.this, "Invalid Input", Toast.LENGTH_SHORT);
            toast.show();
        }
        pointInput.setText("");
    }

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            UserData.NavigationOrChat = true;
            if(UserData.NavigationOrChat){
                try
                {
                    mSrvSocket = new SimWifiP2pSocketServer(port);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                while (!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        if(mSrvSocket == null){
                            port--;
                            mSrvSocket = new SimWifiP2pSocketServer(
                                    port);
                        }
                        SimWifiP2pSocket sock = mSrvSocket.accept();
                        try
                        {
                            BufferedReader sockIn = new BufferedReader(
                                    new InputStreamReader(sock.getInputStream()));
                            String st = sockIn.readLine();
                            publishProgress(st);
                            sock.getOutputStream().write(("\n").getBytes());
                        }
                        catch (IOException e) {
                            Log.d("Error reading socket:", e.getMessage());
                        }
                        finally {
                            sock.close();
                            mSrvSocket.close();
                        }
                    } catch (IOException e)
                    {
                        Log.d("Error socket:", e.getMessage());
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);
            String[] result = values[0].split(":");
            if(result.length > 1){

                //see if the input is a message or points and check if the input is valid
                if(isNumber(result[1]) && MessageOrPoints)
                {
                    UserData.points += Integer.parseInt(result[1]);
                    helper.ChangePoints(receiver, Integer.parseInt(result[1]));
                }
                else if(isNumber(result[1]) && !MessageOrPoints)
                {
                    Toast.makeText(Chat.this, "Invalid Input!", Toast.LENGTH_SHORT).show();
                }
                else if(!isNumber(result[1]) && !MessageOrPoints)
                {
                    UpdateOtherUserScreen(result[1], result[0]);
                }
                else if(!isNumber(result[1]) && MessageOrPoints)
                {
                    Toast.makeText(Chat.this, "Invalid Input!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTextInput.setText("");
            new IncommingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    //method to see if a string is a number
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