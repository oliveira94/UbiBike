package pt.ulisboa.tecnico.cmov.ubibike;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.ulisboa.tecnico.cmov.ubibike.WifiDirect.SimWifiP2pBroadcastReceiver;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SimWifiP2pBroadcastReceiver receiver;
    private ArrayList<LatLng>  markerPoints= new ArrayList<LatLng>();
    private Location currentLocation = new Location ("current locaction");
    private double distance = 0.0;
    private int points=0;
    TextView tx;
    private Map<ArrayList<LatLng>,String> coordinates = new HashMap<>();
    //DataBaseHelper database = new DatabaseHelper (this);

    public MapsActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //TODO ter em todas as actividades
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        receiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(receiver, filter);
        tx=(TextView) findViewById(R.id.KM);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                currentLocation = location;
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                if (markerPoints.size() != 0)
                {

                    tx.setText(String.valueOf(distance() + "Km"));

                }
                markerPoints.add(loc);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
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
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20, (float) 70.00, listener);
    }
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
    public void onPause ()
    {
        super.onPause();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String date = dateFormat.format(calendar.getTime());
        coordinates.put(markerPoints, date);
        UserData.history.add(coordinates);
        calculatePoints();
        UserData.points = points;
        UserData.totalDistance += distance;

        //TODO para enviar os pontos para BD
        //TODO PARA ENVIAR PARA A DB DO SERVER


    }
    //metodo para calcular os pontos dados a um utilizador consoante os kilometros feitos
    public void calculatePoints()
    {
        points = (int)(distance/2);
    }
    //metodo para calcular a distancia com base nas coordenadas (currentLocantion - lastLocation)
    public double distance()
    {
        double AuxDistance = 0.0;
        int lastLocationPosition = markerPoints.size()-1;
        Location lastLocation = new Location ("lastLocation");
        lastLocation.setLongitude(markerPoints.get(lastLocationPosition).longitude);
        lastLocation.setLatitude(markerPoints.get(lastLocationPosition).latitude);
        AuxDistance = lastLocation.distanceTo(currentLocation);
        AuxDistance = AuxDistance/1000;
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        String auxFormat = decimalFormat.format(AuxDistance);
        AuxDistance = Double.valueOf(auxFormat);
        return distance += AuxDistance;
    }
}
