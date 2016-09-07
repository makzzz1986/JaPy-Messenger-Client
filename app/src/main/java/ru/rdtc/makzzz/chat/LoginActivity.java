package ru.rdtc.makzzz.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {
    public static final String NICK = "ru.rdtc.makzzz.chat.NICK";
    private static final String TAG = "LoginActivity";

    // settings filename
    private static final String APP_PREFERENCES = "settings";
    // user nick
    private static final String APP_PREFERENCES_USER_NICK = "user_nick";
    // host ip
    private static final String APP_PREFERENCES_SERVER_IP = "server_ip";
    // экземпляр настроек
    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // сохранённые настройки
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        // getting IP and check it's validity
        EditText nickEdTxt = (EditText) findViewById(R.id.login_edTxt);
        EditText ipEdTxt = (EditText) findViewById(R.id.login_edIpTxt);
        if (mSettings.contains(APP_PREFERENCES_USER_NICK)) {
            String user_nick = mSettings.getString(APP_PREFERENCES_USER_NICK, "");
            nickEdTxt.setText(user_nick);
        }
        try {
            InetAddress host = InetAddress.getByName(mSettings.getString(APP_PREFERENCES_SERVER_IP, "62.231.161.237"));
            ipEdTxt.setText(host.toString().substring(1));
        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void onLoginSet(View view) {
        Intent intent = new Intent();
        EditText edTxt_login = (EditText) findViewById(R.id.login_edTxt);
        EditText edTxt_ip = (EditText) findViewById(R.id.login_edIpTxt);

        // проверяем, если пользователь что-нибудь ввёл, то передаём результат в стартовую активити
        if (edTxt_login.getText().length() != 0 && edTxt_ip.getText().length() != 0) {
            Log.i(TAG, "Get nick = " + edTxt_login.getText());
            Log.i(TAG, "Get ip = " + edTxt_ip.getText());
            String[] resulted_data = {edTxt_login.getText().toString(), edTxt_ip.getText().toString()};
            intent.putExtra(NICK, resulted_data);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            edTxt_login.setHint("Надо что-нибудь ввести!");
        }
    }
}
