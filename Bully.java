import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * disponivel em: http://www.ic.unicamp.br/~islene/mc514/prod-cons/prod-cons.pdf
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class Bully {
  public static ArrayList<Stats> neighbours = new ArrayList<Stats>(); // lista de vizinhos
  public static Stats myStats; // meus dados de configuracao

  public static void main(String args[]) throws FileNotFoundException {

    // carregando e processando o arquivo de config
    File configFile = new File(args[0]);
    Scanner configReader = new Scanner(configFile);

    int myConfigLine = Integer.parseInt(args[1]);
    int aux = 1;
    int biggestId = 0;
    while(configReader.hasNextLine()){

      String helper = configReader.nextLine();
      String[] array = helper.split(" ");
      int id = Integer.parseInt(array[0]);
      String ip = array[1];
      int port = Integer.parseInt(array[2]);

      if(id > biggestId){
        biggestId = id;
      }
      if(aux == myConfigLine){
        myStats = new Stats(id,ip,port);
      }
      else{
        neighbours.add(new Stats(id,ip,port));
      }
      aux++;
    }
    
    if(myStats.idNumber == biggestId){ // na inicializacao do sistema o membro com id maior e o primeiro coord
      GroupCoord.execute();;
    }
    else{
      GroupMember.execute(biggestId);
    }
  }

  // Metodo que performa uma eleicao
  public static void callElection(){
    
    boolean kingOfTheHill = true;
    for(int i = 0; i < neighbours.size(); i++ ){
      /**
        • Processo P convoca uma eleição:
        • P envia msg de eleição para todos os processos com IDs maiores que o dele
        • Se ninguém responde, P vence eleição e torna-se coordenador
        • Se algum processo com ID maior responde, ele desiste
        • Quando processo recebe msg de eleição de membros com ID mais baixa
        • Envia OK para remetente para indicar que está vivo e convoca eleição
      */
    }

    if(kingOfTheHill){ // ganhou eleicao
      
    }
    else{ // perdeu eleicao
      
    }
  }
}