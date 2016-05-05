package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
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
    Points points = new Points();
    private SimWifiP2pBroadcastReceiver mReceiver;

    DataBaseHelper helper = new DataBaseHelper(this);

    public boolean mBound = false;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naviagation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            user = extras.getString("KEY");
        }

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

        Bundle bundle = new Bundle(); //create the bundle
        bundle.putString("USER", user); //attach data to the bundle

        //example of a get of a global variable
        int points = UserData.points;
        bundle.putString("POINTS", Integer.toString(points));

        int age = UserData.age;
        bundle.putString("AGE", Integer.toString(age));

        TextView UpdateHeaderName = (TextView)findViewById(R.id.headername);
        UpdateHeaderName.setText(user);

        TextView UpdateHeaderPoints = (TextView)findViewById(R.id.headerpoints);
        UpdateHeaderPoints.setText("Points: " + Integer.toString(points));

        inicialpage.setArguments(bundle); //set the bundle on the fragment



        fragmenttransaction.replace(R.id.container, inicialpage);
        fragmenttransaction.commit();

        DrawerLayout drawer1 = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer1.closeDrawer(GravityCompat.START);
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
        LinearLayout principalLayout= (LinearLayout) findViewById(R.id.idFriendsVertical);
        LinearLayout secondaryLayout= new LinearLayout(this);
        TextView tx= new TextView(this);
        EditText et= (EditText) findViewById(R.id.ADD);

        String newFriend = String.valueOf(et.getText().toString());

        //TODO

        if (newFriend.equals("")) {
            Toast.makeText(NavigationDrawer.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
        }else
        {
            //insert the list of friends in the database

//            helper.insertFriends(UserData.username, inputString);
//

            tx.setText(newFriend);
            tx.setTextSize(22);
            tx.setTextColor(Color.BLACK);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.setMargins(0, 20, 0, 10);

            secondaryLayout.addView(tx, params);
            principalLayout.addView(secondaryLayout);
        }
        et.setText("");
    }

    public DataBaseHelper getDB(){
        return helper;
    }

    public String getUser(){
        return UserData.username;
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
}
