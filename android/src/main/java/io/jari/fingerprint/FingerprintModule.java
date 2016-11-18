package io.jari.fingerprint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public final class FingerprintModule extends ReactContextBaseJavaModule {

    private static final int FINGERPRINT_ACQUIRED_AUTH_FAILED = 999;
    private static boolean IS_CANCELED = false;
    private static CancellationSignal CANCELLATION_SIGNAL;

    private final FingerprintManagerCompat fpm;

    public FingerprintModule(final ReactApplicationContext rctx) {
        super(rctx);
        fpm = FingerprintManagerCompat.from(rctx);
    }

    @Override
    public final String getName() {
        return "FingerprintAndroid";
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public final void hasPermission(final Promise promise) {
        promise.resolve(
                ActivityCompat.checkSelfPermission(
                        getReactApplicationContext(),
                        Manifest.permission.USE_FINGERPRINT
                ) == PackageManager.PERMISSION_GRANTED
        );
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public final void hasEnrolledFingerprints(final Promise promise) {
        promise.resolve(fpm.hasEnrolledFingerprints());
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public final void isHardwareDetected(final Promise promise) {
        promise.resolve(fpm.isHardwareDetected());
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public final void authenticate(final Promise promise) {
        try {
            IS_CANCELED = false;
            CANCELLATION_SIGNAL = new CancellationSignal();
            fpm.authenticate(
                    null,
                    0,
                    CANCELLATION_SIGNAL,
                    new AuthenticationCallback(promise),
                    null
            );
        } catch (SecurityException ex) {
            promise.reject(
                    new Exception(
                            "Ensure the USE_FINGERPRINT permission is specified in AndroidManifest.xml"
                    )
            );
        }
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public final void isAuthenticationCanceled(final Promise promise) {
        promise.resolve(IS_CANCELED);
    }

    @ReactMethod
    @SuppressWarnings("unused")
    public final void cancelAuthentication(final Promise promise) {
        if (!(IS_CANCELED && CANCELLATION_SIGNAL == null)) {
            CANCELLATION_SIGNAL.cancel();
            IS_CANCELED = CANCELLATION_SIGNAL.isCanceled();
        }
        promise.resolve(null);
    }

    private final class AuthenticationCallback extends FingerprintManagerCompat.AuthenticationCallback {

        private final Promise promise;

        private AuthenticationCallback(final Promise promise) {
            this.promise = promise;
        }

        @Override
        public final void onAuthenticationError(final int errorCode, final CharSequence errorString) {
            super.onAuthenticationError(errorCode, errorString);
            IS_CANCELED = CANCELLATION_SIGNAL.isCanceled();
            promise.reject(Integer.toString(errorCode), errorString.toString());
        }

        @Override
        public void onAuthenticationHelp(final int helpCode, final CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            WritableNativeMap writableNativeMap = new WritableNativeMap();
            writableNativeMap.putInt("code", helpCode);
            writableNativeMap.putString("message", helpString.toString());
            FingerprintModule.this.getReactApplicationContext().getJSModule(
                    DeviceEventManagerModule.RCTDeviceEventEmitter.class
            ).emit("onFingerprintAuthenticationHelp", writableNativeMap);
        }

        @Override
        public final void onAuthenticationSucceeded(final FingerprintManagerCompat.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            promise.resolve(null);
        }

        @Override
        public final void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            WritableNativeMap writableNativeMap = new WritableNativeMap();
            writableNativeMap.putInt("code", FINGERPRINT_ACQUIRED_AUTH_FAILED);
            writableNativeMap.putString("message", "Invalid fingerprint");
            FingerprintModule.this.getReactApplicationContext().getJSModule(
                    DeviceEventManagerModule.RCTDeviceEventEmitter.class
            ).emit("onFingerprintAuthenticationFailed", writableNativeMap);
        }
    }
}
