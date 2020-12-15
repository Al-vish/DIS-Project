package client;

import fileserver.Command;
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
import java.util.Map;

public class MasterCommand {
    private Map<String, Map<String,String>> serverMap;
    private String clientId;

    public void setServerMap(Map<String, Map<String, String>> serverMap) {
        this.serverMap = serverMap;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String ls(){
        String answer = "";
        for(Map.Entry<String, Map<String,String>> entry: serverMap.entrySet()){
            Integer portServer = Integer.parseInt(entry.getValue().get("port"));
            String sessionKey = entry.getValue().get("sessionKey");
            try {
                Command command = (Command) Naming.lookup("rmi://localhost:"+portServer+"/command");
                String encAns = command.commandOperation(AESUtils.encrypt("ls",sessionKey), clientId);
                answer += AESUtils.decrypt(encAns, sessionKey);
            } catch (NotBoundException | MalformedURLException | RemoteException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
        return answer;
    }

    public String pwd(){
        return "System\n";
    }

    public String cat(String name){
        String result;
        for(Map.Entry<String, Map<String,String>> entry: serverMap.entrySet()){

            Integer portServer = Integer.parseInt(entry.getValue().get("port"));
            String sessionKey = entry.getValue().get("sessionKey");
            try {
                Command command = (Command) Naming.lookup("rmi://localhost:"+portServer+"/command");
                String encresult = command.commandOperation(AESUtils.encrypt("cat " + name, sessionKey),clientId);
                result = AESUtils.decrypt(encresult, sessionKey);
                if(!result.equals("null")) {
                    return result;
                }

            } catch (NotBoundException | MalformedURLException | RemoteException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Boolean cp(String name, String des){
        String data = cat(name);
        if(data==null){
            return false;
        }
        for(Map.Entry<String, Map<String,String>> entry: serverMap.entrySet()){
            Integer portServer = Integer.parseInt(entry.getValue().get("port"));
            String sessionKey = entry.getValue().get("sessionKey");
            try {
                Command command = (Command) Naming.lookup("rmi://localhost:"+portServer+"/command");
                String encResult = command.commandOperation(AESUtils.encrypt("is "+des,sessionKey),clientId);
                if(AESUtils.decrypt(encResult, sessionKey).equals("true")){
                    String encresult = command.createAndAdd(AESUtils.encrypt(data, sessionKey), AESUtils.encrypt(des, sessionKey),
                            AESUtils.encrypt(name, sessionKey), clientId);
                    if(AESUtils.decrypt(encResult, sessionKey).equals("true"))
                        return true;
                }
            } catch (NotBoundException | MalformedURLException | RemoteException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {

                e.printStackTrace();
            }
        }
        return false;
    }


}
