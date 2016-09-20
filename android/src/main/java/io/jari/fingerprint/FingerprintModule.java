package io.jari.fingerprint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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
    FingerprintManager fingerprintManager;
    ReactApplicationContext reactContext;
    CancellationSignal cancellationSignal;
    boolean isCancelled = false;

    final int FINGERPRINT_ACQUIRED_AUTH_FAILED = 999;

    public FingerprintModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.reactContext = reactContext;
        this.fingerprintManager = reactContext.getSystemService(FingerprintManager.class);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("FINGERPRINT_ACQUIRED_GOOD", FingerprintManager.FINGERPRINT_ACQUIRED_GOOD);
        constants.put("FINGERPRINT_ACQUIRED_IMAGER_DIRTY", FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY);
        constants.put("FINGERPRINT_ACQUIRED_INSUFFICIENT", FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT);
        constants.put("FINGERPRINT_ACQUIRED_PARTIAL", FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL);
        constants.put("FINGERPRINT_ACQUIRED_TOO_FAST", FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST);
        constants.put("FINGERPRINT_ACQUIRED_TOO_SLOW", FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW);
        constants.put("FINGERPRINT_ACQUIRED_AUTH_FAILED", FINGERPRINT_ACQUIRED_AUTH_FAILED);
        constants.put("FINGERPRINT_ERROR_CANCELED", FingerprintManager.FINGERPRINT_ERROR_CANCELED);
        constants.put("FINGERPRINT_ERROR_HW_UNAVAILABLE", FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE);
        constants.put("FINGERPRINT_ERROR_LOCKOUT", FingerprintManager.FINGERPRINT_ERROR_LOCKOUT);
        constants.put("FINGERPRINT_ERROR_NO_SPACE", FingerprintManager.FINGERPRINT_ERROR_NO_SPACE);
        constants.put("FINGERPRINT_ERROR_TIMEOUT", FingerprintManager.FINGERPRINT_ERROR_TIMEOUT);
        constants.put("FINGERPRINT_ERROR_UNABLE_TO_PROCESS", FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS);
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
            fingerprintManager.authenticate(null, cancellationSignal, 0, new AuthenticationCallback(promise), null);
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

    public class AuthenticationCallback extends FingerprintManager.AuthenticationCallback {
        Promise promise;

        public AuthenticationCallback(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            if(errorCode == FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
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
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
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
