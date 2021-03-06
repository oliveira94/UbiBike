package pt.ulisboa.tecnico.cmov.ubibike.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.UserData;

public class InicialPage extends Fragment {

    View view;
    TextView usernameTextView;
    double distance = 0.0;

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

        usernameTextView=(TextView) view.findViewById(R.id.TotalDistanceText);
        usernameTextView.setText("Total Distance: "+ convertDecimalToDouble(distance)+"KM");

        usernameTextView = (TextView) view.findViewById(R.id.NumberFriendsText);
        usernameTextView.setText("Number of Friends: " + UserData.listOfFriends.size());
        
        return view;
    }
    public double convertDecimalToDouble(double number) {
        NumberFormat decimalFormat = new DecimalFormat("#.0");
        String decimalToString = decimalFormat.format(number);
        double aDouble = Double.valueOf(decimalToString);
        return aDouble;
    }
}
