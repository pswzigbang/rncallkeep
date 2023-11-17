import React from "react";
import { PermissionsAndroid, SafeAreaView, Text } from "react-native";
import RNCallKeep from "react-native-callkeep";

import Callkeep from "./src/Callkeep";

const App = () => {
  return (
    <SafeAreaView style={{ flex: 1 }}>
      {/* <Text>123</Text> */}
      <Callkeep />
    </SafeAreaView>
  );
};

export default App;
