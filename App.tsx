import React, { useEffect } from "react";
import { PermissionsAndroid, Platform, SafeAreaView, Text } from "react-native";
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

  return (
    <SafeAreaView style={{ flex: 1 }}>
      {/* <Text>123</Text> */}
      <Callkeep />
    </SafeAreaView>
  );
};

export default App;
