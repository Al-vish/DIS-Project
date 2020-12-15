package fileserver;

import kdc.kdcInterface;
import serverdiscovery.ServerDiscoveryInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class FileServer {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RED = "\u001B[31m";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print(ANSI_CYAN + "Enter Server Id (No space are allowed) : " + ANSI_RESET);
        String serviceId = in.nextLine();
        while(serviceId.contains(" ")){
            System.out.println(ANSI_RED + "Error : No Space are allowed" + ANSI_RESET);
            System.out.print(ANSI_CYAN + "Enter Server Id Again : " + ANSI_RESET);
            serviceId = in.nextLine();
        }
        System.out.print(ANSI_CYAN + "Enter Path of file System : " + ANSI_RESET);
        String path = in.nextLine();
        System.out.print(ANSI_CYAN + "Enter Port No : " + ANSI_RESET);
        Integer port = Integer.parseInt(in.nextLine());

        try {
            ServerDiscoveryInterface serverDiscovery = (ServerDiscoveryInterface) Naming.lookup("rmi://localhost:1900"+"/command");
            serverDiscovery.registerMe(serviceId, port);
            kdcInterface kdc = (kdcInterface) Naming.lookup("rmi://localhost:1950"+"/command");
            String privateKey = kdc.registerMe(serviceId);

            Command obj = new CommandImp();
            obj.setServerId(serviceId);
            obj.setPrivateKey(privateKey);
            obj.setPath(path);
            LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://localhost:" + port + "/command", obj);
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
