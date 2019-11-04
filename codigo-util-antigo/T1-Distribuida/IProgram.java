import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

//Interface dos metodos RMI
public interface IProgram extends Remote {  

   void register(HashMap<String, String> receivedFileDict, String ip) throws RemoteException;

   ArrayList<String> requestList() throws RemoteException;

   ArrayList<Entry> requestFile(String name) throws RemoteException;
   
   void verifyPings() throws RemoteException;

   void ping(String clientIp) throws RemoteException;

   void overHeadVerify() throws RemoteException;
} 