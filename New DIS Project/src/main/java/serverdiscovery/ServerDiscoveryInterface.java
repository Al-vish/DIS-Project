package serverdiscovery;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ServerDiscoveryInterface extends Remote {
    public void registerMe(String serverId, Integer port) throws RemoteException;
    public Map<Integer, String> getPortWithServerId() throws RemoteException;
}
