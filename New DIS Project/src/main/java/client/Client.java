package client;

import fileserver.Command;
import kdc.kdcInterface;
import serverdiscovery.ServerDiscoveryInterface;
import utils.AESUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Client {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print(ANSI_CYAN + "Enter Client Id (No Space are allowed) : " + ANSI_RESET);
        String clientId = in.nextLine();
        while(clientId.contains(" ")){
            System.out.println(ANSI_RED + "Error : No Space are allowed" + ANSI_RESET);
            System.out.print(ANSI_CYAN + "Enter Client Id Again : " + ANSI_RESET);
            clientId = in.nextLine();
        }

        System.out.print(ANSI_CYAN + "Enter Private key : " + ANSI_RESET);
        String privateKey = in.nextLine();

        try {
            ServerDiscoveryInterface serverDiscovery = (ServerDiscoveryInterface) Naming.lookup("rmi://localhost:1900"+"/command");
            Map<Integer, String> portWithServer = serverDiscovery.getPortWithServerId();
            System.out.println(ANSI_CYAN + "\nList of Current File Server : " + ANSI_RESET);
            for(Map.Entry<Integer, String> entry: portWithServer.entrySet()){
                System.out.println(ANSI_GREEN + entry.getValue() + ANSI_RESET);
            }

            System.out.print(ANSI_CYAN + "\nEnter the list of File Server you want to talk : " + ANSI_RESET);
            String servers = in.nextLine();
            List<String> serverList = new ArrayList<>(Arrays.asList(servers.split(" ")));

            //Check the server list is valid or not
            for(String serverId: serverList){
                if(!portWithServer.containsValue(serverId)){
                    System.out.println(ANSI_RED + "Error : Invalid File Server Id" + ANSI_RESET);
                    return;
                }
            }
            Map<String, Integer> serverWithPort = new HashMap<>();
            for(Map.Entry<Integer, String> entry: portWithServer.entrySet()){
                serverWithPort.put(entry.getValue(),entry.getKey());
            }

            Map<String, Map<String, String>> serverMap = new HashMap<>();
            kdcInterface kdc = (kdcInterface) Naming.lookup("rmi://localhost:1950"+"/command");
            for(String serverId: serverList){
                List<String> module = kdc.authenticateMe(clientId,serverId);
                if(module.size()!=4){
                    System.out.println(ANSI_RED + "Authentication Failed" + ANSI_RESET);
                    return;
                }
                String returnedServerId = AESUtils.decrypt(module.get(0),privateKey);
                if(!serverId.equals(returnedServerId)){
                    System.out.println(ANSI_RED + "Authentication Failed" + ANSI_RESET);
                    return;
                }
                String sessionKey = AESUtils.decrypt(module.get(1),privateKey);
                String encryptedClientId = AESUtils.decrypt(module.get(2),privateKey);
                String encryptedSessionKey = AESUtils.decrypt(module.get(3),privateKey);
                Integer portServer = serverWithPort.get(serverId);
                Integer rand = (int) (Math.random()*1000);
                String randStr = rand.toString();
                Command command = (Command) Naming.lookup("rmi://localhost:"+portServer+"/command");
                String encRandStr = command.authenticateServer(AESUtils.encrypt(randStr, sessionKey),
                        encryptedClientId, encryptedSessionKey, AESUtils.encrypt(clientId, sessionKey));
                if(encRandStr==null){
                    System.out.println(ANSI_RED + "Authentication Failed" + ANSI_RESET);
                }
                randStr = AESUtils.decrypt(encRandStr, sessionKey);
                if(randStr=="null"||(!rand.equals(Integer.parseInt(randStr)-1))){
                    System.out.println(ANSI_RED + "Authentication Failed" + ANSI_RESET);
                    return;
                }
                Map<String, String> temp = new HashMap<>();
                temp.put("port",portServer.toString());
                temp.put("sessionKey", sessionKey);
                serverMap.put(serverId, temp);
            }
            System.out.println(ANSI_GREEN + "Authentication Successful!\n" + ANSI_RESET);
            MasterCommand masterCommand = new MasterCommand();
            masterCommand.setServerMap(serverMap);
            masterCommand.setClientId(clientId);
            String answer;
            List<String> ls_list;
            outerLoop:
            while(true){
                System.out.print(ANSI_CYAN + ">>>" + ANSI_RESET);
                String input = in.nextLine();
                List<String> inputList = new ArrayList<>(Arrays.asList(input.split(" ")));
                switch (inputList.get(0)){
                    case "ls":
                        if(inputList.size()!=1){
                            System.out.println(ANSI_RED + "Error : With 'ls' command no argument is required\n" + ANSI_RESET);
                            break;
                        }
                        answer = masterCommand.ls();
                        ls_list = new ArrayList<>(Arrays.asList(answer.split("\n")));
                        for (String name: ls_list){
                            if(name.contains(".")){
                                System.out.println(ANSI_GREEN + name + ANSI_RESET);
                            }else{
                                System.out.println(ANSI_BLUE + name + ANSI_RESET);
                            }
                        }
                        System.out.println();
                        break;

                    case "pwd":
                        if(inputList.size()!=1){
                            System.out.println(ANSI_RED + "Error : With 'pwd' command no argument is required\n" + ANSI_RESET);
                            break;
                        }
                        answer = masterCommand.pwd();
                        System.out.println(ANSI_GREEN + answer + ANSI_RESET);
                        break;

                    case "cat":
                        if(inputList.size()!=2){
                            System.out.println(ANSI_RED + "Error : With 'cat' command 1 argument is required\n" + ANSI_RESET);
                            break;
                        }
                        answer = masterCommand.cat(inputList.get(1));
                        if(answer==null){
                            System.out.println(ANSI_RED + "Error : " + inputList.get(1) + " file does not exist\n" + ANSI_RESET);
                            break;
                        }
                        System.out.println(ANSI_GREEN + answer + ANSI_RESET);
                        break;

                    case "cp":
                        if(inputList.size()!=3){
                            System.out.println(ANSI_RED + "Error : With 'cp' command 2 argument is required\n" + ANSI_RESET);
                            break;
                        }
                        if(!masterCommand.cp(inputList.get(1),inputList.get(2))){
                            System.out.println(ANSI_RED + "Error : Unable to copy file, please check name and destination\n" + ANSI_RESET);
                        }else{
                            System.out.println();
                        }
                        break;

                    case "exit()":
                        break outerLoop;

                    default: System.out.println(ANSI_RED + "Error : Enter Valid Path\n" + ANSI_RESET);
                }
            }

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            System.out.println(ANSI_RED + "Authentication Failed" + ANSI_RESET);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            System.out.println(ANSI_RED + "Authentication Failed" + ANSI_RESET);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }
}
