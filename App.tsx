import React from "react";
import { PermissionsAndroid, SafeAreaView, Text } from "react-native";
import RNCallKeep from "react-native-callkeep";

import Callkeep from "./src/Callkeep";

RNCallKeep.setup({
  ios: {
    appName: "CallKeepDemo",
  },
  android: {
    alertTitle: "Permissions required",
    alertDescription: "This application needs to access your phone accounts",
    cancelButton: "Cancel",
    okButton: "ok",
    imageName: "phone_account_icon",
    additionalPermissions: [PermissionsAndroid.PERMISSIONS.example],
    // Required to get audio in background when using Android 11
    foregroundService: {
      channelId: "com.company.my",
      channelName: "Foreground service for my app",
      notificationTitle: "My app is running on background",
      notificationIcon: "Path to the resource icon of the notification",
    },
  },
});

const App = () => {
  return (
    <SafeAreaView style={{ flex: 1 }}>
      {/* <Text>123</Text> */}
      <Callkeep />
    </SafeAreaView>
  );
};

export default App;
