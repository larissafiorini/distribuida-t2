public class RMIProgram {  

   private RMIProgram() {}  

   public static void main(String[] args) {  

      try{
         if(args[0].equals("server")){
            ServerRuntime server = new ServerRuntime(args[1]);
         }
         else if(args[0].equals("client")){

            ClientRuntime client = new ClientRuntime(args[1],args[2]);
         }
         else{

            throw new Exception();
         }
      }
      catch(Exception exception){

         System.out.println("Insira Argumentos: java RMIProgram <server ip/client ip serverIp>");
      }
   } 
}