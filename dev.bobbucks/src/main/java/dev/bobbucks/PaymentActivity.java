package dev.bobbucks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    private final Handler mHandler = new Handler();
    private boolean mError;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);

        // The default result is cancelled; this will be set to success instead in the 'Continue'
        // click listener if the user accepts.
        setResultData(RESULT_CANCELED);

        findViewById(R.id.continue_button).setOnClickListener(this);

        SharedPreferences sharedPref = getSharedPreferences("dev.bobbucks.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username_key", null);
        String welcomeText = "Welcome " + (username != null ? username : "non-signed in user") + "!";
        ((TextView) findViewById(R.id.username_text)).setText(welcomeText);

        Intent callingIntent = getIntent();
        if (null == callingIntent) {
            showError("Calling intent is null.");
            return;
        }

        Bundle extras = callingIntent.getExtras();
        if (extras == null) {
            showError("Calling intent contains no extras.");
            return;
        }

        String totalString = extras.getString("total");
        if (TextUtils.isEmpty(totalString)) {
            showError("No total in the extras.");
            return;
        }

        JSONObject totalJson = null;
        try {
            totalJson = new JSONObject(totalString);
        } catch (JSONException e) {
            showError("Cannot parse the total into JSON.");
            return;
        }

        // TODO: Replace container with just a single TextView.
        LinearLayout container = (LinearLayout) findViewById(R.id.line_items);
        if (!addItem(container, totalJson)) showError("Invalid total.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Layout the app to be a half-height modal at the bottom of the screen.
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            int displayHeight = getWindowManager().getCurrentWindowMetrics().getBounds().height();
            attrs.height = (int) (displayHeight * 0.5);
            attrs.width = getWindowManager().getCurrentWindowMetrics().getBounds().width();
            attrs.x = 0;
            attrs.y = 0;
            attrs.gravity = Gravity.BOTTOM;
            getWindow().setAttributes(attrs);
        }
    }
    private boolean addItem(LinearLayout container, JSONObject item) {
        if (item == null) return false;

        JSONObject amount = item.optJSONObject("amount");
        if (amount == null) return false;

        TextView line = new TextView(this);
        line.setText(String.format("%s %s %s", item.optString("label"),
                amount.optString("currency"), amount.optString("value")));
        container.addView(line);
        return true;
    }

    private void showError(String error) {
        mError = true;
        ((TextView) findViewById(R.id.error_message)).setText(error);
    }

    @Override
    public void onClick(View v) {
        setResultData(mError ? RESULT_CANCELED : RESULT_OK);
        finish();
    }

    private void setResultData(int resultType) {
        Intent result = new Intent();
        Bundle extras = new Bundle();
        extras.putString("methodName", "https://bobbucks.dev/pay");
        extras.putString("instrumentDetails", "{\"token\": \"put-some-data-here\"}");
        result.putExtras(extras);
        setResult(resultType, result);
    }
}
