import java.net.*;
import java.io.*;
import java.util.Random;

/*
* Jogo de corrida usando arquitetura cliente-servidor.
* 
* @author Guilherme Piccoli, Fernando Maioli, Igor Brehm
*/

public class Server extends Thread {
    private ServerSocket serverSocket;
    private static int[] scores;
    private static int[] board;
    private int board_size;

    public Server(int port,int board_size) throws IOException {
        serverSocket = new ServerSocket(port);
        this.board_size = board_size;
    }

    // Metodo que controla as rodadas de uma partida do jogo
    public void rodadas(String[] players,DataInputStream in, DataInputStream in2, DataOutputStream out, DataOutputStream out2){
        while(true){
            // ambos jogadores fazem suas jogadas, se o jogo terminou por algum motivo, finaliza as rodadas
            if(!realizaAcao(0,players, out, out2, in)){
                return;
            }
            if(!realizaAcao(1,players, out2, out, in2)){
                return;
            }

        }
    }

    // Realiza a acao do jogador, 
    // caso o jogo terminou porque alguem terminou a corrida ou desistiu retorna false
    // caso o jogador simplesmente executou sua rodada, retorna true
    public Boolean realizaAcao(int playerNumber,String[] players,DataOutputStream outJogando, DataOutputStream outEsperando, DataInputStream inJogando){
        try {
            int adversaryNumber;
            if(playerNumber == 0) adversaryNumber = 1;
            else adversaryNumber = 0;
            String clientSentence;
            int roll = 0;
            outJogando.writeUTF("Sua vez"); // jogador que esta em sua rodada
            outEsperando.writeUTF("Aguardando adversario"); // jogador que esta esperando pelo adversario
            clientSentence = inJogando.readUTF();
            String[] data = clientSentence.split("-", 2); // Pega a mensagem enviada pelo jogador
            if(data[0].equals("Roll")){
                // Se o jogador rodou o dado deve apresentar o resultado para ambos jogadores e verificar se ele terminou a corrida
                roll = Integer.parseInt(data[1]);
                System.out.println("Jogador "+players[playerNumber]+" tirou "+roll+" nos dados!");
                scores[playerNumber] = scores[playerNumber] + roll;
                outEsperando.writeUTF("\nAdversario avancou " + roll + " casas!");
                // Metodo que verifica o evento decorrente da casa onde o jogador parou
                switchCasos(playerNumber+1,board[scores[playerNumber]], outJogando, outEsperando); 

                if(scores[playerNumber] >= board_size){
                    System.out.println("Jogador "+players[playerNumber]+" venceu a corrida!");
                    outJogando.writeUTF("Fim de jogo");
                    outEsperando.writeUTF("Fim de jogo");
                    outJogando.writeUTF("Jogador "+players[playerNumber]+" venceu a corrida!");
                    outEsperando.writeUTF("Jogador "+players[playerNumber]+" venceu a corrida!");
                    return false;
                }
                
                outJogando.writeUTF("-------------------------------");
                outJogando.writeUTF("Voce esta na casa: " + scores[playerNumber] );
                outJogando.writeUTF("-------------------------------");
                outJogando.writeUTF("Seu adversario esta na casa: " + scores[adversaryNumber] );
                outJogando.writeUTF("-------------------------------");

                outEsperando.writeUTF("-------------------------------");
                outEsperando.writeUTF("Voce esta na casa: " + scores[adversaryNumber] );
                outEsperando.writeUTF("-------------------------------");
                outEsperando.writeUTF("Seu adversario esta na casa: " + scores[playerNumber] );
                outEsperando.writeUTF("-------------------------------");

            }
            else if(data[0].equals("Exit")){ // Verifica se o jogador desistiu do jogo
                outEsperando.writeUTF("Desistencia"); // Avisa o outro jogador da desistencia de seu adversario
                System.out.println("Jogador "+data[1]+" desistiu da partida.");
                System.out.println("Esperando proximos jogadores");
                return false;
            }
            else{
                System.out.println(clientSentence);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    //Metodo que inicializa as coneccoes com os jogadores e organiza o inicio da partida
    public void run() {
        try {
            while(true){
                System.out.println("\nIniciando nova partida:");
                Random r = new Random();
                Socket server1 = serverSocket.accept();
                Socket server2 = serverSocket.accept();
                
                String clientSentence; // String usada para as mensagens enviadas pelos clientes
                String[] players = new String[2]; // lista de nomes dos jogadores
                players[0] = ""; // nome do jogador 1
                players[1] = ""; // nome do jogador 2
                scores = new int[]{0,0}; // indice no tabuleiro onde esta cada jogador
                board = new int[board_size];
                
                DataInputStream in = new DataInputStream(server1.getInputStream());
                DataOutputStream out = new DataOutputStream(server1.getOutputStream());
                DataInputStream in2 = new DataInputStream(server2.getInputStream());
                DataOutputStream out2 = new DataOutputStream(server2.getOutputStream());
                out.writeUTF("Adversario encontrado!");
                out2.writeUTF("Adversario encontrado!");
                
                //inicializando os valores das posicoes do tabuleiro
                //dependendo do valor na posicao do tabuleiro, o jogador sofre efeitos extras
                for(int i = 0; i < board.length; i++){
                    if(i % 3 == 0){
                        board[i] = 0; //posicao onde nada acontece
                    }
                    else{
                        board[i] = r.nextInt(6); //posicoes onde se voltam casas ou se andam casas extras
                    }
                }
                
                //adquirindo os nomes dos jogadores
                out2.writeUTF("Aguardando adversario");
                out.writeUTF("Aguardando nome");
                while(players[0].equals("")){
                    clientSentence = in.readUTF();
                    String[] data = clientSentence.split("-", 2);
                    if(data[0].equals("Name")){
                        if(data[1].equals("") || data[1].length() < 3){
                            out.writeUTF("Nome invalido");
                        }
                        else{
                            players[0] = data[1];
                        }
                    }
                    else{
                        out.writeUTF("Nome invalido");
                    }
                }
                out.writeUTF("Aguardando adversario");
                out2.writeUTF("Aguardando nome");
                while(players[1].equals("")){
                    clientSentence = in2.readUTF();
                    String[] data = clientSentence.split("-", 2);
                    if(data[0].equals("Name")){
                        if(data[1].equals("") || data[1].length() < 3 || data[1].equals(players[0])){
                            out2.writeUTF("Nome invalido");
                        }
                        else{
                            players[1] = data[1];
                        }
                    }
                    else{
                        out2.writeUTF("Nome invalido");
                    }
                }
                
                System.out.println("Jogadores: "+players[0]+" e "+players[1]+"\n");
                //executando as rodadas
                out.writeUTF("Iniciando jogo");
                out2.writeUTF("Iniciando jogo");
                rodadas(players, in, in2, out, out2);
                server1.close();
                server2.close();    
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Metodo que realiza a movimentacao de um jogador no tabuleiro
    public void switchCasos(int player,int board_score, DataOutputStream jogador1, DataOutputStream jogador2){
        Random r = new Random();
        try {
            switch(board_score) {
                case 0: 
                    break;
                case 1:
                    if(player == 1){
                        jogador1.writeUTF("Voce passou sobre oleo, e ira voltar duas casas");
                        if (scores[0] >= 2) {
                            scores[0] -= 2;
                        }
                        else scores[0] = 0;

                        jogador2.writeUTF("Voce deu sorte, seu adversario passou sobre oleo e retornou duas casas");
                        break; 
                    }
                    else{
                        jogador1.writeUTF("Voce passou sobre oleo, e ira voltar duas casas");
                        if (scores[1] >= 2) {
                            scores[1] -= 2;
                        }
                        else scores[1] = 0;

                        jogador2.writeUTF("Voce deu sorte, seu adversario passou sobre oleo e retornou duas casas");
                        break; 
                    }

                case 2:
                    if(player == 1){
                        jogador1.writeUTF("Voce derrapou na curva e ira voltar uma casa");
                        if (scores[0] >= 1) {
                            scores[0] -= 1;
                        }
                        else scores[0] = 0;
                        jogador2.writeUTF("Seu adversario errou a curva e retornou uma casa");
                        break;   
                    }
                    else{
                        jogador1.writeUTF("Voce derrapou na curva e ira voltar uma casa");
                        if (scores[1] >= 1) {
                            scores[1] -= 1;
                        }
                        else scores[1] = 0;
                        jogador2.writeUTF("Seu adversario errou a curva e retornou uma casa");
                        break;   
                    }

                case 3:
                    if(player == 1){
                        jogador1.writeUTF("Voce acabou de passar por um turbo, e ira andar mais duas casas");
                        scores[0] += 2;
                        jogador2.writeUTF("Seu adversario pegou um turbo e acelerou mais duas casas a frente");
                        break;  
                    }
                    else{
                        jogador1.writeUTF("Voce acabou de passar por um turbo, e ira andar mais duas casas");
                        scores[1] += 2;
                        jogador2.writeUTF("Seu adversario pegou um turbo e acelerou mais duas casas a frente");
                        break;  
                    }

                case 4:
                    if(player == 1){
                        jogador1.writeUTF("Voce acabou de passar por um buraco, e ira retornar mais duas casas");
                        if (scores[0] >= 2) {
                            scores[0] -= 2;
                        }
                        else scores[0] = 0;
                        jogador2.writeUTF("Seu adversario passou por um buraco e voltou duas casas");
                        break; 
                    }
                    else{
                        jogador1.writeUTF("Voce acabou de passar por um buraco, e ira retornar mais duas casas");
                        if (scores[1] >= 2) {
                            scores[1] -= 2;
                        }
                        else scores[1] = 0;
                        jogador2.writeUTF("Seu adversario passou por um buraco e voltou duas casas");
                        break; 
                    }

                case 5:
                    if(player == 1){
                        jogador1.writeUTF("Voce acabou de realizar uma curva perfeita, e ira andar mais uma casa");
                        scores[0] += 1;

                        jogador2.writeUTF("Seu adversario se deu bem na curva, e andou mais uma casa");
                        break;   
                    }
                    else{
                        jogador1.writeUTF("Voce acabou de realizar uma curva perfeita, e ira andar mais uma casa");
                        scores[1] += 1;

                        jogador2.writeUTF("Seu adversario se deu bem na curva, e andou mais uma casa");
                        break;
                    }
                    
                case 6:
                    int muitaSorte = r.nextInt(2);
                    if(player == 1){
                        if (muitaSorte == 1) {
                            jogador1.writeUTF("Uau, que habilidade!!! Voce encontrou um atalho e ira pular 4 casas");
                            scores[0] += 4;
                            jogador2.writeUTF("De repente seu adversario pulou 4 casas, que loucura!!!");
                            break;
                        }
                        else {
                            break;
                        }
                    }
                    else{
                        if (muitaSorte == 1) {
                            jogador1.writeUTF("Uau, que habilidade!!! Voce encontrou um atalho e ira pular 4 casas");
                            scores[1] += 4;
                            jogador2.writeUTF("De repente seu adversario pulou 4 casas, que loucura!!!");
                            break;
                        }
                        else {
                            break;
                        }
                    }

                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
         }
   }
    
    // Metodo main que inicia o programa servidor

    //Para executar o lado do servidor, deve-se passar como parametro a porta que sera utilizada e o tamanho do tabuleiro
    //ex: java Server 80 20 -> Onde a porta sera 80, e o tamanho do tabuleiro sera 20
    public static void main(String [] args) {
        int port = Integer.parseInt(args[0]);
        int board_size = Integer.parseInt(args[1]);
        try {
            Thread t = new Server(port,board_size);
            t.start();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
