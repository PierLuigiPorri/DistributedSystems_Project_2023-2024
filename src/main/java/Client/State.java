package Client;
/*
    * Enum for the state of the client
    * JOINING: Client is joining the group
    * VIEW_CHANGE: Client is in the process of view change
    * NORMAL: Client is in normal state
    * DISCONNECTED: Client is disconnected
    * LEAVING: Client is leaving the group
 */
public enum State {
    JOINING,
    VIEW_CHANGE,
    NORMAL,
    DISCONNECTED,
    LEAVING
}
