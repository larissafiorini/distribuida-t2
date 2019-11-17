import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class GroupCoord{

    private static int mutex = 1;
    private static ArrayList<Stats> filaMutex = new ArrayList<Stats>();
    private static int vazio = 10;
    private static ArrayList<Stats> filaVazio = new ArrayList<Stats>();
    private static int cheio = 0;
    private static ArrayList<Stats> filaCheio = new ArrayList<Stats>();
    private static int numProducers = 0; // numero atual de produtores
    private static int numConsumers = 0; // numero atual de consumidores
    private static String currentBuffer = ""; // regiao critica
    private static byte[] receiveData = new byte[1024];
    private static byte[] sendData = new byte[1024];
    private static DatagramSocket serverSocket;
    private static DatagramSocket clientSocket;

    // Metodo para inicializar os sockets globais
    private static void initSocket(int socketType){
        try{
            
            if(socketType == 1){
                serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
                serverSocket.setSoTimeout(10000);
            }
            else{
                clientSocket = new DatagramSocket();
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.toString());
            exception.printStackTrace();
        }
    }

    // Metodo que performa a logica de um membro coordenador
    public static void execute(){

        try{

            initSocket(1);
            initSocket(2);
            
            while(true){

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);


                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                
                System.out.println("Mensagem recebida: "+sentence);

                String array[] = sentence.split("-");
                System.out.println(sentence);
                String request = array[0];
                int requesterId = Integer.parseInt(array[1]);
                String requesterIp = array[2];
                int requesterPort = Integer.parseInt(array[3]);

                Stats newRequester = new Stats(requesterId,requesterIp,requesterPort);

                if(request.equals("PVAZIO")){
                    System.out.println("PVAZIO");
                    P(1,newRequester);
                }

                else if(request.equals("VVAZIO")){
                    System.out.println("VVAZIO");

                    V(1,newRequester);
                }

                else if(request.equals("PCHEIO")){
                    System.out.println("PCHEIO");

                    P(2,newRequester);
                }

                else if(request.equals("VCHEIO")){
                    System.out.println("VCHEIO");

                    V(2,newRequester);
                }

                else if(request.equals("PMUTEX")){
                    System.out.println("PMUTEX");

                    P(3,newRequester);
                }

                else if(request.equals("VMUTEX")){
                    System.out.println("VMUTEX");

                    V(3,newRequester);
                }

                else if(request.equals("PRODUCE")){
                    System.out.println("Pedido para produzir");
                    currentBuffer += "X";
                }

                else if(request.equals("CONSUME")){
                    System.out.println("Pedido para consumir");

                    currentBuffer = currentBuffer.substring(1);
                }

                else if(request.equals("ACK")){ // se o pedido for de apenas um ACK do coord
                    String message = "ACK"; // envio um ACK para o requisitor falando sua funcao no sistema
                    System.out.println("Novo membro detectado.");

                    if(numProducers > numConsumers){
                        System.out.println("Funcao atribuida CONSUMER.");

                        message += "-CONSUMER";
                        numConsumers+=1;
                    }
                    else{
                        System.out.println("Funcao atribuida PRODUCER.");

                        message += "-PRODUCER";
                        numProducers+=1;
                    }
                    sendData = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                    clientSocket.send(sendPacket);
                }

                else if(request.equals("ELECTION")){ // se o pedido for de uma nova eleicao
                    
                    // comparo nossos ids e envio uma resposta
                    String message = "STATUS-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requesterIp), requesterPort);
                    clientSocket.send(sendPacket);

                    if(requesterId < Bully.myStats.idNumber){
                        /**
                        • Quando processo recebe msg de eleição de membros com ID mais baixa
                        • Envia OK para remetente para indicar que está vivo e convoca eleição
                        */
                        callElection();
                    }
                }

                else if(request.equals("NEWCOORD")){ // novo coordenador na area
                    serverSocket.close();
                    clientSocket.close();
                    GroupMember.execute(requesterId);
                    return;
                }

                else{ // se eu receber uma mensagem que nao reconheço 
                    throw new Exception("Mensagem Invalida Recebida: "+request); // lanco uma nova excessao
                }
            }
        }
        catch(SocketTimeoutException e){
            serverSocket.close();
            clientSocket.close();
            execute();
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.toString());
            exception.printStackTrace();
        }
    }

    // Sequestra um recurso do semaforo
    private static void P(int numSemaforo, Stats requester){

        try{
            switch(numSemaforo){
                case 1: // PVAZIO
        
                    if(vazio == 0){
                        filaVazio.add(requester);
                        // enviar mensagem avisando que ele nao ganhou acesso
                        String message = "STATUS-ONQUEUE";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    else{ 
                        vazio -= 1;
                        // enviar mensagem avisando que ele ganhou acesso
                        String message = "STATUS-HASACCESS";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    
                    break;
                case 2: // PCHEIO
                    if(cheio == 0){
                        filaCheio.add(requester);
                        // enviar mensagem avisando que ele nao ganhou acesso
                        String message = "STATUS-ONQUEUE";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    else{ 
                        cheio -= 1;
                        // enviar mensagem avisando que ele ganhou acesso
                        String message = "STATUS-HASACCESS";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    break;
                case 3: // PMUTEX
                    if(mutex == 0){
                        filaMutex.add(requester);
                        // enviar mensagem avisando que ele nao ganhou acesso
                        String message = "STATUS-ONQUEUE";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    else{ 
                        mutex -= 1;
                        // enviar mensagem avisando que ele ganhou acesso
                        String message = "STATUS-HASACCESS";
                        sendData = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    break;
                default:
                    break;
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.toString());
            exception.printStackTrace();
        }
    }

    // Libera um recurso do semaforo
    private static void V(int numSemaforo, Stats requester){

        try{ 
            switch(numSemaforo){
                case 1: // VVAZIO
                    if(filaVazio.size() > 0){ // fila nao vazia
                        Stats vazioOwner = filaVazio.get(0);
                        // envio mensagem avisando o novo dono do mutex
                        String message = "STATUS-HASACCESS";
                        sendData = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(vazioOwner.ipAddress), vazioOwner.portNumber);
                        clientSocket.send(sendPacket);
                        filaVazio.remove(0);
                    }
                    else{ // fila vazia
                        vazio += 1;
                    }
                    // envio mensagem avisando que liberei a tranca conforme ele pediu
                    String message = "STATUS-LOSTACCESS";
                    sendData = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                    clientSocket.send(sendPacket);
                    break;
                case 2: //VCHEIO

                    if(filaCheio.size() > 0){ // fila nao vazia
                        Stats cheioOwner = filaCheio.get(0);
                        // envio mensagem avisando o novo dono do mutex
                        message = "STATUS-HASACCESS";
                        sendData = message.getBytes();
                            
                        sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(cheioOwner.ipAddress), cheioOwner.portNumber);
                        clientSocket.send(sendPacket);
                        filaCheio.remove(0);
                    }
                    else{ // fila vazia
                        cheio += 1;
                    }
                    // envio mensagem avisando que liberei a tranca conforme ele pediu
                    message = "STATUS-LOSTACCESS";
                    sendData = message.getBytes();
                        
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                    clientSocket.send(sendPacket);
                    break;
                case 3: //VMUTEX
                    if(filaMutex.size() > 0){ // fila nao vazia
                        Stats mutexOwner = filaMutex.get(0);
                        // envio mensagem avisando o novo dono do mutex
                        message = "STATUS-HASACCESS";
                        sendData = message.getBytes();
                            
                        sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(mutexOwner.ipAddress), mutexOwner.portNumber);
                        clientSocket.send(sendPacket);
                        filaMutex.remove(0);
                    }
                    else{ // fila vazia
                        mutex += 1;
                    }
                    // envio mensagem avisando que liberei a tranca conforme ele pediu
                    message = "STATUS-LOSTACCESS";
                    sendData = message.getBytes();
                        
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                    clientSocket.send(sendPacket);
                    break;
                default:
                    break;
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.toString());
            exception.printStackTrace();
        }
    }

    // Metodo que performa uma eleicao
    public static void callElection(){
        try{
            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                try{
                    if(Bully.neighbours.get(i).idNumber > Bully.myStats.idNumber){ //envia msg de eleição para todos os processos com IDs maiores que o dele
                        
                        String message = "ELECTION-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                        sendData = message.getBytes();
                        
                        Stats candidate = Bully.neighbours.get(i);
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(candidate.ipAddress), candidate.portNumber);
                        clientSocket.send(sendPacket);
                        // agora preciso receber a mensagem de retorno
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        String array[] = sentence.split("-");

                        int idNumber = Integer.parseInt(array[1]);
                        if(idNumber > Bully.myStats.idNumber){ // Se algum processo com ID maior responde, ele desiste
                            return;
                        }
                    }
                }
                catch(SocketTimeoutException e){
                    continue;
                }
            }
            //Se ninguém responde, P vence eleição e continua coordenador
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.toString());
            exception.printStackTrace();
        }
    }
}