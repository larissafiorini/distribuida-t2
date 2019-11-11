import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GroupCoord{

    public static File criticalRegionFile = new File("/regiao-critica/arquivo.txt"); // regiao critica
    public static boolean locked = false;   // arquivo com dono ou nao
    public static Stats lockOwner = null; // dono atual do acesso ao arquivo 
    public static int numProducers = 0; // numero atual de produtores
    public static int numConsumers = 0; // numero atual de consumidores
    public static String currentBuffer = ""; // representa a quantia de produtos no arquivo para serem consumidos

    // Metodo que performa a logica de um membro coordenador
    public static void execute(){

        try{
            DatagramSocket serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
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
                        // enviar mensagem avisando que ele nao ganhou acesso
                        String message = "STATUS-ACCESSDENIED";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                        clientSocket.send(sendPacket);
                    }
                    else{ // e o arquivo nao esta trancado
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
                    String message = "ACK"; // envio um ACK para o requisitor falando sua funcao no sistema
                    if(numProducers > numConsumers){
                        message += "-CONSUMER";
                        numConsumers+=1;
                    }
                    else{
                        message += "-PRODUCER";
                        numProducers+=1;
                    }
                    sendData = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                    clientSocket.send(sendPacket);
                }
                else if(request.equals("PRODUCE")){ // se o pedido for para produzir no arquivo da area critica
                    if(currentBuffer.length() < 10){ // tamanho maximo de produtos sobrando e 10
                        // escreve mais um X no arquivo
                        FileWriter fileWriter = new FileWriter(criticalRegionFile);
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        currentBuffer += "X";
                        printWriter.print(currentBuffer);
                        printWriter.close();
                        // envio mensagem avisando que foi produzido um novo X
                        String message = "STATUS-PRODUCED";
                        sendData = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                        clientSocket.send(sendPacket);
                    }
                    else{ // ja existem 10 produtos sobrando
                        // envio mensagem avisando que nao foi produzido um novo X
                        String message = "STATUS-FULL";
                        sendData = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                        clientSocket.send(sendPacket);
                    }
                }
                else if(request.equals("CONSUME")){ // se o pedido for para consumir no arquivo da area critica
                    if(currentBuffer.length() > 0){ // tamanho minimo de produtos e 0
                        // consome um X do arquivo
                        FileWriter fileWriter = new FileWriter(criticalRegionFile);
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        currentBuffer = currentBuffer.substring(1);
                        printWriter.print(currentBuffer);
                        printWriter.close();
                        // envio mensagem avisando que foi consumido um novo X
                        String message = "STATUS-CONSUMED";
                        sendData = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                        clientSocket.send(sendPacket);
                    }
                    else{ // nao existem produtos sobrando
                        // envio mensagem avisando que nao foi consumido um novo X
                        String message = "STATUS-EMPTY";
                        sendData = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                        clientSocket.send(sendPacket);
                    }
                }
                else if(request.equals("ELECTION")){ // se o pedido for de uma nova eleicao
                    // TODO
                    // comparo nossos ids e envio uma resposta?
                }
                else{ // se eu receber uma mensagem que nao reconheço 
                    throw new Exception("Mensagem Invalida Recebida: "+request); // lanco uma nova excessao
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
}