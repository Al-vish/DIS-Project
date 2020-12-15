package kdc;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface kdcInterface extends Remote {
    public String registerMe(String serverId) throws RemoteException;
    public void getServers() throws RemoteException;
    public void getClient() throws RemoteException;
    public List<String> authenticateMe(String clientId, String serverId) throws RemoteException;
}
