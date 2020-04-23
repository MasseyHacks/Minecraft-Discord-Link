package ca.masseyhacks.minecraftdiscordlink.structures;

public class ParticipantInfo {
    private String discordTag;
    private String discordID;
    private double balance;

    private static final String DEFAULT_TAG = "No Discord Link";

    public ParticipantInfo(){
        this(DEFAULT_TAG, "", -1);
    }

    public ParticipantInfo(String discordTag, String discordID, double balance){
        this.discordTag = discordTag;
        this.discordID = discordID;
        this.balance = balance;
    }

    public ParticipantInfo(ParticipantInfo a, ParticipantInfo b){
        // merges two Participant info objects. takes the non-default value of each one
        this.discordTag = !a.discordTag.equals(DEFAULT_TAG) ? a.discordTag : b.discordTag;
        this.discordID = !a.discordID.equals("") ? a.discordID : b.discordID;
        this.balance = Math.max(a.balance, b.balance);
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public void setDiscordTag(String discordTag) {
        this.discordTag = discordTag;
    }

    public String getDiscordID() {
        return discordID;
    }

    public void setDiscordID(String discordID) {
        this.discordID = discordID;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
