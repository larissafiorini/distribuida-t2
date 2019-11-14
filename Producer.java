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
            myCoord = Bully.neighbours.get(coordId); // meu primeiro coord é o cara com id maior
            long lastPing = System.currentTimeMillis();
            while(true){ // fico enviando pedidos de acesso e producao de tempos em tempos
                long now = System.currentTimeMillis();

                if(((now - lastPing)/1000) >= 10){ // a cada dez segundos eu tento entrar
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
                            return;
                        }
                        // se ficou na fila fica esperando ate receber mensagem que entrou
                        while(true){
                            receivePacket = new DatagramPacket(dataArray, dataArray.length);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            command = array[0];
                            value = array[1];
                            // se entrou apenas retorna
                            if(value.equals("HASACCESS")){
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
                            return;
                        }
                        // se ficou na fila fica esperando ate receber mensagem que entrou
                        while(true){
                            receivePacket = new DatagramPacket(dataArray, dataArray.length);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            command = array[0];
                            value = array[1];
                            // se entrou apenas retorna
                            if(value.equals("HASACCESS")){
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
        switch(numSemaforo){
            case 2: // VMUTEX
                break;
            case 3: // VCHEIO
                break;
            default:
                break;
        }
    }

    public static void Produce(){
        try{
            String message = "PRODUCE-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
            dataArray = message.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
            clientSocket.send(sendPacket);
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
}