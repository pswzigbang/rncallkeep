import messaging from "@react-native-firebase/messaging";
import { AppRegistry, PermissionsAndroid } from "react-native";
import { v4 as uuidv4 } from "uuid";
import RNCallKeep from "react-native-callkeep";

import App from "./App";
import { name as appName } from "./app.json";

const getNewUuid = () => uuidv4();
const getRandomNumber = () => String(Math.floor(Math.random() * 100000));
const displayIncomingCall = (number) => {
  const callUUID = getNewUuid();

  RNCallKeep.displayIncomingCall(callUUID, number, number, "number", false);
};

// Handle background messages using setBackgroundMessageHandler
messaging().setBackgroundMessageHandler(async (remoteMessage) => {
  console.log("Message handled in the background!", remoteMessage);

  if (remoteMessage.notification?.body === "t1") {
    console.log("t1 !!!");
    displayIncomingCall(getRandomNumber());
  }
});

PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);

AppRegistry.registerHeadlessTask("RNCallKeepBackgroundMessage", () => ({ name, callUUID, handle }) => {
  // Make your call here

  console.log({ name, callUUID, handle });

  return Promise.resolve();
});

AppRegistry.registerComponent(appName, () => App);
