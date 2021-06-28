package com.example.petcam.streaming;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import static com.example.petcam.function.App.BROADCAST_LIVE_MSG;
import static com.example.petcam.function.App.CHAT_DATA;

public class LiveChatService extends Service {

    private MsgReceiver msgReceiver;
    private SocketChannel socketChannel;
    private final static String TAG = "LiveChatService";
    private final static String IP = "15.164.220.155"; // 채팅 서버 접속을 위한 IP
    private final static int PORT = 5001; // 채팅 서버 접속을 위한 PORT


    @Override
    public void onCreate() {
        super.onCreate();

        msgReceiver = new MsgReceiver(); // 메시지 받는 곳
        Log.d(TAG, "==================================================================");
        Log.d(TAG, "[onCreate()] -> Netty chat server start!");
        Log.d(TAG, "==================================================================");

        Log.d(TAG, "Client : " + IP + PORT);
        new ChatClient(IP, PORT).start(); // 클라이언트 - 서버 연결
    }

    // =========================================================================================================

    // 서버와 연결하기 위한 클래스
    class ChatClient extends Thread {

        private final String host;
        private final int port;

        public ChatClient(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public void run() {
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(true);
                socketChannel.connect(new InetSocketAddress (host, port));
                Log.d(TAG, "Connect client (socket 정보) : " + socketChannel.socket());
            } catch (IOException e) {
                Log.e(TAG, "Connect error : " + e.getMessage());
                e.printStackTrace();
            }
            msgReceiver.start();
        }
    }
    // =========================================================================================================

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "[onStartCommand()] Start . . .");
        if(intent != null) {
            String command = intent.getStringExtra(CHAT_DATA); // StreamingActivity 에서 받아온 intent (message)

            if (command == null) {
                return Service.START_STICKY;
            } else {
                Log.d(TAG, "Send message : " + command);
                new SendmsgTask().execute(command); // message 서버로 전송
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // =========================================================================================================

    // send message
    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            Log.d(TAG, "==================================================================");
            Log.d(TAG, "[SendMsgTask] -> Send data");
            Log.d(TAG, "==================================================================");
            Log.d(TAG, "Send socket : " + socketChannel.socket());
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로 전달
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // =========================================================================================================

    // receive message
    class MsgReceiver extends Thread{
        Handler handler = new Handler();

        public void run(){
            while(true){
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                try {
                    int readByteCount = socketChannel.read(byteBuffer);
                    if(readByteCount==-1){
                        throw new IOException();
                    }
                    byteBuffer.flip();
                    Charset charset = Charset.forName("UTF-8");
                    String receiveData = charset.decode(byteBuffer).toString();
                    JSONObject jsonObject = new JSONObject(new String(receiveData)); // 받아온 String 을 JSONObject 로 저장
                    String type = jsonObject.getString("type"); // 메시지 분류를 위한 Type 가져오기

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 메시지 타입에 따라 처리 (message, streaming_finish)
                            switch (type) {

                                // 기본적인 메시지를 받았을 때 실행한다.
                                case "message":

                                    try {
                                        String room_id = jsonObject.getString("room_id");
                                        int sender_id = jsonObject.getInt("id");
                                        String sender_name = jsonObject.getString("name");
                                        String sender_profile = jsonObject.getString("profile");
                                        String sender_message = jsonObject.getString("message");

                                        Log.d(TAG, "[Service] room_id : " + room_id);
                                        Log.d(TAG, "[Service] sender_id : " + sender_id);
                                        Log.d(TAG, "[Service] sender_name : " + sender_name);
                                        Log.d(TAG, "[Service] sender_profile : " + sender_profile);
                                        Log.d(TAG, "[Service] sender_message : " + sender_message);

                                        Intent messageIntent = new Intent(BROADCAST_LIVE_MSG);
                                        messageIntent.putExtra("type", "message");
                                        messageIntent.putExtra("room_id", room_id); // 방번호
                                        messageIntent.putExtra("id", sender_id); // uid
                                        messageIntent.putExtra("name", sender_name); // 닉네임
                                        messageIntent.putExtra("profile", sender_profile); // 프로필 사진
                                        messageIntent.putExtra("message", sender_message); // 메시지 내용
                                        sendBroadcast(messageIntent);

                                    } catch (Exception ignored) {

                                    }
                                    break;

                                // 영상 스트리밍 방송 종료시 실행한다.
                                case "liveOff":
                                    Log.d(TAG, "[Service] type : liveOff");

                                    try {
                                        String room_id = jsonObject.getString("room_id");

                                        // 방송 종료 알림 보내기 (StreamingPlayerActivity)로 보낸다.
                                        Intent streamingIntent = new Intent(BROADCAST_LIVE_MSG);
                                        streamingIntent.putExtra("type", "liveOff");
                                        streamingIntent.putExtra("room_id", room_id); // 방번호
                                        sendBroadcast(streamingIntent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                // 채팅 저장을 위해 시간을 전송한다. (추후 VOD 영상과의 싱크를 위해 필요)
                                case "time":

                                    try {
                                        String liveTime = jsonObject.getString("liveTime");
                                        String room_id = jsonObject.getString("room_id");

                                        Intent intent = new Intent(BROADCAST_LIVE_MSG);
                                        intent.putExtra("type", "time");
                                        intent.putExtra("room_id", room_id);
                                        intent.putExtra("liveTime", liveTime); // 현재 라이브 시간
                                        sendBroadcast(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "io receive error : " + e.getMessage());
                    try {
                        socketChannel.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    break;

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "json receive error : " + e.getMessage());
                }
            }
        }
    }

    // =========================================================================================================

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemmented");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
