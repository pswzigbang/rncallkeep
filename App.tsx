import messaging from "@react-native-firebase/messaging";
import React, { useEffect } from "react";
import { SafeAreaView, DeviceEventEmitter } from "react-native";
import RNCallKeep from "react-native-callkeep";

import Callkeep from "./src/Callkeep";
import { displayIncomingCall, getRandomNumber } from "./src/lib/callkeep";

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
      // console.log("A new FCM message arrived!", remoteMessage);
      // Alert.alert("A new FCM message arrived!", JSON.stringify(remoteMessage));
      if (remoteMessage.data?.body === "t1") {
        console.log("t1 !!!");
        displayIncomingCall(getRandomNumber());
      }
    });

    // 이벤트 리스너 등록
    DeviceEventEmitter.addListener("incomingCall", (data) => {
      console.log("data", data);
      // 여기서 RNCallKeep을 사용해 전화를 시작하거나 화면을 업데이트 합니다.
      RNCallKeep.displayIncomingCall(data.uuid, data.phoneNumber, data.phoneNumber, "number", true);
    });

    getFcmToken();
    return () => {
      unsubscribe();
      DeviceEventEmitter.removeAllListeners();
    };
  }, []);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      {/* <Text>123</Text> */}
      <Callkeep />
    </SafeAreaView>
  );
};

export default App;
