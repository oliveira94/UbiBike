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
import pt.ulisboa.tecnico.cmov.ubibike.Activities.Chat;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.DataBaseHelper;
import pt.ulisboa.tecnico.cmov.ubibike.Activities.NavigationDrawer;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.UserData;

public class Messages extends Fragment {

    boolean SeeIfIsFriends = false;

    public Messages() {}

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

        //go get the string with all of his devices
        String devices = helper.getListOfDevices(user);
        //go get the string with all of his friends
        String friends = helper.getListOfFriends(user);

        if(!devices.equals("noDevices") && !friends.equals("noFriends"))
        {
            //arraylist where each position is a device
            ArrayList<String> Devices = gson.fromJson(devices, type);
            //arraylist where each position is a friend
            ArrayList<String> Friends = gson.fromJson(friends, type);

            //travel the arraylist of devices
            for (int i = 0; i < Devices.size(); i++){
                //travel the arraylist of friends
                for(int j = 0; j < Friends.size(); j++)
                {
                    //if a device is a friend too, create a chat for comunicate with that friends
                    if(Devices.get(i).equals(Friends.get(j)))
                        SeeIfIsFriends = true;
                }
                if(SeeIfIsFriends){

                    SeeIfIsFriends = false;

                    TextView chatText = new TextView(getActivity());
                    final String text = Devices.get(i);

                    LinearLayout linearLayoutVertical = (LinearLayout) view.findViewById(R.id.linearverticalmessages);
                    LinearLayout chatHorizontalLayout = new LinearLayout(getActivity());
                    chatHorizontalLayout.setId(i); //

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

                    chatHorizontalLayout.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), Chat.class);
                            i.putExtra("USER", UserData.username);
                            i.putExtra("RECEIVER" , text);
                            startActivity(i);
                        }
                    });

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
        }
        return view;
    }
}
