package Messages;
/*
    * MessageEnum is an enum that contains the types of messages that can be sent between processes.
 */
public enum MessageEnum {
    JOIN,  PING, CONTENT, VIEW_CHANGE, LEAVE, ACK, DROP, FLUSH
}