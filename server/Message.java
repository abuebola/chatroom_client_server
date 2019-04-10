/*+----------------------------------------------------------------------
 || ELEC463 Lab 4 - Andre Al-Khoury - 26017029
 || Class: Message
 || Purpose:  Wrapper class for chat messages (sender, recipient, message)
 ++-----------------------------------------------------------------------*/


public class Message {

    public String sender;
    public String recipient = null;
    public String message;
    private static final char RECORD_SEPARATOR = 0x1e;

    public Message(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    public Message(Message message) {
        this.sender = message.sender;
        this.recipient = message.recipient;
        this.message = message.message;
    }

    public Message(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public Message(String message) {
        this.message = message;
    }

    public static Message decodeMessage(String receivedString) {
        String[] strings = receivedString.split(Character.toString(RECORD_SEPARATOR));
        if (strings[0].equals("1")) {
            return new Message(strings[1], strings[2]);
        } else {
            return new Message(strings[1], strings[2], strings[3]);
        }
    }
}
