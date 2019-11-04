public class Entry implements java.io.Serializable{

    private static final long serialVersionUID = -6060618751433873130L;
    public String ip;
    public String fileName;
    public String hash;

    public Entry(String anIp, String oneFileName, String aHash){
        ip = anIp;
        fileName = oneFileName;
        hash = aHash;
    }
}