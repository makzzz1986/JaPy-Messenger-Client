package ru.rdtc.makzzz.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity_backup extends AppCompatActivity {
    private String message = " ";
    private boolean need_to_send = false;
    private static final String TAG = "MainActivity";
    private boolean conn_exist = false;
    private int msg_id = 0;
    private int scroll_amount = 0;
    private ImageView logo_image;
    private boolean status_online = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // запуск другого потока
        new LongOperation().execute("");

        // установка скролла
        TextView txtV_chat = (TextView) findViewById(R.id.txtV_chat);
        txtV_chat.setMovementMethod(new ScrollingMovementMethod());
    }

    // инициализация меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // обработка нажатий на меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView txtV_status = (TextView) findViewById(R.id.menu_choosen_status);
        switch (item.getItemId()){
            case R.id.stat_online:
                status_online = true;
                txtV_status.setText(R.string.status_online);
                Log.i(TAG, "Online status set!");
                return true;

            case R.id.stat_offline:
                conn_exist = false;
                txtV_status.setText(R.string.status_offline);
                status_online = false;
                Log.i(TAG, "Offline status set!");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LongOperation extends AsyncTask<String, String, String> {
        TextView txtV_chat = (TextView) findViewById(R.id.txtV_chat);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        @Override
        protected String doInBackground(String... params) {
            InetAddress host = null;
            // проверяем IP встроенной библиотекой
            try {
                host = InetAddress.getByName("62.231.161.237");
            } catch (UnknownHostException e) {
                Log.e(TAG, e.getMessage());
            }

            try {
                // создаём сокет через класс Client
                Client client = new Client(host);
                // проверяем состояние подключения
                if (client.conn_chk()) {
                    // выставляем в переменную результат подключения
                    conn_exist = true;

                    Log.i(TAG, "Start sending");
                    Msg msg_init = new Msg(true, df.format(Calendar.getInstance().getTime()), msg_id++, "default nick", "INIT_SOCK");
                    // отсылаем сообщение
                    client.send(msg_init.getJson(msg_init));

                    while (true) {
                        // проверка что стоит в режиме работы - онлайн-офлайн
                        if (!status_online) {
                            conn_exist = false;
                            client.close();
                            Log.i(TAG, "Closing connection");
                            break;
                        }

                        // проверка работы сервера. Если false - выходим
                        if (!client.conn_chk()) {
                            conn_exist = false;
                            Log.i(TAG, "Connection lost!");
                            break;
                        }
                        conn_exist = true;
                        // отправляем запрос за обновлениями на сервер
                        Msg msg_update = new Msg(true, df.format(Calendar.getInstance().getTime()), msg_id++, "default nick", "UPDATE_REQUEST");
                        client.send(msg_update.getJson(msg_update));
                        // ждём ответ от сервера и кладём результат в переменную
                        String response = client.recv();
                        if (response.equals("Connection lost!")) {
                            conn_exist = false;
                            Log.i(TAG, "Connection lost!");
                            break;
                        }
                        // передаём в процесс onProgressUpdate, он опубликует результат в текстовом поле
                        publishProgress(response);
                        // если стоит флаг что надо что-то передать

                        if (need_to_send) {
                            Msg newmsg = new Msg(false, df.format(Calendar.getInstance().getTime()), msg_id++, "default nick", message);
                            // отправляем сообщение
                            client.send(newmsg.getJson(newmsg));
                            // убираем флаг отправки и чистим переменную с сообщением
                            need_to_send = false;
                            message = "";
                        }

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            logo_image = (ImageView) findViewById(R.id.logo_image);
            logo_image.setImageResource(R.mipmap.logor);
            Log.i(TAG, "END OF THREAD!\nPostExecute - logo RED!");
        }

        @Override
        protected void onPreExecute() {
        }

        protected void onProgressUpdate(String... progress) {
            // периодически нам нужно проверять, что изменилось на сервере, мы вставляем в чат полученные данные из doInBackground
//            if (progress[0].equals("> ")) {
//                Log.i(TAG, "> !");
//            }
            String chat = txtV_chat.getText().toString() + "\n" + progress[0];
            txtV_chat.setText(chat);
            // ImageView определяем
            logo_image = (ImageView) findViewById(R.id.logo_image);

            // если подключено есть:
            if (conn_exist) {
                // ставим лого зелёным
                logo_image.setImageResource(R.mipmap.logog);
                Log.i(TAG, "Logo GREEN!");
            } else {
                // ставим лого красным
                logo_image.setImageResource(R.mipmap.logor);
                Log.i(TAG, "Logo RED!");
            }


            // перетаскиваем скролл вниз, если сообщений больше, нежели помещается на экране
            if (txtV_chat.getLineCount() > Math.round(txtV_chat.getHeight()/txtV_chat.getLineHeight())) {
                scroll_amount = scroll_amount + txtV_chat.getLineHeight();
                txtV_chat.scrollTo(0, scroll_amount);
            }
        }
    }


    public void onSend (View view) {
        EditText ed_send = (EditText) findViewById(R.id.ed_send);
        message = ed_send.getText().toString();
        ed_send.setText("");
        need_to_send = true;
        }
    }

