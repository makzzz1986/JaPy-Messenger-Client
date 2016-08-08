package ru.rdtc.makzzz.chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    public static final String NICK = "ru.rdtc.makzzz.chat.NICK";
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginSet(View view) {
        Intent intent = new Intent();
        EditText edTxt_login = (EditText) findViewById(R.id.login_edTxt);

        // проверяем, если пользователь что-нибудь ввёл, то передаём результат в стартовую активити
        if (edTxt_login.getText().length() != 0) {
            Log.i(TAG, "Get nick = " + edTxt_login.getText());
            intent.putExtra(NICK, edTxt_login.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            edTxt_login.setHint("Надо что-нибудь ввести!");
        }
    }
}
