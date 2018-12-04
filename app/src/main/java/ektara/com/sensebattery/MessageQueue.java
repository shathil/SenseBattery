package ektara.com.sensebattery;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mohoque on 31/12/2016.
 */

public class MessageQueue {

    private static MessageQueue queueInstance;

    public static MessageQueue getQueueInstance(){
        if(queueInstance==null){

            synchronized (MessageQueue.class){
                if(queueInstance==null)
                    queueInstance = new MessageQueue();
            }
        }
        return queueInstance;
    }


    private Map<Class<?>, Object> maps = new HashMap<>();
    private Map<Integer,Object> listMap = new HashMap<>();

    public <T> void push(T data) {

        maps.put(data.getClass(),data);

    }

    public <T> T pop(Class classType) {

        T value = (T) maps.get(classType);
        maps.remove(classType);

        return value;
    }
    public <T> void push(int message, T data) {

        listMap.put(message,data);

    }

    public <T> T pop(int message) {

        T value = (T) listMap.get(message);
        listMap.remove(message);
        return value;
    }

}
