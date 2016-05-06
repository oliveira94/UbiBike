package pt.ulisboa.tecnico.cmov.ubibike;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.design.widget.NavigationView;
import android.support.v4.media.session.MediaSessionCompatApi14;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.BookBike;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.Friends;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.Historic;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.InicialPage;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.Messages;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.Points;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SimWifiP2pManager.GroupInfoListener, SimWifiP2pManager.PeerListListener {

    String user = "";
    InicialPage inicialpage = new InicialPage();
    ExchangeMessages exchangeMessages = new ExchangeMessages();
    private SimWifiP2pBroadcastReceiver mReceiver;
    TextView tx;
    private String newFriend;
    private String searchfriend;
    private LinearLayout principalLayout, secondaryLayout;
    private Gson gson = new Gson();
    Toolbar toolbar;

    public static final String TAG = "receivinggmsg";
    private SimWifiP2pSocketServer mSrvSocket = null;
    int port = 9990;

    DataBaseHelper helper = new DataBaseHelper(this);
    public boolean mBound = false;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naviagation_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = UserData.username;

        new serverRequestGetProfile().execute(UserData.username);

    }
        @Override
        public void onBackPressed () {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
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

        // spawn the chat server background task
        new ListeningMsgCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.naviagation_drawer, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected (MenuItem item){
            // Handle navigation view item clicks here.

            int id = item.getItemId();
            android.support.v4.app.FragmentTransaction fragmenttransaction =
                    getSupportFragmentManager().beginTransaction();

            if (id == R.id.InicialPage) {

                fragmenttransaction.replace(R.id.container, inicialpage);
                fragmenttransaction.commit();

            } else if (id == R.id.HistoryItem) {

                fragmenttransaction.replace(R.id.container, new Historic());
                fragmenttransaction.commit();

            } else if (id == R.id.FriendsItem) {

                fragmenttransaction.replace(R.id.container, new Friends());

                fragmenttransaction.commit();

            }else if (id == R.id.BookBikeItem) {

                fragmenttransaction.replace(R.id.container, new BookBike());
                fragmenttransaction.commit();

            } else if (id == R.id.MessagesItem) {

                fragmenttransaction.replace(R.id.container, new Messages());
                fragmenttransaction.commit();

            }else if(id == R.id.Mapdebug)
            {
                Intent maps = new Intent(this,MapsActivity.class);
                startActivity(maps);

            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

    public void FrameClicked(View view) {
        //send user name to chat
        Intent i = new Intent(this, Chat.class);
        i.putExtra("USER", user);
        startActivity(i);
    }

    public void addFriend(View view) {
        principalLayout= (LinearLayout) findViewById(R.id.idFriendsVertical);
        secondaryLayout= new LinearLayout(this);
        tx = new TextView(this);
        EditText friendName= (EditText) findViewById(R.id.ADD);

        newFriend = String.valueOf(friendName.getText().toString());

        if (newFriend.equals("")) {
            Toast.makeText(NavigationDrawer.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
        }else
        {
            new serverRequestAddFriend().execute(UserData.username, newFriend);
        }
        friendName.setText("");
    }

    //method to search a friend after an input
    public void searchFriendButton(View view) {
        UserData.searchClicked = true;
        EditText friend = (EditText)findViewById(R.id.searchfriend);
        searchfriend = String.valueOf(friend.getText().toString());

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        String user = UserData.username;
        String friends = helper.getListOfFriends(user);

        if(!friends.equals("noFriends"))
        {
            ArrayList<String> finalOutputString = gson.fromJson(friends, type);
            System.out.println("final output= " + finalOutputString);

            for (int i = 0; i < finalOutputString.size(); i++){
                String text = finalOutputString.get(i);
                if(text.equals(searchfriend)){

                    //Moving the text to the new text box
                    TextView chatText = new TextView(this);

                    LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.linearverticalmessages);
                    LinearLayout chatHorizontalLayout = new LinearLayout(this);

                    chatText.setText(text);
                    chatText.setTextSize(22);
                    chatText.setTextColor(Color.BLACK);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                    //Setting the parameters to the intended
                    chatHorizontalLayout.setGravity(Gravity.CENTER);

                    //Adding the textView to the HorizontalLayout
                    chatHorizontalLayout.addView(chatText, params);

                    //Adding the whole HorizontalLayout to the VerticalLayout
                    linearLayoutVertical.addView(chatHorizontalLayout);
                }
            }
        }
    }


    private class serverRequestAddFriend extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlServer = "http://10.0.3.2:8080/addFriend?username=";
            urlServer += params[0] + "&newFriend=" + params[1];

            StringBuffer result = new StringBuffer("");
            try{
                URL url = new URL(urlServer);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = rd.readLine()) != null) result.append(line);

            }catch (SocketTimeoutException e) {
                return "FailedConnection";
            }catch(ConnectException e) {
                return "FailedConnection";
            }catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            serverResponseAddFriend(result);
        }
    }

    private void serverResponseAddFriend(String result)
    {
        if (result.equals("success"))
        {
            helper.addFriend(UserData.username, newFriend);
            Toast.makeText(NavigationDrawer.this, "User added successfully!", Toast.LENGTH_SHORT).show();
            tx.setText(newFriend);
            tx.setTextSize(22);
            tx.setTextColor(Color.BLACK);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.setMargins(0, 20, 0, 10);

            secondaryLayout.addView(tx, params);
            principalLayout.addView(secondaryLayout);
        }
        else if (result.equals("alreadyFriend"))
        {
            Toast.makeText(NavigationDrawer.this, "That user is already your friend!", Toast.LENGTH_SHORT).show();
        }
        else if (result.equals("yourself"))
        {
            Toast.makeText(NavigationDrawer.this, "You can't friend yourself!", Toast.LENGTH_SHORT).show();
        }
        else if(result.equals("false"))
        {
            Toast.makeText(NavigationDrawer.this, "User does not exist!", Toast.LENGTH_SHORT).show();
        }
    }

    private class serverRequestGetProfile extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlServer = "http://10.0.3.2:8080/getProfile?username=";
            urlServer += params[0];

            StringBuffer result = new StringBuffer("");
            try{
                URL url = new URL(urlServer);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = rd.readLine()) != null) result.append(line);

            }catch (SocketTimeoutException e) {
                return "FailedConnection";
            } catch(ConnectException e) {
                return "FailedConnection";
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            serverResponseGetProfile(result);
        }
    }

    private void serverResponseGetProfile(String result)
    {
        Type type = new TypeToken<HashMap>() {}.getType();
        HashMap profileData = gson.fromJson(result, type);

        //Update UserData class with information from server
        UserData.name = (String) profileData.get("name");
        UserData.age = Integer.valueOf((String)profileData.get("age"));
        UserData.points = helper.PointsFromUser(UserData.username);
        UserData.totalDistance = Double.valueOf((String) profileData.get("distance"));
        UserData.history = (ArrayList<Object>) profileData.get("history");
        UserData.listOfFriends = (ArrayList<String>) profileData.get("friendsList");

        helper.insertUserData(UserData.name, UserData.age, UserData.username);
        helper.insertFriendsAndHistory(UserData.username, "noFriends", "noTrips");

        for (String friend : UserData.listOfFriends)
            helper.addFriend(UserData.username, friend);

        //TODO helper.addHistory

        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        android.support.v4.app.FragmentTransaction fragmenttransaction =
                getSupportFragmentManager().beginTransaction();


        TextView UpdateHeaderName = (TextView)findViewById(R.id.headername);
        UpdateHeaderName.setText(user);

        TextView UpdateHeaderPoints = (TextView)findViewById(R.id.headerpoints);
        String points = "Points: " + UserData.points;
        UpdateHeaderPoints.setText(points);

        fragmenttransaction.replace(R.id.container, inicialpage);
        fragmenttransaction.commit();

        DrawerLayout drawer1 = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer1.closeDrawer(GravityCompat.START);


    }

    public DataBaseHelper getDB(){
        return helper;
    }


    public void ActClicked(View view){
        if (mBound) {
            mManager.requestGroupInfo(mChannel, NavigationDrawer.this);
        } else {
            Toast.makeText(view.getContext(), "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
        if (mBound) {
            mManager.requestPeers(mChannel, NavigationDrawer.this);
        } else {
            Toast.makeText(view.getContext(), "Service not bound",
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

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
                StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {

            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null) ? "??" : device.getVirtIp()) + ")\n";
            peersStr.append(devstr);

            ((UserData) this.getApplication()).AddDevicesNameToList(deviceName);
            ((UserData) this.getApplication()).AddDeviceIPToList(device.getVirtIp());
            ((UserData) this.getApplication()).GetDeviceIP(deviceName);
            ((UserData) this.getApplication()).GetName(UserData.IP);

        }

        if (devices.getDeviceList().size()!=0)
        {

            detectBeacon(devices);
        }

    }

    public void detectBeacon(SimWifiP2pDeviceList devices)
    {

        String device= devices.getDeviceList().toString();
         if (device.contains("Beacon"))
         {
             UserData.beaconAround = true;
         }else
         {
             UserData.beaconAround = false;
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

    public class ListeningMsgCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");
            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        port);
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
            //mTextOutput.append(values[0] + "\n");
            String[] result = values[0].split(":");
            //update the exchangeMessages
            exchangeMessages.setSender(result[0]);
            exchangeMessages.setMessage(result[1]);
            exchangeMessages.setReceiver(UserData.username);

            Toast toast = Toast.makeText(NavigationDrawer.this, result[0] + " sent you a new message.", Toast.LENGTH_SHORT);
            toast.show();

            //put the message in the database
            helper.sendNewMessage(exchangeMessages);


        }
    }
}
