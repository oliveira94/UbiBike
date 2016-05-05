package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dase5.db";
    private static final String TABLE_NAME_DATA = "mydata";
    private static final String TABLE_NAME_CHAT = "mychat";
    private static final String TABLE_NAME_FRIENDS = "myfriends";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_FRIENDS = "friends";
    private static final String COLUMN_POINTS = "points";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_RECEIVER = "receiver";
    private static final String COLUMN_MESSAGE = "message";

    SQLiteDatabase db;

    private static final String TABLE_CREATE_DATA = "create table mydata (id integer primary key not null , " +
       "username text not null , name text not null , points integer , age integer not null);";

    private static final String TABLE_CREATE_CHAT = "create table mychat (id integer primary key not null , " +
            "sender text not null , receiver text not null , message text not null);";

    private static final String TABLE_CREATE_FRIENDS = "create table myfriends (id integer primary key not null , " +
            "username text not null , friends text);";

    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_DATA);
        db.execSQL(TABLE_CREATE_CHAT);
        db.execSQL(TABLE_CREATE_FRIENDS);
        this.db = db;
    }

    //method that insert a new user data in the database
    public void insertUserData(String name, int age, String username){

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "select * from mydata";
        Cursor cursor = db.rawQuery(query, null);

        int counter = cursor.getCount();


        values.put(COLUMN_ID, counter);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AGE,age);
        values.put(COLUMN_USERNAME,username);

        values.put(COLUMN_POINTS, 0);

        db.insert(TABLE_NAME_DATA, null, values);
        db.close();

    }

    public void insertFriends(String user, String friends){
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "select * from myfriends";
        Cursor cursor = db.rawQuery(query, null);

        int counter = cursor.getCount();

        values.put(COLUMN_ID, counter);
        values.put(COLUMN_USERNAME, user);
        values.put(COLUMN_FRIENDS,friends);

        db.insert(TABLE_NAME_FRIENDS, null, values);
        db.close();
    }

    public void addFriend(String user, String newFriend)
    {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> friendsList;
        String listOfFriends = getListOfFriends(user);

        if(listOfFriends.equals("noFriends"))
            friendsList = new ArrayList<>();
        else
            friendsList = gson.fromJson(listOfFriends, type);

        friendsList.add(newFriend);
        String newFriendsList= gson.toJson(friendsList);

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIENDS, newFriendsList);
        db.update(TABLE_NAME_FRIENDS, values, COLUMN_USERNAME + "='" + user + "'", null);
        db.close();
    }

    public String getListOfFriends(String user){
        db = this.getReadableDatabase();
        String friendsList = "noFriends";

        String query1 = "select username, friends from "+ TABLE_NAME_FRIENDS;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;

        if(cursor1.moveToFirst()){
            do{
                x = cursor1.getString(0);
                if(x.equals(user)){
                    friendsList = cursor1.getString(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }

        return friendsList;
    }

    //method to store a message between a sender and a receiver in the database
    public void sendNewMessage(ExchangeMessages exchangeMessages){
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "select * from mychat";
        Cursor cursor = db.rawQuery(query, null);

        int counter = cursor.getCount();


        values.put(COLUMN_ID, counter);
        values.put(COLUMN_SENDER, exchangeMessages.getSender());
        values.put(COLUMN_RECEIVER,exchangeMessages.getReceiver());
        values.put(COLUMN_MESSAGE, exchangeMessages.getMessage());

        db.insert(TABLE_NAME_CHAT, null, values);
        db.close();
    }


    //get points from a username
    public int PointsFromUser(String username){
        db = this.getReadableDatabase();
        String query1 = "select username, points from "+ TABLE_NAME_DATA;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;
        int y = 0;

        if(cursor1.moveToFirst()){

            do{
                x = cursor1.getString(0);
                if(x.equals(username)){
                    y = cursor1.getInt(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        return y;
    }

    //get points from a username
    public int AgeFromUser(String username){
        db = this.getReadableDatabase();
        String query1 = "select username, age from "+ TABLE_NAME_DATA;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;
        int y = 0;

        if(cursor1.moveToFirst()){

            do{
                x = cursor1.getString(0);
                if(x.equals(username)){
                    y = cursor1.getInt(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        return y;
    }

    //verify if a username already exists when creating account
    public boolean checkUsername(String username){
        boolean existsOrNot = false;
        db = this.getReadableDatabase();
        String query1 = "select username from "+ TABLE_NAME_DATA;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;

        if(cursor1.moveToFirst()){

            do{
                x = cursor1.getString(0);
                if(x.equals(username)){
                    existsOrNot = true;
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        return existsOrNot;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String queryData = "DROP TABLE IF EXISTS " + TABLE_NAME_DATA;
        db.execSQL(queryData);
        String queryChat = "DROP TABLE IF EXISTS " + TABLE_NAME_CHAT;
        db.execSQL(queryChat);
        String queryFriends = "DROP TABLE IF EXISTS " + TABLE_NAME_FRIENDS;
        db.execSQL(queryFriends);
        this.onCreate(db);
    }
}
