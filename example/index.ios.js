/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from 'react-native';
import FingerprintAndroid from 'react-native-fingerprint-android';

class example extends Component {
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
          The following should say 'false': {FingerprintAndroid.isHardwareSupported()}
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
