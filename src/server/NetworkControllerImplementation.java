package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class NetworkControllerImplementation extends UnicastRemoteObject implements NetworkController
{
    ArrayList<Request> requests = new ArrayList<>();

    public NetworkControllerImplementation() throws RemoteException { }

    // used by Student
    @Override
    public void sendRequest(String code, String territory, UUID studentIdentifier)
    {
        requests.add(new Request(code, territory, studentIdentifier));
    }


    // gets the oldest not processed request
    @Override
    public Request getOldestRequest() throws RemoteException
    {
        for(int i = 0; i < requests.size(); i++)
        {
            if(!requests.get(i).isProcessed())
            {
                return requests.get(i);
            }
        }
        return null;
    }

    @Override
    public void setTutorAnswer(String code, String territory, UUID actualStudent) throws RemoteException
    {
        for(int i = 0; i < requests.size(); i++)
        {
            if(actualStudent.equals(requests.get(i).getStudentIdentifier()))
            {
                requests.get(i).setCode(code);
                requests.get(i).setTerritory(territory);
                requests.get(i).setProcessed(true);
                break;
            }
        }
    }

    public boolean isProcessed(UUID identifier)
    {
        for(int i = 0; i < requests.size(); i++)
        {
            if(identifier.equals(requests.get(i).getStudentIdentifier()))
            {
                if(requests.get(i).isProcessed())
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Request getTutorAnswer(UUID identifier) throws RemoteException
    {
        for(int i = 0; i < requests.size(); i++)
        {
            if(identifier.equals(requests.get(i).getStudentIdentifier()))
            {
                Request request = requests.get(i);
                requests.remove(i);
                return request;
            }
        }
        return null;
    }


}
