package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class CreateAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
    }

    public void SignUpToDraw(View view) {
        Intent i = new Intent(this,NaviagationDrawer.class);
        startActivity(i);
    }
}
