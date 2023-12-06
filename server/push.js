const admin = require("firebase-admin");

const serviceAccount = require("./callkeep-poc-f5129-40e66eca3487.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const token =
  "cWkrdjXUQMS8LIoFR1y43z:APA91bGpWUMZKn5DiguzOH99f1xQ4D0Za0aemegJZY-Q8gckb3Zp5PJW0JLCY5LBbcxPgzS-UdluL0SExSvOfE-u7kIUasw4M1Lcj8aXjTASbTwFO4R8ZB1jRsObaaRWw6YhhsqsPhmX";

const message = {
  data: {
    title: "t1",
    body: "t1",
  },
  token,
  android: {
    priority: "high",
  },
};

admin
  .messaging()
  .send(message)
  .then((response) => {
    console.log("Successfully sent message:", response);
  })
  .catch((error) => {
    console.log("Error sending message:", error);
  });
