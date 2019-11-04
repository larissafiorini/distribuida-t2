import java.net.*;
import java.io.*;
import java.util.ArrayList;

/*
* Programa que simula um roteador.
* 
* @author Guilherme Piccoli, Fernando Maioli, Igor Brehm
*/

public class Router {

    public static int findIndex(ArrayList<ArrayList<String>> matrix, String filename){
        int index = -1;
        for(int i = 0; i < matrix.size(); i++){
            if(matrix.get(i).get(0).equals(filename)){
                index = i;
            }
        }
        return index;
    }

    // Metodo main que inicia o programa router
    public static void main(String args[]) throws Exception {
        String ip = args[0];
        int port = 5723;
   		DatagramSocket serverSocket = new DatagramSocket(port,InetAddress.getByName(ip));
        DatagramSocket clientSocket = new DatagramSocket();
        
        File dir = new File(Integer.toString(port)); //pasta de destino das mensagens recebidas
        dir.mkdir();
        
        ArrayList<ArrayList<String>> matrix = new ArrayList<ArrayList<String>>();

        byte[] receiveData = new byte[1024];
        boolean flag = true;
        while(flag == true) {
	         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	         serverSocket.receive(receivePacket);
             
	         String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
             String[] array = sentence.split("-");
             String sender_ip = array[0];
             String destination_ip = array[1];
             String sender_port = array[2];
             String destination_port = array[3];
             String filename = array[4];
             String message_data = array[5];
             System.out.println("Mensagem recebida: " + sentence);
             System.out.println("Sender IP: " + sender_ip);
             System.out.println("Dest IP: " + destination_ip);
             System.out.println("Sender Port: " + sender_port);
             System.out.println("Dest Port: " + destination_port);
             System.out.println("Filename: " + filename);
             System.out.println("Data: " + message_data);
             
             byte[] sendData = new byte[1024];
             sendData = sentence.getBytes();
             
             if(!destination_ip.equals(ip)){ //mensagem para outra rede
                 System.out.println("Redirecionando mensagem para: " + destination_ip + "|" + "5723"); //enviar para o ip e porta do router da rede alvo
                 System.out.println("Rota: "+ ip + "/" + port + " -> " + destination_ip + "/" + "5723");
                 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destination_ip), 5723);
                 clientSocket.send(sendPacket);
             }
             else if(destination_ip.equals(ip) && (port != Integer.parseInt(destination_port))){ //mensagem para host desta rede
                 System.out.println("Redirecionando mensagem para: " + destination_ip + "|" + destination_port); //enviar para o ip e porta do host desta rede
                 System.out.println("Rota: "+ ip + "/" + port + " -> " + destination_ip + "/" + destination_port);
                 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destination_ip), Integer.parseInt(destination_port));
                 clientSocket.send(sendPacket);
             }
             else{ //mensagem para este router
                 int index = findIndex(matrix, filename);
                 if(findIndex(matrix,filename) == -1){
                    ArrayList<String> row = new ArrayList<String>();
                    row.add(filename);
                    row.add(message_data);
                    matrix.add(row);
                 }
                 else if(message_data.contains("ENDOFFILE")){
                     String path = System.getProperty("user.dir")+"/"+port+"/"+filename;
                     System.out.println("PATH: "+ path);
                     FileWriter fileWriter = new FileWriter(path);
                     PrintWriter printWriter = new PrintWriter(fileWriter);
                     
                     for(int i = 1; i < matrix.get(index).size(); i++){
                         printWriter.println(matrix.get(index).get(i)); 
                     }
                     printWriter.close();
                     matrix.remove(index);
                 }
                 else{
                     matrix.get(index).add(message_data);
                 }
             }
        }
        serverSocket.close();
   }
}
