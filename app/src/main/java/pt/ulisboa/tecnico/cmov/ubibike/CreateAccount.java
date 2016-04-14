package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class CreateAccount extends AppCompatActivity {

    DataBaseHelper helper = new DataBaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
    }

    public void SignUpToDraw(View view) {

        EditText name = (EditText)findViewById(R.id.nameid);
        EditText age = (EditText)findViewById(R.id.ageid);
        EditText username = (EditText)findViewById(R.id.userid);
        EditText pass1 = (EditText)findViewById(R.id.pass1id);
        EditText pass2 = (EditText)findViewById(R.id.pass2id);

        String Iname = name.getText().toString();
        String Iage = age.getText().toString();
        String Iusername = username.getText().toString();
        String Ipass1 = pass1.getText().toString();
        String Ipass2 = pass2.getText().toString();

        if(!Ipass2.equals(Ipass1)){
            //Passwords don't match
            Toast password = Toast.makeText(CreateAccount.this, "Passwords don't match", Toast.LENGTH_SHORT);
            password.show();
        }
        else
        {
            UserData userData = new UserData();
            userData.setName(Iname);
            userData.setAge(Integer.parseInt(Iage));
            userData.setUsername(Iusername);
            userData.setPassword(Ipass1);

            helper.insertUserData(userData);
            Toast password = Toast.makeText(CreateAccount.this, "Account created", Toast.LENGTH_SHORT);
            password.show();

            Intent i = new Intent(this,NaviagationDrawer.class);
            startActivity(i);
        }


    }

}
