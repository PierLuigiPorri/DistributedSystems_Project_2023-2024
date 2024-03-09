package Messages;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class Message {
    protected int sourceId;

    public Message(int sourceId) {
        this.sourceId = sourceId;
    }

    public abstract MessageEnum getType();

    public String getSerializedString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // Handle serialization error
            return "Error occurred during serialization";
        }
    }

    public static Message deserialize(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, ContentMessage.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // Handle deserialization error
            return null;
        }
    }

    public abstract int getSourceId();

}