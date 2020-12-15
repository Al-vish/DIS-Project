package fileserver;

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

public class CommandImp extends UnicastRemoteObject implements Command {

    public CommandImp() throws RemoteException{
        super();
    }
    private String serverId;
    private String privateKey;
    Map<String,String> clientMap = new HashMap<>();
    private String path = "C:\\Users\\Shahzeb\\Documents\\DIS project\\file1\\System";
    private String absolutePath = path;

    @Override
    public void setPath(String path) {
        this.path = path;
        this.absolutePath = path;
    }

    @Override
    public String ping() throws RemoteException{
        return serverId;
    }

    @Override
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public void setServerId(String serverId) throws RemoteException {
        this.serverId = serverId;
    }

    @Override
    public String authenticateServer(String encRand, String encClientId, String encSessionKey, String encSesClientId) throws RemoteException {
        try {
            String clientId = AESUtils.decrypt(encClientId, privateKey);
            String sessionKey = AESUtils.decrypt(encSessionKey, privateKey);
            String rand = AESUtils.decrypt(encRand, sessionKey);
            String clientIdCheck = AESUtils.decrypt(encSesClientId, sessionKey);
            if(!clientId.equals(clientIdCheck)){
                return AESUtils.encrypt("null", sessionKey);
            }
            Integer rand2 = Integer.parseInt(rand) + 1;
            clientMap.put(clientId, sessionKey);
            return AESUtils.encrypt(rand2.toString(), sessionKey);
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
        return null;
    }

    private String ls(){
        File file = new File(path);
        String contents[] = file.list();
        String result = "";
        for(String content: contents){
            result = result + content + "\n";
        }
        return result;
    }

    public String cat(String name) throws RemoteException {
        // TODO Auto-generated method stub
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path + "\\" + name));
            String result = new String(encoded, StandardCharsets.US_ASCII);
            return result + "\n";
        } catch (IOException e) {
        }
        return "null";
    }

    public String isExist(String des) throws RemoteException {
        File f = new File(absolutePath + File.separator + des);
        if(f.exists()){
            return "true";
        }
        return "false";
    }

    @Override
    public String commandOperation(String encCommand, String ClientId) throws RemoteException {
        String sessionKey = clientMap.get(ClientId);
        try {
            String command = AESUtils.decrypt(encCommand, sessionKey);
            List<String> inputList = new ArrayList<>(Arrays.asList(command.split(" ")));
            switch (inputList.get(0)){
                case "ls": return AESUtils.encrypt(ls(),sessionKey);
                case "cat": return AESUtils.encrypt(cat(inputList.get(1)), sessionKey);
                case "is": return AESUtils.encrypt(isExist(inputList.get(1)), sessionKey);
                default: return AESUtils.encrypt("null", sessionKey);
            }

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
        try {
            return AESUtils.encrypt("null", sessionKey);
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
        return "null";
    }

    @Override
    public String createAndAdd(String encData, String encDes, String encName, String ClientId) throws RemoteException {
        String sessionKey = clientMap.get(ClientId);
        try {
            String data = AESUtils.decrypt(encData, sessionKey);
            String des = AESUtils.decrypt(encDes, sessionKey);
            String name = AESUtils.decrypt(encName, sessionKey);
            File f = new File(absolutePath + File.separator + des + File.separator + name);
            f.getParentFile().mkdirs();
            f.createNewFile();
            FileUtils.write(f, data, StandardCharsets.US_ASCII);
            return AESUtils.encrypt("true", sessionKey);

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
        } catch (IOException e) {
        }
        try {
            return AESUtils.encrypt("null", sessionKey);
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
        return "null";
    }
}
