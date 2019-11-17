import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class Consumer{

    private static byte[] receiveData = new byte[1024];
    private static byte[] sendData = new byte[1024];
    private static DatagramSocket serverSocket;
    private static DatagramSocket clientSocket;
    private static Stats myCoord;

    // Metodo para inicializar os sockets globais
    private static void initSocket(int socketType){
        try{
            
            if(socketType == 1){
                serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
                serverSocket.setSoTimeout(10000); // 10 segundos acontece timeout
            }
            else{
                clientSocket = new DatagramSocket();
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no consumer: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    // Metodo que performa a logica de um consumidor
    public static void execute(int coordId){
        /*   
            Produtor:                           Consumidor:
                while (true)                        while (true)
                    decrementa(vazio);                  decrementa(cheio);
                    decrementa(mutex);                  decrementa(mutex);
                    escreveX()                          apagaX();
                    incrementa(mutex);                  incrementa(mutex);
                    incrementa(cheio);                  incrementa(vazio);
        */
        try{
            initSocket(1);
            initSocket(2);
            myCoord = Bully.neighbours.get(Bully.findMemberById(coordId)); // meu primeiro coord é o cara com id maior
            long lastPing = System.currentTimeMillis();
            while(true){ // fico enviando pedidos de acesso e consumo de tempos em tempos
                long now = System.currentTimeMillis();

                if(((now - lastPing)/1000) >= 10){ // a cada dez segundos eu tento entrar

                    System.out.println("Tentando consumir...");

                    lastPing = System.currentTimeMillis();
                    P(3);
                    P(2);
                    Consume();
                    V(2);
                    V(1);
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no consumer: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    private static void P(int numSemaforo){
        try{
            switch(numSemaforo){
                case 2: // PMUTEX
                    String message = "PMUTEX-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    System.out.println("P(mutex)...");

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    String array[] = sentence.split("-");
                    String command = array[0];
                    String value = array[1];

                    if(command.equals("STATUS")){
                        // se entrou apenas retorna
                        if(value.equals("HASACCESS")){

                            System.out.println("Entrei no semaforo...");

                            return;
                        }
                        // se ficou na fila fica esperando ate receber mensagem que entrou
                        while(true){

                            System.out.println("Estou na fila...");

                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            command = array[0];
                            value = array[1];
                            // se entrou apenas retorna
                            if(value.equals("HASACCESS")){

                                System.out.println("Entrei no semaforo...");

                                return;
                            }
                        }
                    }
                    else{ // se eu receber uma mensagem que nao reconheço 
                        throw new Exception("Mensagem Invalida Recebida: "+command); // lanco uma nova excessao
                    }   
                case 3: // PCHEIO
                    message = "PCHEIO-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    System.out.println("P(cheio)...");

                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    
                    sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    array = sentence.split("-");
                    command = array[0];
                    value = array[1];

                    if(command.equals("STATUS")){
                        // se entrou apenas retorna
                        if(value.equals("HASACCESS")){

                            System.out.println("Entrei no semaforo...");

                            return;
                        }
                        // se ficou na fila fica esperando ate receber mensagem que entrou
                        while(true){

                            System.out.println("Estou na fila...");

                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            command = array[0];
                            value = array[1];
                            // se entrou apenas retorna
                            if(value.equals("HASACCESS")){

                                System.out.println("Entrei no semaforo...");

                                return;
                            }
                        }
                    }
                    else{ // se eu receber uma mensagem que nao reconheço 
                        throw new Exception("Mensagem Invalida Recebida: "+command); // lanco uma nova excessao
                    }
                default:
                    break;
            }
        }
        catch(SocketTimeoutException e){
            // coord esta morto? Chamo eleicao
            callElection();
        }
        catch(Exception exception){
            System.out.println("Excecao no consumer: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void V(int numSemaforo){
        try{
            switch(numSemaforo){
                case 1: // VVAZIO
                    String message = "VVAZIO-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    System.out.println("V(vazio)...");

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                                        
                    return;
                case 2: // VMUTEX

                    System.out.println("V(mutex)...");

                    message = "VMUTEX-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                                        
                    return;
                default:
                    break;
            }
        }
        catch(SocketTimeoutException e){
            // coord esta morto? Chamo eleicao
            callElection();
        }
        catch(Exception exception){
            System.out.println("Excecao no consumer: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void Consume(){
        try{
            String message = "CONSUME-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
            sendData = message.getBytes();

            System.out.println("Consumindo...");

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
            clientSocket.send(sendPacket);
        }
        catch(Exception exception){
            System.out.println("Excecao no consumer: "+exception.getMessage());
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
            //Se ninguém responde, P vence eleição e torna-se coordenador
            //enviar mensagens a todos avisando sobre novo coord
            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                    
                String message = "NEWCOORD-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                sendData = message.getBytes();
                
                Stats neighbour = Bully.neighbours.get(i);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(neighbour.ipAddress), neighbour.portNumber);
                clientSocket.send(sendPacket);
                    
            }
            GroupCoord.execute();
        }
        catch(Exception exception){
            System.out.println("Excecao no consumer: "+exception.getMessage());
            exception.printStackTrace();
        }
    }
}