package ru.rdtc.makzzz.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {
    private String message = null;
    private String srv_message = null;
    private boolean need_to_send = false;
    private static final String TAG = "MainActivity";
    private boolean conn_exist = false;
    private String user_nick = null;
    private int msg_id = 0;
    private int scroll_amount = 0;
    private ImageView logo_image;
    private Button butt_nick_choose;
    private boolean status_online = true;
    private Client client = null;
    // имя файла настроек
    private static final String APP_PREFERENCES = "settings";
    // ник
    private static final String APP_PREFERENCES_USER_NICK = "user_nick";
    // экземпляр настроек
    private SharedPreferences mSettings;
    // номер возвращённого значения
    private static final int CHOOSE_NICK = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // инициализация важных элементов программно
        butt_nick_choose = (Button) findViewById(R.id.butt_change_nick);

        // сохранённые настройки
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        // если в настройках есть ник
        if (mSettings.contains(APP_PREFERENCES_USER_NICK)) {
            user_nick = mSettings.getString(APP_PREFERENCES_USER_NICK, "default_nick");
            butt_nick_choose.setText(user_nick);
            Log.i(TAG, "Get nick from saved settings - " + user_nick);
        } else {
            // если ника нет - открываем новое активити с выбором ника
            user_nick = "no_nick_chosen";
            Log.i(TAG, "No nick saved, get through LoginActivity");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, CHOOSE_NICK);
        }

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
                if (client == null || client.conn_chk()) {
                    // запуск потока снова
                    new LongOperation().execute("");
                }
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

    // get login from another activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CHOOSE_NICK) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "We get nick!");
                user_nick = data.getStringExtra(LoginActivity.NICK);
                // if nick was changed - we should send service message about it and change button text
                if (!user_nick.equals(butt_nick_choose.getText().toString())) {
                    butt_nick_choose.setText(user_nick);
                    srv_message = "NICK_CHANGE_TO_" + user_nick;
                    need_to_send = true;
                    Log.i(TAG, "Nickname was changed");
                }
                // put nick to settings
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_USER_NICK, user_nick);
                editor.apply();
            } else {
                // if user click BACK in LoginActivity - try to take nick from settings
                mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                // if we have nick in settings
                if (mSettings.contains(APP_PREFERENCES_USER_NICK)) {
                    user_nick = mSettings.getString(APP_PREFERENCES_USER_NICK, "default_nick");
                    butt_nick_choose.setText(user_nick);
                    Log.i(TAG, "Get nick from saved settings - " + user_nick);
                } else {
                    // if no nick in settings
                    user_nick = "default_nick";
                }
            }
        }
    }

    // нажатие на кнопку ника для его смены
    public void onChangeNick(View view) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent, CHOOSE_NICK);
    }

    // собственно, процесс обмена сообщениями, в другом потоке
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
                client = new Client(host);
                // проверяем состояние подключения
                if (client.conn_chk()) {
                    // выставляем в переменную результат подключения
                    conn_exist = true;

                    Log.i(TAG, "Start sending");
                    Msg msg_init = new Msg(true, df.format(Calendar.getInstance().getTime()), msg_id++, user_nick, "INIT_SOCK");
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
                        Msg msg_update = new Msg(true, df.format(Calendar.getInstance().getTime()), msg_id++, user_nick, "UPDATE_REQUEST");
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

                        // if need to send flag is True - we need to send something
                        if (need_to_send) {
                            // if we have user message
                            if (message != null) {
                                Msg newmsg = new Msg(false, df.format(Calendar.getInstance().getTime()), msg_id++, user_nick, message);
                                client.send(newmsg.getJson(newmsg));
                                // clear strings and uncheck need_to_send flag
                                need_to_send = false;
                                message = null;
                            }

                            // and if we have some service message i.e. nick change
                            if (srv_message != null) {
                                Msg newmsg = new Msg(true, df.format(Calendar.getInstance().getTime()), msg_id++, user_nick, srv_message);
                                client.send(newmsg.getJson(newmsg));
                                need_to_send = false;
                                srv_message = null;
                            }
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

        // после прохождения обмена сообщениями
        @Override
        protected void onPostExecute(String result) {
            logo_image = (ImageView) findViewById(R.id.logo_image);
            logo_image.setImageResource(R.mipmap.logor);
            // обнуляем экземпляр класса, чтобы можно было его заново создать и подключиться
            client = null;
            Log.i(TAG, "END OF THREAD!\nPostExecute - logo RED!");
        }

        @Override
        protected void onPreExecute() {
        }

        protected void onProgressUpdate(String... progress) {
            // периодически нам нужно проверять, что изменилось на сервере, мы вставляем в чат полученные данные из doInBackground

            String chat = txtV_chat.getText().toString() + "\n" + progress[0];
            txtV_chat.setText(chat);

            // отрисовываем статус подключения
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

    // нажатие на кнопку отправки сообщения
    public void onSend (View view) {
        EditText ed_send = (EditText) findViewById(R.id.ed_send);
        message = ed_send.getText().toString();
        ed_send.setText("");
        need_to_send = true;
        }
    }

