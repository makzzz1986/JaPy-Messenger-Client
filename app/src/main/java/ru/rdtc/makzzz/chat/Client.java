package ru.rdtc.makzzz.chat;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by makzzz on 21.07.2016.
 */
public class Client {
    private static final String TAG = "Client.class";
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    private boolean conn_chk = false;
    private Socket sock_obj = null;

    public Client(InetAddress address) {
        int[] ports_list = {25901, 25902, 25903, 25904, 25905, 25906, 25907, 25908, 25909, 25910};

        for (int i = 0; i < 10; i++) {
            Log.i(TAG, "trying port - " + ports_list[i]);
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(address, ports_list[i]), 1000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                conn_chk = true;
                sock_obj = socket;
                Log.i(TAG, "Connected by " + ports_list[i] + "!!!");
                break;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    // закрываем соединение
    public void close() {
        try {
            sock_obj.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public boolean conn_chk() {
        // проверка состояния
        return conn_chk;
    }

    public void send(String msg) {
        // добавляем перенос строки
        msg += "\n";
        try {
            // пробуем послать сообщение
            writer.write(msg, 0, msg.length());
            writer.flush();
        } catch (IOException e) {
            // если ошибка, пишем в лог, меняем флаг
            Log.e(TAG, e.getMessage());
            conn_chk = false;
        }
    }


    public String recv() throws IOException {
        // получаем строку от сервера
        String recv_line = reader.readLine();
        // если ничего не пришло
        if (recv_line == null || recv_line.equals("") || recv_line.equals("> ")) {
            // флаг соединения убираем, возвращаем сообщение о разрыве
            conn_chk = false;
            return "Connection lost!";
        }
        return recv_line;
    }


//    public boolean send(String msg) {
//            msg += "\n";
//            try {
//                writer.write(msg, 0, msg.length());
//                writer.flush();
//                return true;
//            } catch (IOException e) {
//                Log.e(TAG, e.getMessage());
//                return false;
//            }
//        }


}
