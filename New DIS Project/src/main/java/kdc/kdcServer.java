package kdc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class kdcServer {
    public static void main(String[] args) {
        try {
            kdcInterface obj = new kdcInterfaceImp();
            obj.getServers();
            obj.getClient();
            LocateRegistry.createRegistry(1950);
            Naming.rebind("rmi://localhost:1950" + "/command", obj);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
