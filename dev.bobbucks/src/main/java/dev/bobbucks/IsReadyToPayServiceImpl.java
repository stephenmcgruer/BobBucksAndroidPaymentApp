package dev.bobbucks;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.chromium.IsReadyToPayService;
import org.chromium.IsReadyToPayServiceCallback;

public class IsReadyToPayServiceImpl extends Service {
    private static final String TAG = "IsReadyToPayServiceImpl";

    private final IsReadyToPayService.Stub mBinder =
            new IsReadyToPayService.Stub() {
                @Override
                public void isReadyToPay(IsReadyToPayServiceCallback callback) {
                    Log.d(TAG, "isReadyToPay called, returning true");
                    // Check permission here.
                    try {
                        callback.handleIsReadyToPay(true);
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
