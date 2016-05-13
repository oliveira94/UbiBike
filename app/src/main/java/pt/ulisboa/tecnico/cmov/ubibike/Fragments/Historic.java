package pt.ulisboa.tecnico.cmov.ubibike.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import java.text.DecimalFormat;
import java.text.NumberFormat;

import pt.ulisboa.tecnico.cmov.ubibike.Activities.MapsActivity;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.DataBase.UserData;

public class Historic extends Fragment {

    public Historic() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View view= inflater.inflate(R.layout.fragment_historic, container, false);

        for (int i =0; i<UserData.history.size();i++)
        {
            LinearLayout linearLayoutVertical = (LinearLayout) view.findViewById(R.id.Historico);
            LinearLayout historyHorizontalLayout = new LinearLayout(getActivity());

            Button routes= new Button(getActivity());
            String text=UserData.history.get(i).toString();

            final String [] spliter= text.split("=");
            spliter[1]=spliter[1].substring(0,spliter[1].length()-1);
            spliter[0]=spliter[0].substring(1);

            routes.setText("Route done on " + spliter[1]);
            routes.setTextSize(12);
            routes.setTextColor(Color.BLACK);
            routes.setBackgroundColor(Color.TRANSPARENT);
            routes.setId(i);

            final int id= routes.getId();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            //Setting the parameters to the intended
            historyHorizontalLayout.setGravity(Gravity.LEFT);

            //Adding the textView to the HorizontalLayout
            historyHorizontalLayout.addView(routes, params);

            //Adding the whole HorizontalLayout to the VerticalLayout
            linearLayoutVertical.addView(historyHorizontalLayout);
            Button button=(Button) view.findViewById(id);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserData.route=true;

                    //get the data of the route and send to the maps
                    Bundle dados=new Bundle();
                    dados.putSerializable("rota",spliter[0]);
                    dados.putSerializable("data",spliter[1]);
                    Intent intent= new Intent(getContext(), MapsActivity.class);
                    intent.putExtras(dados);
                    startActivity(intent);
                }
            });
        }
        return view;
    }
}
