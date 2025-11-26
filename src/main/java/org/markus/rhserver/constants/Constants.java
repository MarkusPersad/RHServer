package org.markus.rhserver.constants;

public class Constants {
    public static final String HEARTBEAT = "heartbeat:";
    public static final String FRIENDLIST_USER = "friendlist:user:";
    public static final String FRIENDLIST_GROUP = "friendlist:group:";
    public static final String APPLICATION_X_PROTOBUF = "application/x-protobuf";
    public static final String FRIEND_SAY_HELLO = "你好，现在可以开始聊天了！";
    public static String groupWelcome(String userName){
        return String.format("欢迎 %s 加入群聊！", userName);
    }
}
