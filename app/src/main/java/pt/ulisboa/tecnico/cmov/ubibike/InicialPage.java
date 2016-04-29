package pt.ulisboa.tecnico.cmov.ubibike;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InicialPage extends Fragment {

    View view;
    TextView usernameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inicial_page, container, false);

        //retrieve the bundle
        Bundle bundle = getArguments(); //retrieve the bundle
        String bundleText = bundle.getString("USER"); //get the data on the bundle

        //find view & set the text
        usernameTextView = (TextView) view.findViewById(R.id.usernametext);
        usernameTextView.setText(bundleText);

        return view;
    }
}
