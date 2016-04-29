package pt.ulisboa.tecnico.cmov.ubibike;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InicialPage extends Fragment {

    SQLiteDatabase db;
    View view;
    TextView usernameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inicial_page, container, false);
        //usernameTextView = (TextView) view.findViewById(R.id.usernametext);

        return view;

    }

    public TextView getUsernameTextView(){
        return usernameTextView;
    }

    public void setUsernameTextView(String username){
        usernameTextView.setText(username);
    }

}
