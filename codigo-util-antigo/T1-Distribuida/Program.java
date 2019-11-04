import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Program implements IProgram {  

   private static ArrayList<Client> clientList = new ArrayList<Client>();

   //Metodo que realiza o registro de um novo cliente e seus recursos
   public void register(HashMap<String, String> receivedFileDict, String ip) { 

      System.out.println("\n**************************************************"); 
      System.out.println("* Novo Cliente registrado:");
      System.out.println("* IP: "+ip);
      System.out.println("* Arquivos:");
      System.out.println("* ");

      Client client= new Client(ip, System.currentTimeMillis());
      client.fileDict = receivedFileDict;
      clientList.add(client);

      Set<Map.Entry<String,String>> set = clientList.get(clientList.size()-1).fileDict.entrySet();
      Iterator<Map.Entry<String,String>> iterator = set.iterator();
      while(iterator.hasNext()) {
         Map.Entry<String,String> mentry = (Map.Entry<String,String>)iterator.next();
         System.out.println("* Arquivo: "+ mentry.getKey() + " | Hash: "+ mentry.getValue());
      }
      System.out.println("**************************************************\n");
   }  

   //Metodo que verifica todas as listas de arquivos dos clientes por um filename ou hash code
   public ArrayList<Entry> requestFile(String name){

      ArrayList<Entry> list = new ArrayList<Entry>();
      System.out.println("\n**************************************************");
      System.out.println("* Arquivo ou hash requisitado: "+name);
      System.out.println("**************************************************\n");

      for(int i = 0; i < clientList.size(); i++){
         Set<Map.Entry<String,String>> set = clientList.get(i).fileDict.entrySet();
         Iterator<Map.Entry<String,String>> iterator = set.iterator();

         while(iterator.hasNext()) {
            Map.Entry<String,String> mentry = (Map.Entry<String,String>)iterator.next();
            String fileName = mentry.getKey().toString();
            String hash = mentry.getValue().toString();

            if(fileName.equals(name) || hash.equals(name)){
               list.add(new Entry(clientList.get(i).ip.toString(),fileName,hash));
            }
         }
      }
      return list;
   }

   //Metodo que lista todos os recursos disponiveis no servidor
   public ArrayList<String> requestList(){

      System.out.println("\n**************************************************");
      System.out.println("* Lista de recursos requisitada.");
      System.out.println("**************************************************\n");
      ArrayList<String> array = new ArrayList<String>();

      for(int i = 0; i < clientList.size(); i++){
         Set<Map.Entry<String,String>> set = clientList.get(i).fileDict.entrySet();
         Iterator<Map.Entry<String,String>> iterator = set.iterator();

         while(iterator.hasNext()) {

            Map.Entry<String,String> mentry = (Map.Entry<String,String>)iterator.next();
            String fileName = mentry.getKey().toString();
            String hash = mentry.getValue().toString();
            String output = "* Arquivo: "+fileName+" | Hash: "+hash;

            if(!array.contains(output)){
               array.add(output);
            }  
         }
      }
      return array;
   }

   // Verifica se tem algum client que demorou demais pra dizer oi
   public void verifyPings(){

      if(clientList.size() > 0){
         for (int i = 0; i < clientList.size(); i++){

            long now = System.currentTimeMillis();

            if(((now - clientList.get(i).life)/1000) > 30){

               System.out.println("\n**************************************************");
               System.out.println("* Cliente "+ clientList.get(i).ip+" demorou demais para dizer oi e foi removido.");
               System.out.println("**************************************************\n");
               clientList.remove(i);
            }
         }
      }
      else{
         return;
      }
   }

   public void overHeadVerify(){

      long lastPing = System.currentTimeMillis();
      while(true){

         long now = System.currentTimeMillis();

         if(((now - lastPing)/1000) >= 5){
            Thread t = new Thread() {
               public void run() {
                  verifyPings();
               }
            };
            t.start();
            lastPing = System.currentTimeMillis();
         }
      }
   }

   // Diz oi pro server
   public void ping(String clientName){

      for (int i = 0; i < clientList.size(); i++){

         if(clientList.get(i).ip.equals(clientName)){

            clientList.get(i).life = System.currentTimeMillis();
            System.out.println("\n**************************************************");
            System.out.println("* Cliente "+clientName+" disse oi.");
            System.out.println("**************************************************\n");
         }
      }
   }
}