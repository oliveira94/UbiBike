package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actibity_main);

    }

    public void SignInClicked(View view){
        Intent i = new Intent(this,LogIn.class);
        startActivity(i);
    }

    public void SignUpClicked(View view){
        Intent i = new Intent(this, CreateAccount.class);
        startActivity(i);
    }
}
