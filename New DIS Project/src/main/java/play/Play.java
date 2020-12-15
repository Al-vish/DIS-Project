package play;

import kdc.kdcInterface;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Play {
    public static void main(String[] args) {

        String clientId = "client002";
        try {
            kdcInterface kdc = (kdcInterface) Naming.lookup("rmi://localhost:1950"+ "/command");
            List<String> module = kdc.authenticateMe(clientId,"server001");
            if(module.size()!=4){
                System.out.println("Authentication Failed");
                return;
            }
            System.out.println(AESUtils.decrypt(module.get(0),"CuoR6vnMkDPKoVDH"));
            System.out.println(AESUtils.decrypt(module.get(1),"CuoR6vnMkDPKoVDH"));
            String encryptedServerId = AESUtils.decrypt(module.get(2),"CuoR6vnMkDPKoVDH");
            String encryptedSessionKey = AESUtils.decrypt(module.get(3),"CuoR6vnMkDPKoVDH");
            System.out.println(encryptedServerId);
            System.out.println(encryptedSessionKey);
            System.out.println(AESUtils.decrypt(encryptedServerId, "abjyuiop67uyghnj"));
            System.out.println(AESUtils.decrypt(encryptedSessionKey, "abjyuiop67uyghnj"));

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

//        String clientId = "clientId2";
//        try {
//            kdcInterface kdc = (kdcInterface) Naming.lookup("rmi://localhost:1950"+ "/command");
//            String key = kdc.registerMe(clientId);
//            System.out.println(key);
//        } catch (NotBoundException e) {
//            e.printStackTrace();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }
}
