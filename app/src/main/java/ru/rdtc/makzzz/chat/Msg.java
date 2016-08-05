package ru.rdtc.makzzz.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by makzzz on 26.07.2016.
 */
public class Msg {
    String user_nick;
    int msg_id;
    String cl_time;
    boolean srv_tag;
    String msg_text;

    public Msg(boolean sr, String tm, int id, String nick, String ms) {
        srv_tag = sr;
        cl_time = tm;
        msg_id = id;
        user_nick = nick;
        msg_text = ms;
    }

    public String getJson(Object object){
        // преобразуем текст в JSON
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
