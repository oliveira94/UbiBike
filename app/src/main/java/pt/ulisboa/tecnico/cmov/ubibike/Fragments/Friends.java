package pt.ulisboa.tecnico.cmov.ubibike.Fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.ubibike.DataBaseHelper;
import pt.ulisboa.tecnico.cmov.ubibike.NavigationDrawer;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.UserData;


/**
 * A simple {@link Fragment} subclass.
 */
public class Friends extends Fragment {


    public Friends() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        DataBaseHelper helper = ((NavigationDrawer) getActivity()).getDB();
        String user = ((NavigationDrawer) getActivity()).getUser();


        String friends = helper.getListOfFriends(user);

        if(!friends.equals("noFriends"))
        {
            //TODO ler da base de dados os friends e imprimir os TextViews
            ArrayList<String> finalOutputString = gson.fromJson(friends, type);
            System.out.println("final output= " + finalOutputString);
        }


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Gson gson = new Gson();
//        Type type = new TypeToken<ArrayList<String>>() {}.getType();
//
//        DataBaseHelper helper = ((NavigationDrawer) getActivity()).getDB();
//        String user = ((NavigationDrawer) getActivity()).getUser();
//
//        String friends = helper.getListOfFriends(user);
//
//        ArrayList<String> finalOutputString = gson.fromJson(friends, type);
//
//        System.out.println("final output= " + finalOutputString);

//        LinearLayout linearLayoutVertical = (LinearLayout) getView().findViewById(R.id.idChatLinearVertical);
//        LinearLayout chatHorizontalLayout = new LinearLayout(getActivity());
//
//        //Moving the text to the new text box
//        TextView chatText = new TextView(this);
//        EditText entryText = (EditText) findViewById(R.id.textEntryChat);
//        String text = entryText.getText().toString();
//
//        chatText.setText(text);
//        chatText.setTextSize(22);
//        chatText.setTextColor(Color.BLACK);
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//
//        //Setting the parameters to the intended
//        chatHorizontalLayout.setGravity(Gravity.RIGHT);
//
//        //Adding the textView to the HorizontalLayout
//        chatHorizontalLayout.addView(chatText, params);
//
//        //Adding the whole HorizontalLayout to the VerticalLayout
//        linearLayoutVertical.addView(chatHorizontalLayout);
    }
}
