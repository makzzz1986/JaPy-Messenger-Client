package ru.rdtc.makzzz.chat;

import android.util.Log;

import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by makzzz on 23.08.2016.
 */
public class RecvMsgs {
    private String TAG = "RecvMsgs.java class";
    public String message = null;

    public RecvMsgs(String input_msg) {
        message = input_msg;
    }

    public String getString() {
        // make strings from JSON
        String resulted = "";
        Log.i(TAG, "message input:" + message);
        try {
            // dismember JSON to array
            JSONArray array = new JSONArray(message);
            // taking every dictionary: one dic - one message
            for (int i = 0; i < array.length(); i++) {
                JSONObject dic = array.getJSONObject(i);
                if (!dic.getBoolean("srv_tag")) {
                    resulted += dic.getString("user_nick") + ": ";
                    resulted += dic.getString("msg_text");
                    resulted += "\n";
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "can't use JSON, error " + e);
        }
//        Log.i(TAG, "add to chat - " + resulted);
        return resulted;
    }
}
