package ca.masseyhacks.minecraftdiscordlink.structures;

public class LinkConfirmData {
    public final String secret;
    public long timestamp;

    public LinkConfirmData(String secret, long timestamp){
        this.secret = secret;
        this.timestamp = timestamp;
    }
}
