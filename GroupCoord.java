import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * disponivel em: http://www.ic.unicamp.br/~islene/mc514/prod-cons/prod-cons.pdf
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class GroupCoord{

    private static int semaforoConsumer = 1;   
    private static Stats semaforoConsumerOwner = null;
    private static int semaforoProducer = 1;
    private static Stats semaforoProducerOwner = null;
    private static ArrayList<Stats> filaMutex = new ArrayList<Stats>();
    private static int numProducers = 0; // numero atual de produtores
    private static int numConsumers = 0; // numero atual de consumidores
    private static String currentBuffer = ""; // regiao critica
    private static int vazio = 10;
    private static ArrayList<Stats> filaProducers = new ArrayList<Stats>();
    private static int cheio = 0;
    private static ArrayList<Stats> filaConsumers = new ArrayList<Stats>();
    private static byte[] dataArray = new byte[1024];
    private static DatagramSocket serverSocket;
    private static DatagramSocket clientSocket;

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
            System.out.println("Excecao no coord: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }

    // Metodo que performa a logica de um membro coordenador
    public static void execute(){

        try{

            initSocket(1);
            initSocket(2);

            while(true){

                DatagramPacket receivePacket = new DatagramPacket(dataArray, dataArray.length);
                serverSocket.receive(receivePacket);
                
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                String array[] = sentence.split("-");
                String request = array[0];
                int requesterId = Integer.parseInt(array[1]);
                String requesterIp = array[2];
                int requesterPort = Integer.parseInt(array[3]);
                Stats newRequester = new Stats(requesterId,requesterIp,requesterPort);

                /**
                *   Produtor:                       Consumidor:
                    while (true)                    while (true)
                        item = produz();
                        decrementa(vazio);              decrementa(cheio);
                        decrementa(semaforoProducer);   decrementa(semaforoConsumer);
                        f = (f + 1) % N;                i = (i + 1) % N;
                        buffer[f] = item;               item = buffer[i];
                        incrementa(semaforoProducer);   incrementa(semaforoConsumer);
                        incrementa(cheio);              incrementa(vazio);
                                                        consome(item);
                */

                if(request.equals("PVAZIO")){
                    P(1,newRequester);
                }

                else if(request.equals("VVAZIO")){
                    V(1,newRequester);
                }

                if(request.equals("PCHEIO")){
                    P(2,newRequester);
                }

                else if(request.equals("VCHEIO")){
                    V(2,newRequester);
                }

                if(request.equals("PPRODUCER")){
                    P(3,newRequester);
                }

                else if(request.equals("VPRODUCER")){
                    V(3,newRequester);
                }

                if(request.equals("PCONSUMER")){
                    P(4,newRequester);
                }

                else if(request.equals("VCONSUMER")){
                    V(4,newRequester);
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
                    dataArray = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requesterIp), requesterPort);
                    clientSocket.send(sendPacket);
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

    // Sequestra um recurso do semaforo
    private static void P(int numSemaforo, Stats requester){

        /**
        *   Produtor:                       Consumidor:
            while (true)                    while (true)
                item = produz();
                decrementa(vazio);              decrementa(cheio);
                decrementa(semaforoProducer);   decrementa(semaforoConsumer);
                f = (f + 1) % N;                i = (i + 1) % N;
                buffer[f] = item;               item = buffer[i];
                incrementa(semaforoProducer);   incrementa(semaforoConsumer);
                incrementa(cheio);              incrementa(vazio);
                                                consome(item);
        */

        try{ // TODO CODIGO ESTA QUEBRADO DAQUI EM DIANTE!!!!!!!!!!
            switch(numSemaforo){
                case 1: // ENTER

                    if(mutex == true){ // e o arquivo esta trancado
                        // enviar mensagem avisando que ele nao ganhou acesso
                        String message = "STATUS-ONQUEUE";
                        dataArray = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    else{ // e o arquivo nao esta trancado
                        mutex = true; // ganha o acesso a area critica
                        mutexOwner = requester;
                        // enviar mensagem avisando que ele ganhou acesso
                        String message = "STATUS-HASACCESS";
                        dataArray = message.getBytes();
                        
                        DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    break;
                case 2: // PRODUCE

                    if(currentBuffer.length() < 10){ // tamanho maximo de produtos sobrando e 10
                        // escreve mais um X no arquivo
                        FileWriter fileWriter = new FileWriter(criticalRegionFile);
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        currentBuffer += "X";
                        printWriter.print(currentBuffer);
                        printWriter.close();
                        // envio mensagem avisando que foi produzido um novo X
                        String message = "STATUS-PRODUCED";
                        dataArray = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    else{ // ja existem 10 produtos sobrando
                        // envio mensagem avisando que nao foi produzido um novo X
                        String message = "STATUS-FULL";
                        dataArray = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    break;
                case 3: // CONSUME

                    if(currentBuffer.length() > 0){ // tamanho minimo de produtos e 0
                        // consome um X do arquivo
                        FileWriter fileWriter = new FileWriter(criticalRegionFile);
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        currentBuffer = currentBuffer.substring(1);
                        printWriter.print(currentBuffer);
                        printWriter.close();
                        // envio mensagem avisando que foi consumido um novo X
                        String message = "STATUS-CONSUMED";
                        dataArray = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    else{ // nao existem produtos sobrando
                        // envio mensagem avisando que nao foi consumido um novo X
                        String message = "STATUS-EMPTY";
                        dataArray = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                        clientSocket.send(sendPacket);
                    }
                    break;
                default:
                    break;
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }

    // Libera um recurso do semaforo
    private static void V(int numSemaforo, Stats requester){
        /**
        *   Produtor:                       Consumidor:
            while (true)                    while (true)
                item = produz();
                decrementa(vazio);              decrementa(cheio);
                decrementa(semaforoProducer);   decrementa(semaforoConsumer);
                f = (f + 1) % N;                i = (i + 1) % N;
                buffer[f] = item;               item = buffer[i];
                incrementa(semaforoProducer);   incrementa(semaforoConsumer);
                incrementa(cheio);              incrementa(vazio);
                                                consome(item);
        */

        try{ // TODO CODIGO ESTA QUEBRADO DAQUI EM DIANTE!!!!!!!!!!
            switch(numSemaforo){
                case 1:
                    if(filaMutex.size() > 0){ // fila nao vazia
                        mutexOwner = filaMutex.get(0);
                        // envio mensagem avisando o novo dono do mutex
                        String message = "STATUS-HASACCESS";
                        dataArray = message.getBytes();
                            
                        DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(mutexOwner.ipAddress), mutexOwner.portNumber);
                        clientSocket.send(sendPacket);
                        filaMutex.remove(0);
                    }
                    else{ // fila vazia
                        mutex = false; // libero o arquivo
                        mutexOwner = null; // removo o dono do acesso ao arquivo
                    }
                    // envio mensagem avisando que liberei a tranca conforme ele pediu
                    String message = "STATUS-LOSTACCESS";
                    dataArray = message.getBytes();
                        
                    DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, InetAddress.getByName(requester.ipAddress), requester.portNumber);
                    clientSocket.send(sendPacket);
                    break;
                case 2:

                    break;
                case 3:

                    break;
                default:
                    break;
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no coord: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
}