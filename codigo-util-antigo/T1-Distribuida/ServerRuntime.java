import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerRuntime extends Program {

    public ServerRuntime(String ip){

        Server(ip);
    }

    public static void Server(String ip){
        try { 

            System.setProperty("java.rmi.server.hostname",ip);
            Program obj = new Program(); 
        
            IProgram stub = (IProgram) UnicastRemoteObject.exportObject(obj, 0);  
            
            Registry registry = LocateRegistry.getRegistry(); 
            
            registry.bind("Server", stub);  
            System.err.println("Server Pronto"); 

            Thread t = new Thread() {
                public void run() {
                    try {
                        stub.overHeadVerify();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
             };
             t.start();
        } 
        catch (Exception e) { 
            System.err.println("Excecao no Server: " + e.toString()); 
            e.printStackTrace(); 
        } 
    }
}