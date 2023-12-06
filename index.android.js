import messaging from "@react-native-firebase/messaging";
import { AppRegistry, PermissionsAndroid } from "react-native";
import RNCallKeep from "react-native-callkeep";

import App from "./App";
import { name as appName } from "./app.json";
import { displayIncomingCall, getRandomNumber } from "./src/lib/callkeep";

// Handle background messages using setBackgroundMessageHandler
messaging().setBackgroundMessageHandler(async (remoteMessage) => {
  console.log("Message handled in the background!", remoteMessage);

  if (remoteMessage.data?.body === "t1") {
    console.log("t1 !!!");
    const randNum = getRandomNumber();
    const uuid = displayIncomingCall(randNum);
    console.log("uuid", uuid);
    RNCallKeep.endCall(uuid);
    RNCallKeep.backToForeground();
    // setTimeout(() => {
    //   console.log("uuid", uuid);
    // }, 1000);
  }
});

RNCallKeep.addEventListener("answerCall", async ({ callUUID }) => {
  RNCallKeep.endCall(callUUID);
  RNCallKeep.backToForeground();

  console.log("navigate");

  //  RootNavigation.navigate("Call", {callUUID});
});

PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);

AppRegistry.registerHeadlessTask("RNCallKeepBackgroundMessage", () => ({ name, callUUID, handle }) => {
  // Make your call here

  console.log({ name, callUUID, handle });

  return Promise.resolve();
});

AppRegistry.registerComponent(appName, () => App);
