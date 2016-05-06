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

public class LogIn extends AppCompatActivity {

    DataBaseHelper helper = new DataBaseHelper(this);
    private SimWifiP2pBroadcastReceiver mReceiver;
    String Iusername;
    String Ipass;


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
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void LogToNavigation(View view) {

        EditText username = (EditText)findViewById(R.id.lognameid);
        EditText pass = (EditText)findViewById(R.id.logpasswordid);

        Iusername = username.getText().toString();
        Ipass = pass.getText().toString();

        if(Iusername.isEmpty() || Ipass.isEmpty())
        {
            Toast.makeText(LogIn.this, "You need to fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        new serverRequestLogIn().execute(Iusername);

    }

    private void serverResponse(String response) {


        //response comes in name:password:points:age or noUsername in case it doesn't exist
        if (response.equals("FailedConnection"))
        {
            Toast.makeText(LogIn.this, "Problem connecting to the server!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (response.equals("noUsername")) {
            Toast.makeText(LogIn.this, "Username does not exist!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] result = response.split(":");

        if (Ipass.equals(result[1]))    //If password is correct
        {
            //example of set a global variable
            UserData.name = result[0];
            UserData.points = Integer.parseInt(result[2]);
            UserData.age = Integer.parseInt(result[3]);
            UserData.username = Iusername;

            Intent i = new Intent(this, NavigationDrawer.class);
            startActivity(i);
        } else
            Toast.makeText(LogIn.this, "Password is wrong!", Toast.LENGTH_SHORT).show();

    }

    private class serverRequestLogIn extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlServer = "http://10.0.3.2:8080/logIn?username=";
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
            serverResponse(result);
        }
    }
}
