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

    // Metodo para inicializar os sockets globais
    private static void initSocket(int socketType){
        try{
            
            if(socketType == 1){
                serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
                serverSocket.setSoTimeout(2000); // 2 segundos acontece timeout
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
        try {
            Thread t = new Thread() {
                public void run() {
                    waitForElections(); // thread separada fica ouvindo por eleicoes
                }
            };
            t.start();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
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
                        P(2);
                        Produce();
                        V(2);
                        V(3);
                    }
                    else{

                        P(3);
                        P(2);
                        Consume();
                        V(2);
                        V(1);
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
                default:
                    break;
            }
        }
        catch(SocketTimeoutException e){
            // coord esta morto? Chamo eleicao
            callElection();
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

        try{
            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                try{
                    if(Bully.neighbours.get(i).idNumber > Bully.myStats.idNumber){ //envia msg de eleição para todos os processos com IDs maiores que o dele
                        
                        String message = "ELECTION-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                        sendData = message.getBytes();
                        
                        Stats candidate = Bully.neighbours.get(i);
                        
                        if(candidate.idNumber != myCoord.idNumber){
                            // membros ouvem na porta+1
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(candidate.ipAddress), candidate.portNumber+1);
                            clientSocket.send(sendPacket);
                        }
              
                        // agora preciso receber a mensagem de retorno
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        String array[] = sentence.split("-");

                        int idNumber = Integer.parseInt(array[1]);
                        if(idNumber > Bully.myStats.idNumber){ // Se algum processo com ID maior responde, ele desiste
                            System.out.println("Perdi a eleicao...");
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

            System.out.println("Ganhei a eleicao...");

            for(int i = 0; i < Bully.neighbours.size(); i++ ){
                    
                String message = "NEWCOORD-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                sendData = message.getBytes();
                
                Stats neighbour = Bully.neighbours.get(i);

                if(neighbour.idNumber != myCoord.idNumber){
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(neighbour.ipAddress), neighbour.portNumber+1);
                    clientSocket.send(sendPacket);
                }     
            }
            clientSocket.close();
            serverSocket.close();
            exit = true;
            GroupCoord.execute();
        }
        catch(Exception exception){
            System.out.println("Excecao: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void waitForElections(){

        try{
            byte[] electionData = new byte[1024];
            DatagramSocket electionSocket; // ouco na porta+1
            electionSocket = new DatagramSocket(Bully.myStats.portNumber+1,InetAddress.getByName(Bully.myStats.ipAddress));

            while(true){
                if(exit){

                    System.out.println("Parando de ouvir por eleicoes...");

                    electionSocket.close();
                    return;
                }
                DatagramPacket receivePacket = new DatagramPacket(electionData, electionData.length);
                electionSocket.receive(receivePacket);


                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                
                System.out.println("Mensagem recebida: "+sentence);

                String array[] = sentence.split("-");
                String request = array[0];
                int requesterId = Integer.parseInt(array[1]);
                String requesterIp = array[2];
                int requesterPort = Integer.parseInt(array[3]);

                if(request.equals("ELECTION")){ // se o pedido for de uma nova eleicao
                            
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
                if(request.equals("NEWCOORD")){ // novo coordenador na area, aponto coord para ele

                    System.out.println("Novo coordenador: "+requesterId);
                    
                    myCoord = new Stats(requesterId,requesterIp,requesterPort);
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            exception.printStackTrace();
        }
    }
}