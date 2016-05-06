package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;


public class CreateAccount extends AppCompatActivity {

    DataBaseHelper helper = new DataBaseHelper(this);
    private SimWifiP2pBroadcastReceiver mReceiver;
    String Iname;
    String Iage;
    String Iusername;
    String Ipass1;
    String Ipass2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
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
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void SignUpToDraw(View view) {

        EditText name = (EditText)findViewById(R.id.nameid);
        EditText age = (EditText)findViewById(R.id.ageid);
        EditText username = (EditText)findViewById(R.id.userid);
        EditText pass1 = (EditText)findViewById(R.id.pass1id);
        EditText pass2 = (EditText)findViewById(R.id.pass2id);

        Iname = name.getText().toString();
        Iage = age.getText().toString();
        Iusername = username.getText().toString();
        Ipass1 = pass1.getText().toString();
        Ipass2 = pass2.getText().toString();


        if(Iname.isEmpty() || Iage.isEmpty() || Iusername.isEmpty() || Ipass1.isEmpty() || Ipass2.isEmpty())
        {
            Toast.makeText(CreateAccount.this, "You need to fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Ipass1.equals(Ipass2))
        {
            Toast.makeText(CreateAccount.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        new serverRequestCreate().execute(Iusername, Ipass1, Iname, Iage);
    }

    public void serverResponse(String result) {

        if (result.equals("FailedConnection"))
        {
            Toast.makeText(CreateAccount.this, "Problem connecting to the server!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (result.equals("true"))
            Toast.makeText(CreateAccount.this, "Account was created successfully!", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(CreateAccount.this, "Username already exists! Please choose another", Toast.LENGTH_SHORT).show();
            return;
        }


        //crete a object with info for later put it in the database
        UserData.name = Iname;
        UserData.age = Integer.parseInt(Iage);
        UserData.username = Iusername;
        UserData.points = helper.PointsFromUser(Iname);

        //put userdata in the database
        helper.insertUserData(UserData.name, UserData.age, UserData.username);
        helper.insertFriendsAndHistoric(UserData.username, "noFriends", "noTrips");

        Intent i = new Intent(this, NavigationDrawer.class);
        i.putExtra("KEY", Iusername);
        startActivity(i);

    }

    private class serverRequestCreate extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlServer = "http://10.0.2.2:8080/create?username=";
            urlServer += params[0] + "&password=" + params[1] + "&name=" + params[2] + "&age=" + params[3];

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
}
