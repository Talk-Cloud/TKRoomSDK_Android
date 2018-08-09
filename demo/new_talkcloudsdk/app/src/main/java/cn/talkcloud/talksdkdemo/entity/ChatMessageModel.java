package cn.talkcloud.talksdkdemo.entity;

/**
 * @author: Liys
 * create date: 2018/7/17
 * 描述：
 */
public class ChatMessageModel {

    private String id;
    private String name;
    private String chatmessage;
    private int type;

    public ChatMessageModel(String id, String name, String chatmessage, int type) {
        this.id = id;
        this.name = name;
        this.chatmessage = chatmessage;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatmessage() {
        return chatmessage;
    }

    public void setChatmessage(String chatmessage) {
        this.chatmessage = chatmessage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
