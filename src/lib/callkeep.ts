import RNCallKeep from "react-native-callkeep";
import { v4 as uuidv4 } from "uuid";

export const getNewUuid = () => uuidv4();
export const getRandomNumber = () => String(Math.floor(Math.random() * 100000));
export const displayIncomingCall = (number: string) => {
  const callUUID = getNewUuid();

  RNCallKeep.displayIncomingCall(callUUID, number, number, "number", false);
  return callUUID;
};
