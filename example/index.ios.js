//@flow

import React, { Component } from 'react';
import { AppRegistry, StyleSheet, Text, View } from 'react-native';
import { default as Fingerprint } from 'react-native-fingerprint-android';

class example extends Component {
    state = {
        enrolled: undefined,
        supported: undefined,
        permission: undefined
    }
    
    async componentDidMount() {
        this.setState({
            enrolled: await Fingerprint.hasEnrolledFingerprints(),
            supported: await Fingerprint.isHardwareDetected(),
            permission: await Fingerprint.hasPermission(),
        })
    }
    
    render() {
        return (
            <View style={styles.container}>
                <Text style={styles.welcome}>
                    Welcome to FingerprintAndroid!
                </Text>
                <Text style={styles.instructions}>
                    However, as the name might suggest, this library is android-only.
                </Text>
                <Text style={styles.instructions}>
                    This version of the app only exists to check if iOS doesn't crash.
                </Text>
                <Text style={styles.instructions}>
                    Hardware detected (should be false): {this.state.supported}
                </Text>
                <Text style={styles.instructions}>
                    Has permission (should be false): {this.state.permission}
                </Text>
                <Text style={styles.instructions}>
                    Is enrolled (should be false): {this.state.enrolled}
                </Text>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
});

AppRegistry.registerComponent('example', () => example);
