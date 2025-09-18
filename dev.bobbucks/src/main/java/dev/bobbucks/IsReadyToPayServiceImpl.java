package dev.bobbucks;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.chromium.IsReadyToPayService;
import org.chromium.IsReadyToPayServiceCallback;
import org.json.JSONException;
import org.json.JSONObject;

public class IsReadyToPayServiceImpl extends Service {
    private static final String TAG = "IsReadyToPayServiceImpl";

    private final IsReadyToPayService.Stub mBinder =
            new IsReadyToPayService.Stub() {
                @Override
                public void isReadyToPay(IsReadyToPayServiceCallback callback, Bundle parameters) {
                    Log.d(TAG, "isReadyToPay called, returning true");
                    Log.d(TAG, "parameters: " + (parameters == null ? "<null>" : parameters.toString()));

                    // For testing, this sample app allows the website to directly override what
                    // the return value of IS_READY_TO_PAY will be. In practice, you should
                    // determine this based on internal app state (e.g., is the current user logged
                    // in, do they have a payment method on file, etc).
                    boolean returnValue = true;
                    if (parameters != null) {
                        Log.d(TAG, "parameters keys: " + parameters.keySet().toString());
                        String data = parameters.getString("data");
                        if (!data.isEmpty()) {
                            Log.d(TAG, "data: " + data);
                            try {
                                JSONObject dataObject = new JSONObject(data);
                                if (dataObject.has("returnValue")) {
                                    try {
                                        returnValue = dataObject.getBoolean("returnValue");
                                        Log.d(TAG, "Overriding return value to " + returnValue);
                                    } catch (JSONException e) {
                                        Log.d(TAG, "'returnValue' parameter was not a boolean: " + data);
                                    }
                                } else {
                                    Log.d(TAG, "no 'returnValue' present in data");
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Unable to parse data as JSON: " + data);
                            }
                        } else {
                            Log.d(TAG, "No 'data' passed into PaymentRequest");
                        }
                    }

                    // Check permission here.
                    try {
                        callback.handleIsReadyToPay(returnValue);
                    } catch (RemoteException e) {
                        // Ignore.
                    }
                }
            };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
