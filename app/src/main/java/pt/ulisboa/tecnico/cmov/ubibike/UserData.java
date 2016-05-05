package pt.ulisboa.tecnico.cmov.ubibike;

import android.app.Application;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UserData extends Application{


    public static int age, points;
    public static String name, username, IP, receiver, totalDistance;
    public static List listOfDevices = new ArrayList();
    public static List listOfIPs = new ArrayList();
    public static List listOfFriends = new ArrayList();



    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

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
