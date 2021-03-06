package pt.ulisboa.tecnico.cmov.ubibike.DataBase;

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
    private static final String DATABASE_NAME = "dase16.db";
    private static final String TABLE_NAME_DATA = "mydata";
    private static final String TABLE_NAME_CHAT = "mychat";
    private static final String TABLE_NAME_FRIENDS = "myfriends";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_TOTALDISTANCE = "totaldistance";
    private static final String COLUMN_DEVICES = "devices";
    private static final String COLUMN_HISTORIC = "historic";
    private static final String COLUMN_FRIENDS = "friends";
    private static final String COLUMN_POINTS = "points";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_RECEIVER = "receiver";
    private static final String COLUMN_MESSAGE = "message";

    SQLiteDatabase db;

    private static final String TABLE_CREATE_DATA = "create table mydata (id integer primary key not null , " +
       "username text not null , name text not null , points integer , age integer not null , totaldistance real);";

    private static final String TABLE_CREATE_CHAT = "create table mychat (id integer primary key not null , " +
            "sender text not null , receiver text not null , message text not null);";

    private static final String TABLE_CREATE_FRIENDS = "create table myfriends (id integer primary key not null , " +
            "username text not null , friends text , historic text , devices text);";

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
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_POINTS, 100);
        values.put(COLUMN_TOTALDISTANCE, 0);

        db.insert(TABLE_NAME_DATA, null, values);
        db.close();
    }

    //method that is called when created a profile
    public void insertFriendsAndHistory(String user, String friends, String history, String devices){
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if(friends.equals("noFriends"))
        {
            String query = "select * from myfriends";
            Cursor cursor = db.rawQuery(query, null);
            db.delete(TABLE_NAME_FRIENDS, COLUMN_USERNAME + "='" + user + "'", null);

            int counter = cursor.getCount();

            values.put(COLUMN_ID, counter);
            values.put(COLUMN_USERNAME, user);
            values.put(COLUMN_FRIENDS,friends);
            values.put(COLUMN_HISTORIC, history);
            values.put(COLUMN_DEVICES,devices);

            db.insert(TABLE_NAME_FRIENDS, null, values);
            db.close();
        }
        else
        {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> listOfFriends = gson.fromJson(friends, type);
            for (String newFriend : listOfFriends)
                addFriend(user, newFriend);
        }
    }

    //method to add a friend to the DB
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

    //method to add a device to the DB
    public void addDevice(String user, String newDevice)
    {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> deviceList;
        String listOfDevices = getListOfDevices(user);

        if(listOfDevices.equals("noDevices"))
            deviceList = new ArrayList<>();
        else
            deviceList = gson.fromJson(listOfDevices, type);

        deviceList.add(newDevice);
        String newDeviceList= gson.toJson(deviceList);

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICES, newDeviceList);
        db.update(TABLE_NAME_FRIENDS, values, COLUMN_USERNAME + "='" + user + "'", null);
        db.close();
    }

    //have the list of the friends in a concatenated string
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

    //have the list of the devices in a concatenated string
    public String getListOfDevices(String user){
        db = this.getReadableDatabase();
        String devicesList = "noDevices";

        String query1 = "select username, devices from "+ TABLE_NAME_FRIENDS;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;

        if(cursor1.moveToFirst()){
            do{
                x = cursor1.getString(0);
                if(x.equals(user)){
                    devicesList = cursor1.getString(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        return devicesList;
    }

    //add a new route to the database
    public void addTrip(String user, String newTrip){
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> tripList;
        String listOfTrips = getListOfTrips(user);

        if(listOfTrips.equals("noTrips"))
            tripList = new ArrayList<>();
        else
            tripList = gson.fromJson(listOfTrips, type);

        tripList.add(newTrip);
        String newTripsList= gson.toJson(tripList);

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HISTORIC, newTripsList);
        db.update(TABLE_NAME_FRIENDS, values, COLUMN_USERNAME + "='" + user + "'", null);
        db.close();
    }

    //get a string with all routes done by the user
    public String getListOfTrips(String user){
        db = this.getReadableDatabase();
        String tripsList = "noTrips";

        String query1 = "select username, historic from "+ TABLE_NAME_FRIENDS;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;

        if(cursor1.moveToFirst()){
            do{
                x = cursor1.getString(0);
                if(x.equals(user)){
                    tripsList = cursor1.getString(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        return tripsList;
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

    //if is a add points AddOrSub is true
    public void ChangePoints(String username, int points){
        db = this.getReadableDatabase();
        int updatepoints = 0;

        String query1 = "select username, points from "+ TABLE_NAME_DATA;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;

        if(cursor1.moveToFirst()){
            do{
                x = cursor1.getString(0);
                if(x.equals(username)){
                    updatepoints = cursor1.getInt(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }

            updatepoints = updatepoints + points;


        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_POINTS, updatepoints);
        db.update(TABLE_NAME_DATA, values, COLUMN_USERNAME + "='" + username + "'", null);
        db.close();
    }

    //add a new distance from a route to totaldistance
    public void AddNewDistance(String username, double distance){
        db = this.getReadableDatabase();
        double TotalDistance = 0.0;

        String query1 = "select username, totaldistance from "+ TABLE_NAME_DATA;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x;

        if(cursor1.moveToFirst()){
            do{
                x = cursor1.getString(0);
                if(x.equals(username)){
                    TotalDistance = cursor1.getDouble(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        TotalDistance = TotalDistance + distance;

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TOTALDISTANCE, TotalDistance);
        db.update(TABLE_NAME_DATA, values, COLUMN_USERNAME + "='" + username + "'", null);
        db.close();
    }

    //get the total distance of the user
    public double getListOfTotalDistance(String user){
        db = this.getReadableDatabase();
        String query1 = "select username, totaldistance from "+ TABLE_NAME_DATA;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String username;
        double totalDistance = 0;

        if(cursor1.moveToFirst()){
            do{
                username = cursor1.getString(0);
                if(username.equals(user)){
                    totalDistance = cursor1.getDouble(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        return totalDistance;
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
