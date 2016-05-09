package pt.ulisboa.tecnico.cmov.ubibike.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulisboa.tecnico.cmov.ubibike.DataBase.DataBaseHelper;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.UserData;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SimWifiP2pBroadcastReceiver receiver;
    private ArrayList<LatLng>  markerPoints= new ArrayList<LatLng>();
    private Location currentLocation = new Location ("current locaction");
    //distancia percorrida enquanto o mapa esta ligado
    private double distance = 0.0;
    //pontos obtidos nesta rota
    private int points=0;
    TextView tx;
    private Map<ArrayList<LatLng>,String> coordinates = new HashMap<>();
    DataBaseHelper helper = new DataBaseHelper(this);
    private int seconds, minutes,hours = 0;
    String seconds1,minutes1,hours1 ="";
    private boolean buttonClick = false;
    public MapsActivity() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setLayout();

        if (UserData.route == true)
        {
            mMap= ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            String data = bundle.getSerializable("data").toString();
            String [] splitData= data.split("\\s+");
            String timer = splitData[splitData.length - 1];
            tx=(TextView)findViewById(R.id.time);
            tx.setText(timer);
            parseCoordinates(bundle.getSerializable("rota").toString());
            int focus= Integer.valueOf(markerPoints.size()/2);
            travelDistance();
            tx=(TextView) findViewById(R.id.KM);
            tx.setText(formarter(distance)+"KM");
            tx = (TextView) findViewById(R.id.points);
            calculatePoints();
            tx.setText( points+" Points");
            mMap.addPolyline(plot());
            mMap.addMarker(new MarkerOptions().position(markerPoints.get(0)).title("Start").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions().position(markerPoints.get(markerPoints.size()-1)).title("End").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPoints.get(focus-1)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(markerPoints.get(focus - 1).latitude, markerPoints.get(focus-1).longitude), 12.0f));

        }

    }
    public void parseCoordinates(String coordinates)
    {
        String [] spliter=coordinates.split("lat/lng:");
        spliter[spliter.length-1]+=" ";
        for (int i =1; i < spliter.length;i++)
        {
            spliter[i]=spliter[i].substring(2,spliter[i].length()-3);
            Log.i("Rota: ", spliter[i]);
            String [] spliter2= spliter[i].split(",");
            LatLng position = new LatLng(Double.valueOf(spliter2[0]),Double.valueOf(spliter2[1]));
            markerPoints.add(position);
        }
    }
    //metodo para desenhar a rota no mapa
    public PolylineOptions plot()
    {
        PolylineOptions p = new PolylineOptions().width(6).color(0xFFEE8888);
        for (int i = 0; i < markerPoints.size(); i++) {
            LatLng is = markerPoints.get(i);
            p.add(new LatLng(is.latitude,is.longitude));
        }
        return p;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
    }
    @Override
    public void onPause () {
        super.onPause();

        if (UserData.route == false && hours1 != null && minutes1 != null && seconds1 != null )  {
            //linhas para ver a data para de seguida
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String date = dateFormat.format(calendar.getTime());
            date+=" Duration "+hours1+minutes1+seconds1;

            coordinates.put(markerPoints, date);
            //actualizar o percurso na variavel na classe global
            UserData.history.add(coordinates);
            //calcular os pontos  obtidos na viagem
            calculatePoints();
            //actualizar os pontos na classe global
            UserData.points += points;
            //actializar a variavel da distancia da classe global
            UserData.totalDistance += distance;
            //variavel para saber se o mapa foi inicializadp atravez do histório

            //adiccionar a distancia percorrida à base de dados
            helper.AddNewDistance(UserData.username, distance);
            mMap.clear();
            Gson gson = new Gson();
            String coordinatesString = gson.toJson(coordinates);

            new serverRequestAddDistance().execute(UserData.username, String.valueOf(distance));
            new serverRequestAddHistory().execute(UserData.username, coordinatesString);
            Log.i("coordinates: ", coordinatesString);
            Button bt = (Button) findViewById(R.id.startRoute);
            bt.setEnabled(false);

        } else {
            mMap.clear();
            UserData.route = false;
        }
    }
    //metodo para calcular os pontos dados a um utilizador consoante os kilometros feitos
    public void calculatePoints()
    {
        points = (int)(distance*10);
    }
    //metodo para calcular a distancia com base nas coordenadas (currentLocantion - lastLocation)
    public double distance()
    {
        double AuxDistance = 0.0;
        //obter a ultima coordenada inserida no array
        int lastLocationPosition = markerPoints.size()-1;
        //retirar do array a ultima distancia inserida
        Location lastLocation = new Location ("lastLocation");
        lastLocation.setLongitude(markerPoints.get(lastLocationPosition).longitude);
        lastLocation.setLatitude(markerPoints.get(lastLocationPosition).latitude);
        //variavel AuxDistance é uma variavel auxiliar para calcular a distancia
        AuxDistance = lastLocation.distanceTo(currentLocation);
        AuxDistance = AuxDistance/1000;
        return distance += AuxDistance;
    }
    public double formarter(double number)
    {
        NumberFormat decimalFormat = new DecimalFormat("#.0");
        String auxFormat = decimalFormat.format(number);
        double retorno = Double.valueOf(auxFormat);
        return retorno;
    }
    public void setLayout() {
        //textview para mostrar os km efectuados pelo dispositivo
        tx = (TextView) findViewById(R.id.KM);
        tx.setGravity(Gravity.CENTER_HORIZONTAL);
        Button bt = (Button) findViewById(R.id.startRoute);
        bt.setEnabled(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        params.height = (int) (metrics.heightPixels * 0.90);
        mapFragment.getView().setLayoutParams(params);
        mapFragment.getMapAsync(this);
        RelativeLayout bottom = (RelativeLayout) findViewById(R.id.bottomVertival);
        bottom.setBackgroundColor(Color.rgb(255, 255, 255));
        bottom.setGravity((int) (metrics.heightPixels * 0.90));
        ViewGroup.LayoutParams params1 = bottom.getLayoutParams();
        params1.height = (int) ((metrics.heightPixels) * 0.10);
        bottom.setLayoutParams(params1);
        //bottom.addView(tx);
        if (UserData.route == true) {
            bt.setVisibility(View.INVISIBLE);
            TextView tx2 = (TextView) findViewById(R.id.points);
            tx2.setVisibility(View.VISIBLE);
        }

        if (UserData.beaconAround == false)
        {
            bt.setEnabled(false);
        }
    }
    public void travelDistance()
    {
        Location firstLocation = new Location("firstLocstion");
        Location secondLocation = new Location ("secondLocation");
        double dist = 0.0;
        for (int i = 1; i < markerPoints.size();i++)
        {
            firstLocation.setLatitude(markerPoints.get(i-1).latitude);
            firstLocation.setLongitude(markerPoints.get(i - 1).longitude);
            secondLocation.setLatitude(markerPoints.get(i).latitude);
            secondLocation.setLongitude(markerPoints.get(i).longitude);
            dist += secondLocation.distanceTo(firstLocation);
        }
        distance = dist/1000;
    }

    public void startRoute(View view) {

        if (UserData.route == false && buttonClick == false) {
            buttonClick = true;
            LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            final TextView time = (TextView) findViewById(R.id.time);
            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seconds++;
                            if (seconds == 60) {
                                seconds = 0;
                                minutes++;
                            }
                            if (minutes == 60) {
                                minutes = 0;
                                hours++;
                            }
                            if (seconds < 10) {
                                seconds1 = ":0" + seconds;
                            } else {
                                seconds1 = ":" + seconds;
                            }
                            if (minutes < 10) {
                                minutes1 = ":0" + minutes;
                            } else {
                                minutes1 = ":" + minutes;
                            }
                            if (hours < 10) {
                                hours1 = "0" + hours;
                            } else {
                                hours1 = ":" + hours;
                            }
                            time.setText(hours1 + minutes1 + seconds1);

                        }
                    });

                }
            }, 0, 1000);
            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //variavel onde está a posição actual do dispositivo
                    currentLocation = location;
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    if (markerPoints.size() != 0) {
                        distance();
                        tx.setText(String.valueOf("" + formarter(distance) + "Km"));

                    }
                    //adicionar a localização do dispositivo
                    markerPoints.add(loc);
                    //centrar o mappa na posição actual do dispositivo
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    //desenhar no mapa a rota percorrida pelo dispositivo
                    mMap.addPolyline(plot());

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20, (float) 20.00, listener);
        }

    }

    private class serverRequestAddDistance extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlServer = UserData.serverAddress + "/addDistance?username=";
            urlServer += params[0] + "&newDistance=" + params[1];

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
            if (result.equals("FailedConnection"))
                Toast.makeText(MapsActivity.this, "Can't connect to the server!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MapsActivity.this, "Distance added to the server successfully!", Toast.LENGTH_SHORT).show();

        }
    }

    private class serverRequestAddHistory extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlServer = UserData.serverAddress + "/setHistory?username=";
            urlServer += params[0] + "&history=" + params[1];

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
            if (result.equals("FailedConnection"))
                Toast.makeText(MapsActivity.this, "Can't connect to the server!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MapsActivity.this, "Distance added to the server successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}
