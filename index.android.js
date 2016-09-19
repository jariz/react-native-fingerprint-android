/* @flow */

'use strict';
import { NativeModules, DeviceEventEmitter} from 'react-native';
const FingerprintAndroidNative = NativeModules.FingerprintAndroid;
const OVERRIDDEN_METHODS = ["authenticate"];

export interface FingerprintError {
    code: number;
    message: string;
}

const nop = () => console.warn("NOP'd function called! This should never happen!");

class FingerprintAndroid {
    //nop functions because I love my autocomplete
    static cancelAuthentication(): Promise<null> { nop() };
    static isAuthenticationCanceled(): Promise<boolean> { nop() };
    static hasPermission(): Promise<boolean> { nop() };
    static hasEnrolledFingerprints(): Promise<boolean> { nop() };
    static isHardwareDetected(): Promise<boolean> { nop() };

    //constants
    static FINGERPRINT_ACQUIRED_GOOD:string;
    static FINGERPRINT_ACQUIRED_IMAGER_DIRTY:string;
    static FINGERPRINT_ACQUIRED_INSUFFICIENT:string;
    static FINGERPRINT_ACQUIRED_PARTIAL:string;
    static FINGERPRINT_ACQUIRED_TOO_FAST:string;
    static FINGERPRINT_ACQUIRED_TOO_SLOW:string;
    static FINGERPRINT_ERROR_CANCELED:string;
    static FINGERPRINT_ERROR_HW_UNAVAILABLE:string;
    static FINGERPRINT_ERROR_LOCKOUT:string;
    static FINGERPRINT_ERROR_NO_SPACE:string;
    static FINGERPRINT_ERROR_TIMEOUT:string;
    static FINGERPRINT_ERROR_UNABLE_TO_PROCESS:string;
    static FINGERPRINT_ERROR_AUTH_FAILED:string;

    static async authenticate(warningCallback:?(response:FingerprintError) => {}):Promise<null> {
        if(typeof warningCallback === "function") {
            DeviceEventEmitter.addListener('fingerPrintAuthenticationHelp', warningCallback);
        }

        let err;
        try {
            await FingerprintAndroidNative.authenticate();
        } catch(ex) {
            err = ex
        }
        finally {
            //remove the subscriptions and crash if needed
            DeviceEventEmitter.removeAllListeners("fingerPrintAuthenticationHelp");

            if(err) {
                throw err
            }
        }
    }
}

//add all available functions from the native module (besides authenticate)
Object.keys(FingerprintAndroidNative).forEach(key => {
    if (OVERRIDDEN_METHODS.indexOf(key) === -1) {
        FingerprintAndroid[key] = FingerprintAndroidNative[key];
    }
});


export default FingerprintAndroid;