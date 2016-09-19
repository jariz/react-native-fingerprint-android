export default class FingerprintAndroid {
    static async cancelAuthentication(): Promise<null> { 
        return null;
    };
    static async isAuthenticationCanceled(): Promise<boolean> { 
        return false;
    };

    static async hasPermission(): Promise<boolean> {
        return false;
    };

    static async hasEnrolledFingerprints(): Promise<boolean> {
        return false;
    };

    static async isHardwareDetected(): Promise<boolean> {
        return false;
    };

    static async authenticate(warningCallback:?(response:FingerprintError) => {}):Promise<null> {
        throw new Error("Believe it or not, but react-native-fingerprint-android actually doesn't support iOS.")
    }
}