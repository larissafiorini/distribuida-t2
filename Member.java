import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class Member{

    private static byte[] receiveData = new byte[1024];
    private static byte[] sendData = new byte[1024];
    private static DatagramSocket serverSocket;
    private static DatagramSocket clientSocket;
    private static Stats myCoord;
    private static boolean exit = false;
    private static boolean reset = false;
    private static boolean electionOngoing = false;

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
            System.out.println("Excecao: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    // Metodo que performa a logica de um produtor
    public static void execute(int coordId, int role){
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

            while(true){ // fico enviando pedidos de acesso e producao de tempos em tempos
                long now = System.currentTimeMillis();

                if(((now - lastPing)/1000) >= 1){ // a cada segundo eu tento entrar

                    lastPing = System.currentTimeMillis();
                    if(role == 1){

                        P(1);
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        P(2);
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        Produce();
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        V(2);
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        V(3);
                        if(exit){
                            return;
                        }
                    }
                    else{

                        P(3);
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        P(2);
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        Consume();
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        V(2);
                        if(exit){
                            return;
                        }
                        if(reset){
                            reset = false;
                            continue;
                        }
                        V(1);
                        if(exit){
                            return;
                        }
                    }
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    private static void P(int numSemaforo){
        try{
            switch(numSemaforo){
                case 1: // PVAZIO

                    System.out.println("P(vazio)...");

                    String message = "PVAZIO-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();
                    
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);

                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    String array[] = sentence.split("-");
                    String request = array[0];

                    if(request.equals("ELECTION")){ // se o pedido for de uma nova eleicao
                        electionOngoing = true;
                        int requesterId = Integer.parseInt(array[1]);
                        String requesterIp = array[2];
                        int requesterPort = Integer.parseInt(array[3]);
                        boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                        if(result){
                            return;
                        }
                    }                 

                    else if(request.equals("STATUS")){

                        String value = array[1];

                        // se entrou apenas retorna
                        if(value.equals("HASACCESS")){

                            System.out.println("Entrei no semaforo...");

                            return;
                        }
                        // se ficou na fila fica esperando ate receber mensagem que entrou
                        while(true){

                            System.out.println("Estou na fila...");

                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.setSoTimeout(0);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            request = array[0];
                            value = array[1];
                            // se entrou apenas retorna
                            if(value.equals("HASACCESS")){

                                System.out.println("Entrei no semaforo...");
                                serverSocket.setSoTimeout(10000);
                                return;
                            }
                            if(request.equals("ELECTION")){
                                electionOngoing = true;
                                int requesterId = Integer.parseInt(array[1]);
                                String requesterIp = array[2];
                                int requesterPort = Integer.parseInt(array[3]);
                                boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                                if(result){
                                    return;
                                }
                            }
                        }
                    }
                    else{ // se eu receber uma mensagem que nao reconheço 
                        throw new Exception("Mensagem Invalida Recebida: "+request); // lanco uma nova excessao
                    }
                case 2: // PMUTEX
                    message = "PMUTEX-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    System.out.println("P(mutex)...");

                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    
                    sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    array = sentence.split("-");
                    request = array[0];

                    if(request.equals("ELECTION")){
                        electionOngoing = true;
                        int requesterId = Integer.parseInt(array[1]);
                        String requesterIp = array[2];
                        int requesterPort = Integer.parseInt(array[3]);
                        boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                        if(result){
                            return;
                        }
                    }           

                    else if(request.equals("STATUS")){

                        String value = array[1];

                        // se entrou apenas retorna
                        if(value.equals("HASACCESS")){

                            System.out.println("Entrei no semaforo...");

                            return;
                        }
                        // se ficou na fila fica esperando ate receber mensagem que entrou
                        while(true){

                            System.out.println("Estou na fila...");

                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.setSoTimeout(0);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                            array = sentence.split("-");
                            request = array[0];
                            value = array[1];
                            // se entrou apenas retorna
                            if(value.equals("HASACCESS")){
                                serverSocket.setSoTimeout(10000);
                                System.out.println("Entrei no semaforo...");

                                return;
                            }
                            if(request.equals("ELECTION")){
                                electionOngoing = true;
                                int requesterId = Integer.parseInt(array[1]);
                                String requesterIp = array[2];
                                int requesterPort = Integer.parseInt(array[3]);
                                boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                                if(result){
                                    return;
                                }
                            }
                        }
                    }
                    else{ // se eu receber uma mensagem que nao reconheço 
                        throw new Exception("Mensagem Invalida Recebida: "+request); // lanco uma nova excessao
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
                    request = array[0];

                    if(request.equals("ELECTION")){
                        electionOngoing = true;
                        int requesterId = Integer.parseInt(array[1]);
                        String requesterIp = array[2];
                        int requesterPort = Integer.parseInt(array[3]);
                        boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                        if(result){
                            return;
                        }
                    }         

                    else if(request.equals("STATUS")){

                        String value = array[1];

                        // se entrou apenas retorna
                        if(value.equals("HASACCESS")){

                            System.out.println("Entrei no semaforo...");

                            return;
                        }
                        // se ficou na fila fica esperando ate receber mensagem que entrou
                        while(true){

                            System.out.println("Estou na fila...");

                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.setSoTimeout(0);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            request = array[0];
                            value = array[1];
                            // se entrou apenas retorna
                            if(value.equals("HASACCESS")){
                                serverSocket.setSoTimeout(10000);
                                System.out.println("Entrei no semaforo...");

                                return;
                            }
                            if(request.equals("ELECTION")){
                                electionOngoing = true;
                                int requesterId = Integer.parseInt(array[1]);
                                String requesterIp = array[2];
                                int requesterPort = Integer.parseInt(array[3]);
                                boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                                if(result){
                                    return;
                                }
                            }
                        }
                    }
                    else{ // se eu receber uma mensagem que nao reconheço 
                        throw new Exception("Mensagem Invalida Recebida: "+request); // lanco uma nova excessao
                    }
                default:
                    break;
            }
        }
        catch(SocketTimeoutException e){
            if(!electionOngoing){
                // coord esta morto? Chamo eleicao
                callElection();
            }
        }
        catch(Exception exception){
            System.out.println("Excecao: "+exception.getMessage());
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
                    
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    String array[] = sentence.split("-");
                    String request = array[0];

                    if(request.equals("ELECTION")){
                        electionOngoing = true;
                        int requesterId = Integer.parseInt(array[1]);
                        String requesterIp = array[2];
                        int requesterPort = Integer.parseInt(array[3]);
                        boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                        if(result){
                            return;
                        }
                    }
                    return;
                case 2: // VMUTEX
                    message = "VMUTEX-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    System.out.println("V(mutex)...");

                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    array = sentence.split("-");
                    request = array[0];

                    if(request.equals("ELECTION")){
                        electionOngoing = true;
                        int requesterId = Integer.parseInt(array[1]);
                        String requesterIp = array[2];
                        int requesterPort = Integer.parseInt(array[3]);
                        boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                        if(result){
                            return;
                        }
                    }
                    return;
                case 3: // VCHEIO
                    message = "VCHEIO-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    System.out.println("V(cheio)...");

                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    
                    sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    array = sentence.split("-");
                    request = array[0];

                    if(request.equals("ELECTION")){
                        electionOngoing = true;
                        int requesterId = Integer.parseInt(array[1]);
                        String requesterIp = array[2];
                        int requesterPort = Integer.parseInt(array[3]);
                        boolean result = handleElectionMessage(requesterId,requesterIp,requesterPort);
                        if(result){
                            return;
                        }
                    }
                    return;
                default:
                    break;
            }
        }
        catch(SocketTimeoutException e){
            if(!electionOngoing){
                // coord esta morto? Chamo eleicao
                callElection();
            }
        }
        catch(Exception exception){
            System.out.println("Excecao: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void Produce(){
        try{
            String message = "PRODUCE-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
            sendData = message.getBytes();

            System.out.println("Produzindo...");

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
            clientSocket.send(sendPacket);
        }
        catch(Exception exception){
            System.out.println("Excecao: "+exception.getMessage());
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
            System.out.println("Excecao: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    // Metodo que performa uma eleicao
    public static void callElection(){

        System.out.println("Chamando eleicao...");
        Stats winner = Bully.myStats;
        try{
            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                try{
                        
                    String message = "ELECTION-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();
                    
                    Stats candidate = Bully.neighbours.get(i);
                    
                    if(candidate.idNumber != myCoord.idNumber){
                        // membros ouvem na porta+1
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(candidate.ipAddress), candidate.portNumber);
                        clientSocket.send(sendPacket);
                    }
            
                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    String[] array = sentence.split("-");

                    int requesterId = Integer.parseInt(array[1]);
                    String requesterIp = array[2];
                    int requesterPort = Integer.parseInt(array[3]);

                    if(requesterId > winner.idNumber){ // Se algum processo com ID maior responde, ele desiste
                        
                        winner = new Stats(requesterId,requesterIp,requesterPort);
                    }
                }
                catch(SocketTimeoutException e){
                    continue;
                }
            }

            //enviar mensagens a todos avisando sobre novo coord

            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                    
                String message = "NEWCOORD-"+winner.idNumber+"-"+winner.ipAddress+"-"+winner.portNumber;
                sendData = message.getBytes();
                
                Stats neighbour = Bully.neighbours.get(i);

                if(neighbour.idNumber != myCoord.idNumber){
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(neighbour.ipAddress), neighbour.portNumber);
                    clientSocket.send(sendPacket);
                }
            }

            if(winner.idNumber == Bully.myStats.idNumber){ //sou o novo coord
                System.out.println("Sou o novo coord");
                clientSocket.close();
                serverSocket.close();
                GroupCoord.execute();
                exit = true;
                return;
            }
            else{
                System.out.println("Novo coordenador: "+winner.idNumber);
                                
                myCoord = new Stats(winner.idNumber,winner.ipAddress,winner.portNumber);
                reset = true;
                electionOngoing = false;
                Thread.sleep(5000);
                return;
            }
        }
        catch(Exception exception){
            System.out.println("Excecao: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static boolean handleElectionMessage(int id, String ip, int port){
        try{
            System.out.println("Mensagem de eleicao recebida...");

            // comparo nossos ids e envio uma resposta
            String message = "STATUS-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
            sendData = message.getBytes();
                
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ip), port);
            clientSocket.send(sendPacket);

            System.out.println("Aguardando fim da eleicao...");
            while(true){

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.setSoTimeout(0);
                serverSocket.receive(receivePacket);
                
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                System.out.println("Sentence: "+sentence);/////////////////////////

                String[] array = sentence.split("-");
                String request = array[0];
                int requesterId = Integer.parseInt(array[1]);
                String requesterIp = array[2];
                int requesterPort = Integer.parseInt(array[3]);
                
                if(request.equals("NEWCOORD")){ // novo coordenador na area, aponto coord para ele
                    serverSocket.setSoTimeout(10000);
                    if(requesterId == Bully.myStats.idNumber){ // sou novo coord
                        System.out.println("Sou o novo coord");
                        clientSocket.close();
                        serverSocket.close();
                        GroupCoord.execute();
                        exit = true;
                        electionOngoing = false;
                        return true;
                    }
                    System.out.println("Novo coordenador: "+requesterId);
                    
                    myCoord = new Stats(requesterId,requesterIp,requesterPort);
                    reset = true;
                    electionOngoing = false;
                    Thread.sleep(5000);
                    return true;
                }
            }
        }
        catch(Exception exception){
            return false;
        }
    }
}