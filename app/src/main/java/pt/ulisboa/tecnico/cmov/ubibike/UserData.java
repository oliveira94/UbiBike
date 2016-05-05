package pt.ulisboa.tecnico.cmov.ubibike;

import android.app.Application;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UserData extends Application {

    int age, points;
    String name, username, password, IP, receiver, totalDistance;
    List listOfDevices = new ArrayList();
    List listOfIPs = new ArrayList();
    List listOfFriends = new ArrayList();

    public void setDistance(String distance)
    {
        this.totalDistance=distance;
    }
    public String getDistance()
    {
        return totalDistance;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return this.age;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {
        return this.points;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getIP() {
        return this.IP;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiver() {
        return this.receiver;
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
        if (!listOfDevices.contains(friend)) {
            listOfDevices.add(friend);
        }
    }

    public List getListOfFriends()
    {
        return listOfFriends;
    }



}
