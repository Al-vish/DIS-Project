package kdc;

import org.apache.commons.io.FileUtils;
import utils.AESUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class kdcInterfaceImp extends UnicastRemoteObject implements kdcInterface {
    public kdcInterfaceImp() throws RemoteException {
        super();
    }

    String filename = "kdcRegisteredServer.txt";
    Map<String, String> keyMapServer = new HashMap<>();
    Map<String, String> keyMapClient = new HashMap<>();

    @Override
    public void getServers() throws RemoteException {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(filename));
            String data = new String(encoded, StandardCharsets.US_ASCII);
            if(data.equals(""))
                return;
            List<String> list = new ArrayList<>(Arrays.asList(data.split(",")));
            for(String element: list){
                keyMapServer.put(element.split(" ")[0],element.split(" ")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getClient() throws RemoteException {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get("kdcRegisteredClient.txt"));
            String data = new String(encoded, StandardCharsets.US_ASCII);
            if(data.equals(""))
                return;
            List<String> list = new ArrayList<>(Arrays.asList(data.split(",")));
            for(String element: list){
                keyMapClient.put(element.split(" ")[0],element.split(" ")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String registerMe(String serverId) throws RemoteException {
        for(Map.Entry<String, String> entry: keyMapServer.entrySet()){
            if(entry.getKey().equals(serverId)){
                return entry.getValue();
            }
        }
        File kdcRegisteredServer = new File(filename);

        String key = AESUtils.generateKey16();
        String content = serverId + " " + key;
        keyMapServer.put(serverId, key);
        try {
            FileUtils.write(kdcRegisteredServer, content + ",", StandardCharsets.US_ASCII, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public List<String> authenticateMe(String clientId, String serverId) throws RemoteException {
        String clientKey = keyMapClient.get(clientId);
        String serverKey = keyMapServer.get(serverId);
        List<String> module = new ArrayList<>();

        if(clientKey==null||serverKey==null){
            return module;
        }

        String sessionKey = AESUtils.generateKey16();
        try {
            module.add(AESUtils.encrypt(serverId, clientKey));
            module.add(AESUtils.encrypt(sessionKey, clientKey));

            String encryptedClientId = AESUtils.encrypt(clientId, serverKey);
            String encryptedSessionKey = AESUtils.encrypt(sessionKey, serverKey);

            module.add(AESUtils.encrypt(encryptedClientId,clientKey));
            module.add(AESUtils.encrypt(encryptedSessionKey,clientKey));
            return module;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return module;
    }
}
