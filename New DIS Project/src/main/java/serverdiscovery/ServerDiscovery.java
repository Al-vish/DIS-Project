package serverdiscovery;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class ServerDiscovery {
    public static void main(String[] args) {
        try {
            ServerDiscoveryInterface obj = new ServerDiscoveryInterfaceImp();
            LocateRegistry.createRegistry(1900);
            Naming.rebind("rmi://localhost:1900" + "/command", obj);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
