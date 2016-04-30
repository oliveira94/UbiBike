package pt.ulisboa.tecnico.cmov.ubibike.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.ubibike.R;

public class InicialPage extends Fragment {

    View view;
    TextView usernameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inicial_page, container, false);

        //retrieve the bundle
        Bundle bundle = getArguments();

        //get the data on the bundle
        String bundleText = bundle.getString("USER");
        String bundlePoints = bundle.getString("POINTS");
        String bundleAge = bundle.getString("AGE");

        //find view & set the text
        usernameTextView = (TextView) view.findViewById(R.id.usernametext);
        usernameTextView.setText(bundleText);

        usernameTextView = (TextView)view.findViewById(R.id.pointsText);
        usernameTextView.setText("Current Points: " + bundlePoints);

        usernameTextView = (TextView)view.findViewById(R.id.ageupdate);
        usernameTextView.setText("Age: " + bundleAge);



        return view;
    }
}
