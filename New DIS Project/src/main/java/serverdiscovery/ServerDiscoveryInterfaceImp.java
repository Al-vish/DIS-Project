package serverdiscovery;

import fileserver.Command;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerDiscoveryInterfaceImp extends UnicastRemoteObject implements ServerDiscoveryInterface {
    public ServerDiscoveryInterfaceImp() throws RemoteException{
        super();
    }

    Map<Integer, String> portWithServer = new HashMap<>();

    private void updateList(){
        for(Map.Entry<Integer, String> entry: portWithServer.entrySet()){
            try {
                Command command = (Command) Naming.lookup("rmi://localhost:"+ entry.getKey() + "/command");
                String serverId = command.ping();
                entry.setValue(serverId);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                portWithServer.remove(entry.getKey());
            }
        }
    }

    @Override
    public void registerMe(String serverId, Integer port) throws RemoteException {
        updateList();
        if(portWithServer.containsKey(port)||portWithServer.containsValue(serverId)){
            return;
        }
        portWithServer.put(port, serverId);
    }

    @Override
    public Map<Integer, String> getPortWithServerId() throws RemoteException {
        updateList();
        return portWithServer;
    }
}
