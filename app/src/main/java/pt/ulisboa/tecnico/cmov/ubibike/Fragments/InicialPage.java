package pt.ulisboa.tecnico.cmov.ubibike.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;

import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.UserData;

public class InicialPage extends Fragment {

    View view;
    TextView usernameTextView;
    double distance = 0.0;

    //retrieve the bundle


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inicial_page, container, false);



        //find view & set the text
        usernameTextView = (TextView) view.findViewById(R.id.usernametext);
        usernameTextView.setText(UserData.username);

        usernameTextView = (TextView) view.findViewById(R.id.pointsText);
        usernameTextView.setText("Current Points: " + UserData.points);

        usernameTextView = (TextView) view.findViewById(R.id.ageupdate);
        usernameTextView.setText("Age: " + UserData.age);

        distance = UserData.totalDistance;
//        double distanceValue = Double.valueOf(distance);
//        DecimalFormat DF = new DecimalFormat("#.0");
//        distance = DF.format(distanceValue);

        usernameTextView=(TextView) view.findViewById(R.id.TotalDistanceText);
        usernameTextView.setText("Total Distance: "+ distance);

        usernameTextView = (TextView) view.findViewById(R.id.NumberFriendsText);
        usernameTextView.setText("Number of Friends: " + UserData.listOfFriends.size());
        
        return view;
    }
}
