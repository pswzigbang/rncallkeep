const WebSocket = require("ws");
const express = require("express");

const app = express();
const port = 3000;

app.use(express.static(__dirname));

app.listen(port, () => {
  console.log(`서버가 http://localhost:${port} 에서 실행 중입니다.`);
});

// 웹소켓 서버 생성
const wss = new WebSocket.Server({ port: 8080 });

// 연결 이벤트 핸들링
wss.on("connection", function connection(ws) {
  console.log("A new client connected.");

  // 클라이언트로부터 메시지 수신
  ws.on("message", function incoming(message) {
    console.log("received: %s", message);

    wss.clients.forEach(function each(client) {
      if (client.readyState === WebSocket.OPEN) {
        client.send(message.toString());
      }
    });
  });

  // 연결 종료 이벤트
  ws.on("close", () => {
    console.log("Client has disconnected");
  });
});

console.log("WebSocket server is running on ws://localhost:8080");
