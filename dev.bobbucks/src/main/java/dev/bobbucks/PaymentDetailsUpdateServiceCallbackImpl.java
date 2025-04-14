package dev.bobbucks;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.chromium.components.payments.IPaymentDetailsUpdateService;
import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback;

public class PaymentDetailsUpdateServiceCallbackImpl extends Service {
    private static final String TAG = "PaymentDetailsUpdateServiceCallbackImpl";

    // TODO: Find a proper way to save the Chrome service and make it available to the PaymentActivity.
    public static IPaymentDetailsUpdateService sChromeService = null;

    private final IPaymentDetailsUpdateServiceCallback.Stub mBinder = new IPaymentDetailsUpdateServiceCallback.Stub() {
        @Override
        public void updateWith(Bundle updatedPaymentDetails) throws RemoteException {
            Log.e(TAG, "updateWith called");
            Log.e(TAG, "\terror: " + updatedPaymentDetails.getString("error"));
            Log.e(TAG, "\tstringifiedPaymentMethodErrors: "
                    + updatedPaymentDetails.getString("stringifiedPaymentMethodErrors"));
        }

        @Override
        public void paymentDetailsNotUpdated() throws RemoteException {
            Log.e(TAG, "paymentDetailsNotUpdated called");
        }

        @Override
        public void setPaymentDetailsUpdateService(IPaymentDetailsUpdateService service) throws RemoteException {
            Log.e(TAG, "setPaymentDetailsUpdateService called");
            sChromeService = service;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public IPaymentDetailsUpdateServiceCallback.Stub getBinder() {
        return mBinder;
    }
}
