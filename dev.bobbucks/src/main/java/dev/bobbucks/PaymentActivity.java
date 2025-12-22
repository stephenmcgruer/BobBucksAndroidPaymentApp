package dev.bobbucks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback;
import org.json.JSONException;
import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PaymentActivity";
    private final Handler mHandler = new Handler();
    private boolean mError;
    private BroadcastReceiver mUpdateReceiver;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);

        // The default result is cancelled; this will be set to success instead in the 'Continue'
        // click listener if the user accepts.
        setResultData(RESULT_CANCELED);

        findViewById(R.id.continue_button).setOnClickListener(this);
        findViewById(R.id.send_update_button).setOnClickListener(this);

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
        if (!addTotal(container, totalJson)) showError("Invalid total.");

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

        mUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (PaymentDetailsUpdateServiceCallbackImpl.ACTION_UPDATE_PAYMENT.equals(action)) {
                    Log.i(TAG, "Received ACTION_UPDATE_PAYMENT broadcast");
                    Bundle bundle = intent.getBundleExtra(PaymentDetailsUpdateServiceCallbackImpl.EXTRA_PAYMENT_DETAILS);
                    if (bundle != null) {
                        String error = bundle.getString("error");
                        if (!TextUtils.isEmpty(error)) {
                            // Can't use showError, because that also sets mError=true which isn't
                            // the case here, we're just showing this for demo purposes.
                            ((TextView) findViewById(R.id.error_message)).setText(
                                    "Received message from website: " + error);
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(PaymentDetailsUpdateServiceCallbackImpl.ACTION_UPDATE_PAYMENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateReceiver);
        }
    }

    private boolean addTotal(LinearLayout container, JSONObject total) {
        if (total == null) return false;

        TextView line = new TextView(this);
        line.setText(String.format("Total %s %s",
                total.optString("currency"), total.optString("value")));
        container.addView(line);
        return true;
    }

    private void showError(String error) {
        mError = true;
        ((TextView) findViewById(R.id.error_message)).setText(error);
    }

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.continue_button)) {
            Log.d(TAG, "Handling 'continue' button press");
            setResultData(mError ? RESULT_CANCELED : RESULT_OK);
            finish();
        } else if (v == findViewById(R.id.send_update_button)) {
            Log.i(TAG, "Handling 'send update' button press");
            if (PaymentDetailsUpdateServiceCallbackImpl.sChromeService == null) {
                Log.e(TAG, "UpdatePaymentDetailsServiceImpl.sChromeService is null");
            } else {
                Bundle paymentHandlerMethodData = new Bundle();
                paymentHandlerMethodData.putString("methodName", "New Method Name");
                IPaymentDetailsUpdateServiceCallback.Stub callbackStub = new PaymentDetailsUpdateServiceCallbackImpl().getBinder();
                try {
                    Log.d(TAG, "Calling into sChromeService.changePaymentMethod");
                    PaymentDetailsUpdateServiceCallbackImpl.sChromeService.changePaymentMethod(paymentHandlerMethodData, callbackStub);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when calling changePaymentMethod:", e);
                }
            }
        } else {
            Log.e(TAG, "onClick, unknown view!");
        }
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
