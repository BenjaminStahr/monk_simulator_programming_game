package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface NetworkController extends Remote
{
    public void sendRequest(String code, String territory, UUID studentIdentifier) throws RemoteException;

    public Request getOldestRequest() throws RemoteException;

    public void setTutorAnswer(String code, String territory, UUID actualStudent) throws RemoteException;

    public boolean isProcessed(UUID identifier)throws RemoteException;

    public Request getTutorAnswer(UUID identifier) throws RemoteException;
}
