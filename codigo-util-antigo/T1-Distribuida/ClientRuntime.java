import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class ClientRuntime {

    public static final int port = 6000;
    public static String ip;
    public static Registry registry; 
    public static IProgram stub; 

    public ClientRuntime(String ip, String serverIp){
        try{
            System.setProperty("java.rmi.server.hostname",serverIp);
            this.ip = ip;
            this.registry = getRegistry(serverIp);
            this.stub = getStub();
            Client();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // Metodo que pega o registro
    public static Registry getRegistry(String serverIp){
        try{

            Registry registry = LocateRegistry.getRegistry(serverIp); 
            return registry;
        }
        catch (Exception e){

            System.err.println("Excecao no Client: " + e.toString()); 
            e.printStackTrace(); 
        }
        return null;
    }

    // Metodo que pega o stub
    public static IProgram getStub(){
        try {  
        
            IProgram stub = (IProgram) registry.lookup("Server"); 
            return stub;
        } 
        catch (Exception e) {

           System.err.println("Excecao no Client: " + e.toString()); 
           e.printStackTrace(); 
        } 
        return null;
    }

    // Metodo principal do cliente
    public static void Client(){
        
        registerToServer(); // Registrar no servidor

        try {
            Thread t = new Thread() { // Thread para enviar oi pro server
               public void run() {
                    sayHello();
               }
            };
            t.start();
            Thread t2 = new Thread(){ // Thread para receber requests de arquivos
                public void run(){
                    waitForRequests();
                }
            };
            t2.start();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }

        menu(); // Menu de interacao com o usuario
    }
  
    // A cada 10 segundos envia um oi pro server
    public static void sayHello(){

        long lastPing = System.currentTimeMillis();
        while(true){

            long now = System.currentTimeMillis();

            if(((now - lastPing)/1000) >= 10){
                try{
                    stub.ping(ip.toString());
                    lastPing = System.currentTimeMillis();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    // Cria o hash MD5 e retorna
    public static String encode(File file) {
        try {
           MessageDigest messageDigest = MessageDigest.getInstance("MD5");
           FileInputStream inputStream = new FileInputStream(file);
           DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);
           byte[] buffer = new byte[4096];
           while (digestInputStream.read(buffer) > -1) {
           }
           MessageDigest digest = digestInputStream.getMessageDigest();
           digestInputStream.close();
           byte[] md5 = digest.digest();
           StringBuilder sb = new StringBuilder();
           for (byte b : md5) {
              sb.append(String.format("%02X", b));
           }
           return sb.toString();
        } 
        catch (Exception e) {
           e.printStackTrace();
        }
        return null;
    }
  
    // Metodo que cria a lista de arquivos e chama o metodo remoto de registro no server
    public static void registerToServer(){

        File folder = new File("Files");
        File[] listOfFiles = folder.listFiles();
        HashMap<String, String> fileDict = new HashMap<String, String>();
  
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String hashCode = encode(file);
                fileDict.put(file.getName(), hashCode);
            }
        }
  
        try{

            stub.register(fileDict, ip);
        }
        catch (Exception e){
    
            System.err.println("Excecao no Client: " + e.toString()); 
            e.printStackTrace(); 
        }
    }

    // Metodo que faz a funcao de menu do usuario
    public static void menu(){

        Scanner in = new Scanner(System.in);
        while(true){

            System.out.println("\n**************************************************");
            System.out.println("* Digite o numero da acao que deseja:");
            System.out.println("* ");
            System.out.println("* 1. Solicitar lista de recursos");
            System.out.println("* ");
            System.out.println("* 2. Solicitar recurso especifico");
            System.out.println("* ");
            System.out.println("* 3. Sair");
            System.out.println("**************************************************\n");

            int choice = 0;
            try{
                choice = in.nextInt();
            }
            catch(Exception exception){
                System.out.println("* O valor digitado deve ser apenas um número do menu!");
            }
            
            try{
                if(choice == 1){

                    ArrayList<String> list = stub.requestList();
                    System.out.println("\n**************************************************");
                    System.out.println("* Lista de recursos:");
                    Iterator<String> itr = list.iterator();

                    while(itr.hasNext()){

                        String aux = itr.next().toString();
                        System.out.println(aux);
                    }
                    System.out.println("**************************************************\n");
                }
                else if(choice == 2){

                    System.out.println("\n* Digite o nome do recurso:");
                    String dump = in.nextLine();
                    String name = in.nextLine();

                    ArrayList<Entry> result = stub.requestFile(name);
                    if(result.size() > 0){

                        System.out.println("\n**************************************************");
                        System.out.println("* Recurso encontrado:");

                        for(int i = 0; i < result.size(); i++){

                            System.out.println("* "+(i+1)+":");
                            System.out.println("* IP: "+result.get(i).ip);
                            System.out.println("* Nome: "+result.get(i).fileName);
                            System.out.println("* Hash: "+result.get(i).hash);
                        }
                        System.out.println("**************************************************\n");

                        System.out.println("Escolha o numero do arquivo que deseja requisitar, ou 0 para cancelar.");
                        try{
                            choice = in.nextInt();
                        }
                        catch(Exception exception){
                            System.out.println("* O valor digitado deve ser apenas um número da lista ou 0!");
                        }

                        if(choice > 0){
                            handleTransfer(result, choice);
                        }
                    }
                    else{

                        System.out.println("\n**************************************************");
                        System.out.println("* Recurso nao encontrado.");
                        System.out.println("**************************************************\n");
                    }
                }
                else if(choice == 3){

                    in.close();
                    System.exit(0);
                }
                else{

                    System.out.println("\n**************************************************");
                    System.out.println("* O valor digitado deve ser apenas um número do menu!");
                    System.out.println("**************************************************\n");
                }
            }
            catch (Exception e){
    
                System.err.println("Excecao no Client: " + e.toString()); 
                e.printStackTrace(); 
            }
        }
    }

    // Envia os arquivos requisitados
    public static void waitForRequests() {

        try {
            
            DatagramSocket serverSocket = new DatagramSocket(port,InetAddress.getByName(ip));
            
            byte[] receiveData = new byte[1024];
            
            boolean flag = true;
            while(flag == true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                String array[] = sentence.split("-");

                String sender_ip = array[0];
                String fileName = array[1];
                System.out.println("\n**************************************************");
                System.out.println("Request recebido: " + sentence);
                System.out.println("Sender IP: " + sender_ip);
                System.out.println("Arquivo requisitado: " + fileName);
                System.out.println("**************************************************\n");
                
                try {
                    Thread t = new Thread() {
                       public void run() {
                            sendFile(sender_ip,fileName);
                       }
                    };
                    t.start();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            serverSocket.close();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Realiza o envio de um arquivo
    public static void sendFile(String sender_ip, String fileName){
        try{
            File file = new File("Files/"+fileName);
            Scanner input = new Scanner (file);
            
            System.out.println("\n**************************************************");
            System.out.println("Enviando arquivo: "+fileName);
            System.out.println("**************************************************\n");

            DatagramSocket clientSocket = new DatagramSocket();
            byte[] sendData = new byte[1024];
            while (input.hasNextLine()) {
                String message = ip + "-" + fileName + "-" + input.nextLine();

                sendData = message.getBytes();
                
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(sender_ip), port+2);
                clientSocket.send(sendPacket);
            }

            String message = ip + "-" + fileName + "-" + "ENDOFFILE";
            sendData = message.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(sender_ip), port+2);
            clientSocket.send(sendPacket);
            System.out.println("\n**************************************************");
            System.out.println("Envio do arquivo "+fileName+" concluido.");
            System.out.println("**************************************************\n");

            clientSocket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // Handler para requisicao de arquivos
    public static void handleTransfer(ArrayList<Entry> list, int choice){

        if(choice > list.size()){
            System.out.println("* O valor digitado deve ser apenas um número da lista ou 0!");
            return;
        }

        Entry entry = list.get(choice-1);
        String targetIp = entry.ip;
        String fileName = entry.fileName;

        try{

            DatagramSocket clientSocket = new DatagramSocket();
            byte[] sendData = new byte[1024];

            String message = ip + "-" + fileName;
            sendData = message.getBytes();
        
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(targetIp), port);
            clientSocket.send(sendPacket);

            clientSocket.close();

            DatagramSocket serverSocket = new DatagramSocket(port+2,InetAddress.getByName(ip));
            
            ArrayList<String> file = new ArrayList<String>();

            byte[] receiveData = new byte[1024];
            
            while(true) {

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                String array[] = sentence.split("-");
                String sender_ip = array[0];
                String filename = array[1];
                String message_data = array[2];
                System.out.println("Mensagem recebida: " + sentence);
                System.out.println("Sender IP: " + sender_ip);
                System.out.println("Filename: " + filename);
                System.out.println("Data: " + message_data);
                
                if(message_data.contains("ENDOFFILE")){

                    FileWriter fileWriter = new FileWriter("Files/"+fileName);
                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    
                    for(int i = 0; i < file.size(); i++){
                        printWriter.println(file.get(i)); 
                    }
                    printWriter.close();
                    break;
                }
                else{
                    file.add(message_data);
                }

            }

            serverSocket.close();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}