package pt.ulisboa.tecnico.cmov.ubibike.DataBase;

public class ExchangeMessages {
    String message, sender, receiver;

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public void setSender(String sender){
        this.sender = sender;
    }

    public String getSender(){
        return this.sender;
    }

    public void setReceiver(String receiver){
        this.receiver = receiver;
    }

    public String getReceiver(){
        return this.receiver;
    }

}
