package pt.ulisboa.tecnico.cmov.ubibike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by utilizador on 14/04/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper {



    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mydata.db";
    private static final String TABLE_NAME = "mydata";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    SQLiteDatabase db;

    private static final String TABLE_CREATE = "create table mydata (id integer primary key not null , " +
       "username text not null , password text not null , name text not null , age integer not null);";



    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        this.db = db;
    }


    public void insertUserData(UserData userData){

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "select * from mydata";
        Cursor cursor = db.rawQuery(query, null);

        int counter = cursor.getCount();


        values.put(COLUMN_ID, counter);
        values.put(COLUMN_NAME, userData.getName());
        values.put(COLUMN_AGE,userData.getAge());
        values.put(COLUMN_USERNAME, userData.getUsername());
        values.put(COLUMN_PASSWORD, userData.getPassword());

        db.insert(TABLE_NAME, null, values);
        db.close();

    }

    public String searchPassword(String user){

        db = this.getReadableDatabase();
        String query1 = "select username, password from "+TABLE_NAME;
        Cursor cursor1;
        cursor1 = db.rawQuery(query1, null);
        String x,y;
        y = "Not Found";

        if(cursor1.moveToFirst()){

            do{
                x = cursor1.getString(0);
                if(x.equals(user)){
                    y = cursor1.getString(1);
                    break;
                }
            }
            while (cursor1.moveToNext());
        }
        return y;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(query);
        this.onCreate(db);
    }


}
