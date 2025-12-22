package dev.bobbucks;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.chromium.components.payments.IPaymentDetailsUpdateService;
import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback;

public class PaymentDetailsUpdateServiceCallbackImpl extends Service {
    private static final String TAG = "PaymentDetailsUpdateServiceCallbackImpl";

    public static final String ACTION_UPDATE_PAYMENT = "dev.bobbucks.UPDATE_PAYMENT";
    public static final String EXTRA_PAYMENT_DETAILS = "updated_payment_details";

    // TODO: Find a proper way to save the Chrome service and make it available to the PaymentActivity.
    public static IPaymentDetailsUpdateService sChromeService = null;

    private final IPaymentDetailsUpdateServiceCallback.Stub mBinder = new IPaymentDetailsUpdateServiceCallback.Stub() {
        @Override
        public void updateWith(Bundle updatedPaymentDetails) throws RemoteException {
            Log.e(TAG, "updateWith called");
            Log.e(TAG, "\terror: " + updatedPaymentDetails.getString("error"));
            Log.e(TAG, "\tstringifiedPaymentMethodErrors: "
                    + updatedPaymentDetails.getString("stringifiedPaymentMethodErrors"));

            Intent intent = new Intent(ACTION_UPDATE_PAYMENT);
            intent.putExtra(EXTRA_PAYMENT_DETAILS, updatedPaymentDetails);
            LocalBroadcastManager.getInstance(PaymentDetailsUpdateServiceCallbackImpl.this).sendBroadcast(intent);
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
