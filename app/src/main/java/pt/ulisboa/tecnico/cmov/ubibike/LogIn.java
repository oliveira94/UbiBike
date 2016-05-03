package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class LogIn extends AppCompatActivity {

    DataBaseHelper helper = new DataBaseHelper(this);

    private SimWifiP2pBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

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
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(mReceiver);
    }

    public void LogToNavigation(View view) {

        EditText username = (EditText)findViewById(R.id.lognameid);
        EditText pass = (EditText)findViewById(R.id.logpasswordid);

        String Iusername = username.getText().toString();
        String Ipass = pass.getText().toString();

        String Password = helper.searchPassword(Iusername);

        if(Ipass.equals(Password)){
            //example of set a global variable
            ((UserData) this.getApplication()).setName(Iusername);
            ((UserData) this.getApplication()).setPassword(Iusername);
            ((UserData) this.getApplication()).setPoints(helper.PointsFromUser(Iusername));
            ((UserData) this.getApplication()).setAge(helper.AgeFromUser(Iusername));

            Intent i = new Intent(this,NavigationDrawer.class);
            i.putExtra("KEY", Iusername);
            startActivity(i);
        }
        else
        {
            Toast toast = Toast.makeText(LogIn.this, "Username or password are wrong", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
