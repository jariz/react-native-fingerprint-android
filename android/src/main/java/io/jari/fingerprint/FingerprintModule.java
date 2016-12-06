package io.jari.fingerprint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("MissingPermission")
public class FingerprintModule extends ReactContextBaseJavaModule {
    FingerprintManagerCompat fingerprintManager;
    ReactApplicationContext reactContext;
    CancellationSignal cancellationSignal;
    boolean isCancelled = false;

    final int FINGERPRINT_ACQUIRED_AUTH_FAILED = 999;

    final int FINGERPRINT_ERROR_CANCELED = 5;

    public FingerprintModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.reactContext = reactContext;
        this.fingerprintManager = FingerprintManagerCompat.from(reactContext);
    }

    // Harcdoded from https://developer.android.com/reference/android/hardware/fingerprint/FingerprintManager.html#FINGERPRINT_ACQUIRED_GOOD
    // So we don't depend on FingerprintManager directly
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("FINGERPRINT_ACQUIRED_GOOD", 0);
        constants.put("FINGERPRINT_ACQUIRED_IMAGER_DIRTY", 3);
        constants.put("FINGERPRINT_ACQUIRED_INSUFFICIENT", 2);
        constants.put("FINGERPRINT_ACQUIRED_PARTIAL", 1);
        constants.put("FINGERPRINT_ACQUIRED_TOO_FAST", 5);
        constants.put("FINGERPRINT_ACQUIRED_TOO_SLOW", 4);
        constants.put("FINGERPRINT_ACQUIRED_AUTH_FAILED", FINGERPRINT_ACQUIRED_AUTH_FAILED);
        constants.put("FINGERPRINT_ERROR_CANCELED", FINGERPRINT_ERROR_CANCELED);
        constants.put("FINGERPRINT_ERROR_HW_UNAVAILABLE", 1);
        constants.put("FINGERPRINT_ERROR_LOCKOUT", 7);
        constants.put("FINGERPRINT_ERROR_NO_SPACE", 4);
        constants.put("FINGERPRINT_ERROR_TIMEOUT", 3);
        constants.put("FINGERPRINT_ERROR_UNABLE_TO_PROCESS", 2);
        return constants;
    }

    @ReactMethod
    public void hasPermission(Promise promise) {
        try {
            promise.resolve(ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void hasEnrolledFingerprints(Promise promise) {
        try {
            promise.resolve(fingerprintManager.hasEnrolledFingerprints());
        } catch (SecurityException secEx) {
            Exception exception = new Exception("App does not have the proper permissions. (did you add USE_FINGERPRINT to your manifest?)\nMore info see https://github.com/jariz/react-native-fingerprint-android");
            exception.initCause(secEx);
            promise.reject(exception);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void isHardwareDetected(Promise promise) {
        try {
            promise.resolve(fingerprintManager.isHardwareDetected());
        } catch (SecurityException secEx) {
            Exception exception = new Exception("App does not have the proper permissions. (did you add USE_FINGERPRINT to your manifest?)\nMore info see https://github.com/jariz/react-native-fingerprint-android");
            exception.initCause(secEx);
            promise.reject(exception);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void authenticate(Promise promise) {
        try {
            isCancelled = false;
            cancellationSignal = new CancellationSignal();
            fingerprintManager.authenticate(null, 0, cancellationSignal, new AuthenticationCallback(promise), null);
        } catch (SecurityException secEx) {
            Exception exception = new Exception("App does not have the proper permissions. (did you add USE_FINGERPRINT to your manifest?)\nMore info see https://github.com/jariz/react-native-fingerprint-android");
            exception.initCause(secEx);
            promise.reject(exception);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void isAuthenticationCanceled(Promise promise) {
        promise.resolve(isCancelled);
    }

    @ReactMethod
    public void cancelAuthentication(Promise promise) {
        try {
            if(!isCancelled) {
                cancellationSignal.cancel();
                isCancelled = true;
            }
            promise.resolve(null);
        } catch(Exception e) {
            promise.reject(e);
        }
    }


    @Override
    public String getName() {
        return "FingerprintAndroid";
    }

    public class AuthenticationCallback extends FingerprintManagerCompat.AuthenticationCallback {
        Promise promise;


        public AuthenticationCallback(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            if(errorCode == FINGERPRINT_ERROR_CANCELED) {
                isCancelled = true;
            }
            if(promise == null) {
                throw new AssertionError("Tried to reject the auth promise, but it was already resolved / rejected. This shouldn't happen.");
            }
            promise.reject(Integer.toString(errorCode), errString.toString());
            promise = null;
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);

            WritableNativeMap writableNativeMap = new WritableNativeMap();
            writableNativeMap.putInt("code", helpCode);
            writableNativeMap.putString("message", helpString.toString());
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("fingerPrintAuthenticationHelp", writableNativeMap);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            if(promise == null) {
                throw new AssertionError("Tried to resolve the auth promise, but it was already resolved / rejected. This shouldn't happen.");
            }
            promise.resolve(null);
            promise = null;
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();

            WritableNativeMap writableNativeMap = new WritableNativeMap();
            writableNativeMap.putInt("code", FINGERPRINT_ACQUIRED_AUTH_FAILED);
            writableNativeMap.putString("message", "Fingerprint was recognized as not valid.");
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("fingerPrintAuthenticationHelp", writableNativeMap);
        }


    }
}
