import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;

public class GroupCoord{

    public static File criticalRegionFile = new File("/regiao-critica/arquivo.txt");
    public static boolean locked = true;  
    public static Stats lockOwner = null; 

    // Metodo que performa a logica de um membro coordenador
    public static void execute(){

        try{
            DatagramSocket serverSocket = new DatagramSocket(myStats.portNumber,InetAddress.getByName(myStats.ipAddress));
            DatagramSocket clientSocket = new DatagramSocket();
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            while(true){
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                String array[] = sentence.split("-");
                String request = array[0];
                int requesterId = Integer.parseInt(array[1]);
                String requesterIp = array[2];
                int requesterPort = Integer.parseInt(array[3]);
                Stats newRequester = new Stats(requesterId,requesterIp,requesterPort);

                if(request.equals("ENTER")){ // se o pedido for de entrada na area critica
                    if(locked == true){ // e o arquivo esta trancado
                        String message = "STATUS-ACCESSDENIED";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                        clientSocket.send(sendPacket);
                    }
                    else{ // senao
                        locked = true; // ganha o acesso a area critica
                        lockOwner = newRequester;
                        // enviar mensagem avisando que ele ganhou acesso
                        String message = "STATUS-HASACCESS";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                        clientSocket.send(sendPacket);
                    }
                }
                else if(request.equals("LEAVE")){ // se o pedido for de saida da area critica
                    locked = false; // libero o arquivo
                    lockOwner = null; // removo o dono do acesso ao arquivo
                    
                    // envio mensagem avisando que liberei a tranca conforme ele pediu
                    String message = "STATUS-LOSTACCESS";
                    sendData = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                    clientSocket.send(sendPacket);
                }
                else if(request.equals("ACK")){ // se o pedido for de apenas um ACK do coord
                    String message = "ACK"; // envio um ACK para o requisitor
                    sendData = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                    clientSocket.send(sendPacket);
                }
                else if(request.equals("ELECTION")){ // se o pedido for de uma nova eleicao
                    // TODO
                    // comparo nossos ids e envio uma resposta?
                }
                else{ // se eu receber uma mensagem que nao reconheço 
                    throw new Exception("Mensagem Invalida Recebida"); // lanco uma nova excessao
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
}