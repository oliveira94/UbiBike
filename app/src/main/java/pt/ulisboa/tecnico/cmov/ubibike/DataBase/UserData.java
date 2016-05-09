package pt.ulisboa.tecnico.cmov.ubibike.DataBase;

import android.app.Application;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;

public class UserData extends Application{

    public static int age, points;
    public static String name, username, IP, receiver;
    public static double totalDistance;
    public static List listOfDevices = new ArrayList();
    public static List listOfIPs = new ArrayList();
    public static ArrayList<String> listOfFriends = new ArrayList();
    public static ArrayList<Object> history = new ArrayList<>();
    public static boolean searchClicked = false;
    public static SecretKey secretKey;
    public static boolean beaconAround = false;
    public static boolean route = false;
    public static String serverAddress = "http://10.0.3.2:8080";

    //add a IP to a list of the IP in the network
    public void AddDeviceIPToList(String IP) {
        if (!listOfIPs.contains(IP)) {
            listOfIPs.add(IP);
            Toast.makeText(this, listOfIPs.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void AddDevicesNameToList(String device) {

        if (!listOfDevices.contains(device)) {
            listOfDevices.add(device);
            Toast.makeText(this, listOfDevices.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    //get Ip through device name
    public void GetDeviceIP(String devicename) {
        int positonInList = listOfDevices.indexOf(devicename);
        IP = String.valueOf(listOfIPs.get(positonInList));
    }

    //get name device through ip
    public void GetName(String ip) {
        int positonInList = listOfIPs.indexOf(ip);
        receiver = String.valueOf(listOfDevices.get(positonInList));
    }

    public void AddFriend(String friend) {
        if (!listOfFriends.contains(friend)) {
            listOfFriends.add(friend);
        }
    }

    public List getListOfFriends()
    {
        return listOfFriends;
    }

}
