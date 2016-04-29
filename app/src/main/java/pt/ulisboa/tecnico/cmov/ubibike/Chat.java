package pt.ulisboa.tecnico.cmov.ubibike;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Chat extends AppCompatActivity {

    SQLiteDatabase db;
    DataBaseHelper helper = new DataBaseHelper(this);
    ExchangeMessages exchangeMessages = new ExchangeMessages();
    UserData userData = new UserData();
    String user = getIntent().getExtras().getString("UserInfo");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        updateMessages(user);
        Toast toast = Toast.makeText(Chat.this, user, Toast.LENGTH_SHORT);
        toast.show();

    }

    public void sendClickedChat(View view) {
        LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
        LinearLayout chatHorizontalLayout = new LinearLayout(this);

        //Moving the text to the new text box
        TextView chatText = new TextView(this);
        EditText entryText = (EditText) findViewById(R.id.textEntryChat);
        String text = entryText.getText().toString();

        //update the exchangeMessages
        exchangeMessages.setSender(user);
        exchangeMessages.setMessage(text);
        exchangeMessages.setReceiver("joao");

        //put the message in the database
        helper.sendNewMessage(exchangeMessages);

        chatText.setText(text);
        chatText.setTextSize(22);
        chatText.setTextColor(Color.BLACK);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        //Setting the parameters to the intended
        chatHorizontalLayout.setGravity(Gravity.RIGHT);

        //Adding the textView to the HorizontalLayout
        chatHorizontalLayout.addView(chatText, params);

        //Adding the whole HorizontalLayout to the VerticalLayout
        linearLayoutVertical.addView(chatHorizontalLayout);
    }

    public void updateMessages(String user){//TODO update to receive user
        LinearLayout linearLayoutVertical = (LinearLayout) findViewById(R.id.idChatLinearVertical);
        LinearLayout chatHorizontalLayout = new LinearLayout(this);

        db = helper.getReadableDatabase();
        String query1 = "select sender, receiver, message from mychat";
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String sender, receiver, message;

        if(cursor1.moveToFirst()){

            do{
                sender = cursor1.getString(0);

                receiver = cursor1.getString(1);

                message = cursor1.getString(2);
                TextView chatText = null;
                chatText.setText(message);
                chatText.setTextSize(22);
                chatText.setTextColor(Color.BLACK);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                String isSender = SenderOrReceiver(sender);
                if(isSender.equals("sender")){
                    chatHorizontalLayout.setGravity(Gravity.RIGHT);
                }else {//TODO where we need to put a else if where the receiver will be us, and if yes, will print
                    chatHorizontalLayout.setGravity(Gravity.LEFT);
                }
                chatHorizontalLayout.addView(chatText, params);
                linearLayoutVertical.addView(chatHorizontalLayout);
            }
            while (cursor1.moveToNext());
        }
        Toast toast = Toast.makeText(Chat.this, "finish", Toast.LENGTH_SHORT);
        toast.show();
    }

    public String SenderOrReceiver(String sender) {
        String WhoIs = null;
        if (user.equals(sender)) {
            WhoIs = "sender";
        }
        return WhoIs;
    }

}
