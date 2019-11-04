import java.util.Map;
import java.util.HashMap;

public class Client{
    public String ip;
    public Map<String, String> fileDict;
    public long life;

    public Client(String  ip, long time){
        this.ip = ip;
        this.fileDict = new HashMap<String, String>();
        this.life = time;
    }
}