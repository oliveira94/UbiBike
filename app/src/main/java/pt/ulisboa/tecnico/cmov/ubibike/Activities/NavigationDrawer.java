package pt.ulisboa.tecnico.cmov.ubibike.Activities;

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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.DataBaseHelper;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.ExchangeMessages;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.BookBike;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.Friends;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.Historic;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.InicialPage;
import pt.ulisboa.tecnico.cmov.ubibike.Fragments.Messages;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.UserData;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SimWifiP2pManager.GroupInfoListener, SimWifiP2pManager.PeerListListener {

    DataBaseHelper helper = new DataBaseHelper(this);
    ExchangeMessages exchangeMessages = new ExchangeMessages();
    InicialPage inicialpage = new InicialPage();

    String user = "";
    Toolbar toolbar;
    TextView tx;
    int port = 10001;

    public boolean mBound = false;
    private SimWifiP2pBroadcastReceiver mReceiver;
    private String newFriend;
    private LinearLayout principalLayout, secondaryLayout;
    private Gson gson = new Gson();
    public static final String TAG = "receivinggmsg";
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    public static ListeningMsgCommTask listeningMsgCommTask;

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

        //update the points in the toolbar of the naviagation drawer
        TextView UpdateHeaderPoints = (TextView)findViewById(R.id.headerpoints);
        String points = "Points: " + UserData.points;
        UpdateHeaderPoints.setText(points);

        listeningMsgCommTask = new ListeningMsgCommTask();
        listeningMsgCommTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.naviagation_drawer, menu);
            return true;
        }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){

            int id = item.getItemId();

            if (id == R.id.action_settings)
                return true;

            return super.onOptionsItemSelected(item);
        }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected (MenuItem item){

            int id = item.getItemId();
            android.support.v4.app.FragmentTransaction fragmenttransaction =
                    getSupportFragmentManager().beginTransaction();

            //select a fragment from the toolbar
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

    //add a friend in the fragment of friends
    public void addFriend(View view) {
        principalLayout= (LinearLayout) findViewById(R.id.idFriendsVertical);
        secondaryLayout= new LinearLayout(this);
        tx = new TextView(this);
        EditText friendName= (EditText) findViewById(R.id.ADD);

        newFriend = String.valueOf(friendName.getText().toString());

        if (newFriend.equals(""))
        {
            Toast.makeText(NavigationDrawer.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            new serverRequestAddFriend().execute(UserData.username, newFriend);
        }
        friendName.setText("");
    }

    private class serverRequestAddFriend extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String urlServer = UserData.serverAddress + "/addFriend?username=";
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

            String urlServer = UserData.serverAddress + "/getProfile?username=";
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
        helper.insertFriendsAndHistory(UserData.username, "noFriends", "noTrips", "noDevices");

        for (String friend : UserData.listOfFriends)
            helper.addFriend(UserData.username, friend);

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
        if (mBound)
            mManager.requestGroupInfo(mChannel, NavigationDrawer.this);

        if (mBound)
            mManager.requestPeers(mChannel, NavigationDrawer.this);

        new serverRequestUpdate().execute(UserData.username, String.valueOf(helper.PointsFromUser(UserData.username)),
                String.valueOf(helper.getListOfTotalDistance(UserData.username)));
    }

    private class serverRequestUpdate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String urlServer = UserData.serverAddress + "/updateProfile?username=";
            urlServer += params[0] + "&points=" + params[1] + "&totaldistance=" + params[2];

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
            }catch(ConnectException e)
            {
                return "FailedConnection";
            }
            catch (IOException e) {
                e.printStackTrace();
                return "FailedConnection";
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            serverResponse(result);
        }
    }

    public void serverResponse(String result) {

        if (result.equals("FailedConnection"))
        {
            Toast.makeText(NavigationDrawer.this, "Problem connecting to the server!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (result.equals("success"))
        {
            Toast.makeText(NavigationDrawer.this, "Updated information to the server!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

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

            if(!helper.getListOfDevices(UserData.username).contains(deviceName)){
                helper.addDevice(UserData.username,deviceName);
            }

        }

        if (groupInfo.getDevicesInNetwork().toString() != "")
        {
            detectBeacon(groupInfo);
        }
    }

    public void detectBeacon(SimWifiP2pInfo devices)
    {
        String device= devices.getDevicesInNetwork().toString();
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

            if(!UserData.NavigationOrChat){
                UserData.NavigationOrChat = false;
                if(!isCancelled()) {
                    try {
                        mSrvSocket = new SimWifiP2pSocketServer(port);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            if (mSrvSocket == null) {
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
                }
            }
            return null;

        }

        @Override
        protected void onProgressUpdate(String... values) {
            String[] result = values[0].split(":");
            if(result.length > 1){
                //see if the input is the points or a message
                if(isNumber(result[1])){
                    UserData.points += Integer.parseInt(result[1]);
                    helper.ChangePoints(UserData.username, Integer.parseInt(result[1]));
                    Toast toast = Toast.makeText(NavigationDrawer.this, result[0] + " sent you points.", Toast.LENGTH_SHORT);
                    toast.show();
                    TextView UpdateHeaderPoints = (TextView)findViewById(R.id.headerpoints);
                    String points = "Points: " + UserData.points;
                    UpdateHeaderPoints.setText(points);
                }
                else {
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

        @Override
        protected void onPostExecute(Void aVoid) {
            // spawn the chat server background task
//            new ListeningMsgCommTask().executeOnExecutor(
//                    AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    //see if a string is a number
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

}
