import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class Producer{

    private static byte[] dataArray = new byte[1024];
    private static DatagramSocket serverSocket;
    private static DatagramSocket clientSocket;
    private static Stats myCoord;

    // Metodo para inicializar os sockets globais
    private static void initSocket(int socketType){
        try{
            
            if(socketType == 1){
                serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
            }
            else{
                clientSocket = new DatagramSocket();
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }

    // Metodo que performa a logica de um produtor
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
            myCoord = Bully.neighbours.get(coordId-1); // meu primeiro coord é o cara com id maior
            long lastPing = System.currentTimeMillis();

            while(true){ // fico enviando pedidos de acesso e producao de tempos em tempos
                long now = System.currentTimeMillis();

                if(((now - lastPing)/1000) >= 2){ // a cada dois segundos eu tento entrar

                    System.out.println("Tentando produzir...");

                    lastPing = System.currentTimeMillis();
                    P(1);
                    P(2);
                    Produce();
                    V(2);
                    V(3);
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }

    private static void P(int numSemaforo){
        try{
            switch(numSemaforo){
                case 1: // PVAZIO
                
                    System.out.println("P(vazio)...");

                    String message = "PVAZIO-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    dataArray = message.getBytes();
                    
                    DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);

                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(dataArray, dataArray.length);
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

                            receivePacket = new DatagramPacket(dataArray, dataArray.length);
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
                case 2: // PMUTEX
                    message = "PMUTEX-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    dataArray = message.getBytes();

                    System.out.println("P(mutex)...");

                    sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    receivePacket = new DatagramPacket(dataArray, dataArray.length);
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

                            receivePacket = new DatagramPacket(dataArray, dataArray.length);
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
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }

    public static void V(int numSemaforo){
        try{
            switch(numSemaforo){
                case 2: // VMUTEX
                    String message = "VMUTEX-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    dataArray = message.getBytes();

                    System.out.println("V(mutex)...");

                    DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(dataArray, dataArray.length);
                    serverSocket.receive(receivePacket);
                                        
                    return;
                case 3: // VCHEIO
                    message = "VCHEIO-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    dataArray = message.getBytes();

                    System.out.println("V(cheio)...");

                    sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    receivePacket = new DatagramPacket(dataArray, dataArray.length);
                    serverSocket.receive(receivePacket);
                                        
                    return;
                default:
                    break;
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }

    public static void Produce(){
        try{
            String message = "PRODUCE-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
            dataArray = message.getBytes();

            System.out.println("Produzindo...");

            DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
            clientSocket.send(sendPacket);
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
    
    // Metodo que performa uma eleicao
    public static void callElection(){
        try{
            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                if(Bully.neighbours.get(i).idNumber > Bully.myStats.idNumber){ //envia msg de eleição para todos os processos com IDs maiores que o dele
                    
                    String message = "ELECTION-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    dataArray = message.getBytes();
                    
                    Stats candidate = Bully.neighbours.get(i);
                    DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(candidate.ipAddress), candidate.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(dataArray, dataArray.length);
                    serverSocket.receive(receivePacket);
                    
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    String array[] = sentence.split("-");

                    int idNumber = Integer.parseInt(array[1]);
                    if(idNumber > Bully.myStats.idNumber){ // Se algum processo com ID maior responde, ele desiste
                        return;
                    }
                }
            }
            //Se ninguém responde, P vence eleição e torna-se coordenador
            //enviar mensagens a todos avisando sobre novo coord
            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                    
                String message = "NEWCOORD-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                dataArray = message.getBytes();
                
                Stats neighbour = Bully.neighbours.get(i);
                DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(neighbour.ipAddress), neighbour.portNumber);
                clientSocket.send(sendPacket);
                    
            }
            GroupCoord.execute();
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
}