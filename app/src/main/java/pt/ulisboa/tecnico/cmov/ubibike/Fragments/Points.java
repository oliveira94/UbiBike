package pt.ulisboa.tecnico.cmov.ubibike.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.ubibike.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class Points extends Fragment {

    View view;
    TextView usernameTextView;

    public Points() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_points, container, false);

        //retrieve the bundle
        Bundle bundle = getArguments();

        //get the data on the bundle
        String bundleText = bundle.getString("USER");
        String bundlePoints = bundle.getString("POINTS");

        //find view & set the text
        usernameTextView = (TextView) view.findViewById(R.id.myusername);
        usernameTextView.setText(bundleText);

        usernameTextView = (TextView)view.findViewById(R.id.mypoints);
        usernameTextView.setText("Current Points: " + bundlePoints);




        return view;
    }

}
