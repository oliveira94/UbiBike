package pt.ulisboa.tecnico.cmov.ubibike;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LogIn extends AppCompatActivity {

    DataBaseHelper helper = new DataBaseHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

    }

    public void LogToNavigation(View view) {

        EditText username = (EditText)findViewById(R.id.lognameid);
        EditText pass = (EditText)findViewById(R.id.logpasswordid);

        String Iusername = username.getText().toString();
        String Ipass = pass.getText().toString();

        String Password = helper.searchPassword(Iusername);
        Toast toast1 = Toast.makeText(LogIn.this, Password, Toast.LENGTH_SHORT);
        toast1.show();


        if(Ipass.equals(Password)){
            Intent i = new Intent(this,NaviagationDrawer.class);
            i.putExtra("Username", Iusername);
            startActivity(i);
        }
        else
        {
            Toast toast = Toast.makeText(LogIn.this, "Username or password are wrong", Toast.LENGTH_SHORT);
            toast.show();
        }


    }
}
