package dev.bobbucks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import dev.bobbucks.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button mSetButton;
    EditText mUsernameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSetButton = (Button) findViewById(R.id.set_button);
        mSetButton.setOnClickListener(this);

        mUsernameInput = (EditText) findViewById(R.id.username_input);

        SharedPreferences sharedPref = getSharedPreferences("dev.bobbucks.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username_key", null);
        if (username != null) {
            mUsernameInput.setText(username);
        }

        mUsernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                mSetButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == mSetButton) {
            SharedPreferences sharedPref = getSharedPreferences("dev.bobbucks.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username_key", mUsernameInput.getText().toString());
            editor.apply();
            mUsernameInput.clearFocus();
            mSetButton.setEnabled(false);
        }
    }
}
