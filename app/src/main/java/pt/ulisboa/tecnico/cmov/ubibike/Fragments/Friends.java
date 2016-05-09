package pt.ulisboa.tecnico.cmov.ubibike.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import pt.ulisboa.tecnico.cmov.ubibike.DataBase.DataBaseHelper;
import pt.ulisboa.tecnico.cmov.ubibike.Activities.NavigationDrawer;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.UserData;

public class Friends extends Fragment {

    public Friends() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        DataBaseHelper helper = ((NavigationDrawer) getActivity()).getDB();
        String user = UserData.username;

        //go get the string with all of his friends
        String friends = helper.getListOfFriends(user);

        if(!friends.equals("noFriends"))
        {
            //each element of the arraylist is a friend
            ArrayList<String> FriendsList = gson.fromJson(friends, type);

            for (int i = 0; i < FriendsList.size(); i++){

                LinearLayout linearLayoutVertical = (LinearLayout) view.findViewById(R.id.idFriendsVertical);
                LinearLayout chatHorizontalLayout = new LinearLayout(getActivity());

                TextView chatText = new TextView(getActivity());
                String text = FriendsList.get(i);

                chatText.setText(text);
                chatText.setTextSize(22);
                chatText.setTextColor(Color.BLACK);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                //Setting the parameters to the intended
                chatHorizontalLayout.setGravity(Gravity.LEFT);

                //Adding the textView to the HorizontalLayout
                chatHorizontalLayout.addView(chatText, params);

                //Adding the whole HorizontalLayout to the VerticalLayout
                linearLayoutVertical.addView(chatHorizontalLayout);
            }
            }

        return view;
    }
}
