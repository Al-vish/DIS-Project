package fileserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Command extends Remote {
    public void setPath(String path) throws RemoteException;
    public void setServerId(String serverId) throws RemoteException;
    public void setPrivateKey(String privateKey) throws RemoteException;
    public String ping() throws RemoteException;
    public String authenticateServer(String encRand, String encClientId, String encSessionKey, String encSesClientId) throws RemoteException;
    public String commandOperation(String encCommand, String ClientId) throws RemoteException;
    public String createAndAdd(String encData, String encDes, String encName, String ClientId) throws RemoteException;
}
