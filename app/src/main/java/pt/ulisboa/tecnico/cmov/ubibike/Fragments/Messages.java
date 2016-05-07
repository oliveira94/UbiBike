package pt.ulisboa.tecnico.cmov.ubibike.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.ubibike.Chat;
import pt.ulisboa.tecnico.cmov.ubibike.DataBaseHelper;
import pt.ulisboa.tecnico.cmov.ubibike.NavigationDrawer;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.UserData;


/**
 * A simple {@link Fragment} subclass.
 */
public class Messages extends Fragment {

    public Messages() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        if(UserData.searchClicked){
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            ft.detach(this).remove(this).attach(this).commit();
            UserData.searchClicked = false;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        DataBaseHelper helper = ((NavigationDrawer) getActivity()).getDB();
        String user = UserData.username;

        String devices = helper.getListOfDevices(user);

        if(!devices.equals("noDevices"))
        {
            //TODO ler da base de dados os friends e imprimir os TextViews
            ArrayList<String> finalOutputString = gson.fromJson(devices, type);
            System.out.println("final output= " + finalOutputString);

            for (int i = 0; i < finalOutputString.size(); i++){
                LinearLayout linearLayoutVertical = (LinearLayout) view.findViewById(R.id.linearverticalmessages);
                LinearLayout chatHorizontalLayout = new LinearLayout(getActivity());
                chatHorizontalLayout.setId(i); //

                //Moving the text to the new text box
                TextView chatText = new TextView(getActivity());
                final String text = finalOutputString.get(i);

                chatHorizontalLayout.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), Chat.class);
                        i.putExtra("USER", UserData.username);
                        i.putExtra("RECEIVER" , text);
                        startActivity(i);
                    }
                });

                chatText.setText(text);
                chatText.setTextSize(22);
                chatText.setTextColor(Color.BLACK);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                //Setting the parameters to the intended
                chatHorizontalLayout.setGravity(Gravity.CENTER);

                //Adding the textView to the HorizontalLayout
                chatHorizontalLayout.addView(chatText, params);

                //Adding the whole HorizontalLayout to the VerticalLayout
                linearLayoutVertical.addView(chatHorizontalLayout);
            }
        }
        return view;
    }
}
