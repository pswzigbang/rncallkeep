import messaging from "@react-native-firebase/messaging";
import React, { useEffect } from "react";
import { Alert, SafeAreaView } from "react-native";
import RNCallKeep from "react-native-callkeep";

import Callkeep from "./src/Callkeep";

const App = () => {
  useEffect(() => {
    const options = {
      ios: {
        appName: "VideoSDK",
      },
      android: {
        alertTitle: "Permissions required",
        alertDescription: "This application needs to access your phone accounts",
        cancelButton: "Cancel",
        okButton: "ok",
        imageName: "phone_account_icon",
      },
    };
    RNCallKeep.setup(options as any);
    RNCallKeep.setAvailable(true);

    // if (Platform.OS === "android") {
    //   OverlayPermissionModule.requestOverlayPermission();
    // }
  }, []);

  async function getFcmToken() {
    const token = await messaging().getToken();
    if (token) {
      console.log("Your Firebase Token is:", token);
    } else {
      console.log("Failed to get Firebase Token");
    }
  }

  useEffect(() => {
    const unsubscribe = messaging().onMessage(async (remoteMessage) => {
      console.log("A new FCM message arrived!", remoteMessage);
      Alert.alert("A new FCM message arrived!", JSON.stringify(remoteMessage));
    });

    getFcmToken();
    return unsubscribe;
  }, []);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      {/* <Text>123</Text> */}
      <Callkeep />
    </SafeAreaView>
  );
};

export default App;
