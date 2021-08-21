package com.example.petcam.ui.chatting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.CHAT_DATA;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.MESSAGE_TEXT;
import static com.example.petcam.function.App.NOTIFI_ROOM_ID;
import static com.example.petcam.function.App.RECEIVE_DATA;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.ROOM_STATUS;
import static com.example.petcam.function.App.SEND_TIME;
import static com.example.petcam.function.App.USER_INVITE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_NICKNAME;
import static com.example.petcam.function.App.USER_UID;

public class ChattingService extends Service {

    private static final String TAG = "ChattingService";
    private final static String IP = "15.164.220.155"; // 서버 접속 IP
    private final static String PORT = "8888"; // 서버 접속 PORT

    // 서버 관련
    SocketClient client;
    ReceiveThread receive;
    SendThread send;
    Handler msghandler;
    Socket socket;
    Message hdmsg;
    String messageData;

    // 알림 관련 (메시지 보낸 유저 정보)
    private ServiceApi mServiceApi;
    private SharedPreferences pref;
    String userID, userName;
    String sendUserID;
    String sendUserName;
    Bitmap bitmap;
    private List<ChatroomItem> mChatroomList;
    NotificationManager notificationManager;
    Notification notifiMessage;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // SharedPreferences 에 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 아이디
        userName = pref.getString(USER_NAME, ""); // 유저 닉네임

        createNotificationChannel(getApplicationContext());


        Log.d(TAG, "==================================================================");
        Log.d(TAG, "on Create() : 서버와 소켓 연결");
        Log.d(TAG, "==================================================================");
        // Client 연결부
        client = new SocketClient(IP, PORT);
        // 시작
        client.start();

    }
    // =========================================================================================================

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // NotificationManager 객체화
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // ChattingActivity 에서 넘겨받은 message 데이터가 있을 경우,
        if (messageData != null) {
            messageData = intent.getStringExtra(CHAT_DATA); // 보낼 메시지 데이터
        } else {
            messageData = "Create new room";
        }

        // 메시지를 보낼 경우 sendThread 실행
        send = new SendThread(socket);
        send.start();


        // 메시지를 받았을 경우, 받은 메시지를 처리하는 메소드
        receiveHandler();
        return super.onStartCommand(intent, flags, startId);
    }
    // =========================================================================================================

    // ReceiveThread 통해서 받은 메세지를 Handler 로 MainThread 에서 처리
    @SuppressLint("HandlerLeak")
    private void receiveHandler() {

        // 받은 메세지를 처리하는 핸들러 (서버에서 받은 메시지를 액티비티로 보낸다.)
        msghandler = new Handler() {
            @Override
            public void handleMessage(Message hdmsg) {
                if (hdmsg.what == 1111) { // 만약 메세지 코드가 1111이라면,
                    String message = hdmsg.obj.toString(); // 받은 메세지

                    try {
                        JSONObject jsonObject = new JSONObject(message);


                        String room_id = jsonObject.getString(ROOM_ID);
                        String status = jsonObject.getString(ROOM_STATUS);
                        String sender_uid = jsonObject.getString(USER_UID);
                        String sender_name = jsonObject.getString(USER_NICKNAME);
                        String message_text = jsonObject.optString(MESSAGE_TEXT, "text on no value");
                        String send_time = jsonObject.getString(SEND_TIME);
                        String invite_user = jsonObject.getString(USER_INVITE);

                        String[] timeSplit = send_time.split("_");
                        String date = timeSplit[0];
                        String time = timeSplit[1];

                        // 내가 보낸 메시지일 경우 알림을 보내지 않는다.
                        if (sender_uid.equals(userID)) {
                            Log.d(TAG, "내가 보낸 메시지 : NOTIFI(X)");
                        } else {
                            getChatroom(userID, room_id, message_text); // 메세지를 보낸 사람의 프로필 사진과 메세지 내용을 노티피케이션을 통해 띄우기
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "==================================================================");
                    Log.d(TAG, "브로드 캐스트 액션 : com.dwfox.myapplication.SEND_BROAD_CAST");
                    Log.d(TAG, "==================================================================");

                    Intent sendIntent = new Intent("com.dwfox.myapplication.SEND_BROAD_CAST");
                    sendIntent.putExtra(RECEIVE_DATA, message);
                    sendBroadcast(sendIntent);
                }
            }
        };
    }

    // =========================================================================================================
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // =========================================================================================================
    // 소켓 연결부
    class SocketClient extends Thread {
        boolean threadAlive;
        String ip;
        String port;

        //InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedReader br = null;

        private DataOutputStream output = null;

        public SocketClient(String ip, String port) {
            threadAlive = true;
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {

            try {
                // 스트림 소켓을 만들고 명명된 호스트의 지정된 포트 번호에 연결
                socket = new Socket(ip, Integer.parseInt(port));
                Log.d(TAG, "*************** 소켓 연결 ***************");
                // 이 소켓의 출력 스트림을 리턴
                output = new DataOutputStream(socket.getOutputStream());
                // 연결후 바로 ReceiveThread 시작 (수신 대기 상태)
                receive = new ReceiveThread(socket);
                receive.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================================================================
    // 메세지 수신 스레드
    class ReceiveThread extends Thread {
        private Socket socket = null;
        DataInputStream input;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
            // 메세지를 받는다.
            try {
                // 이 소켓의 입력 스트림을 리턴
                input = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 메시지 수신 후 Handler 전달
        public void run() {
            try {
                while (input != null) {
                    // 읽어온 메시지 (받은 메시지)
                    String msg = input.readUTF();
                    // 받은 메시지가 있다면, 핸들러 실행
                    if (msg != null) {
                        Log.d(TAG, "==================================================================");
                        Log.d(TAG, "ReceiveThread : 서버에서 날아온 메세지를 받는다.");
                        Log.d(TAG, "==================================================================");

                        hdmsg = msghandler.obtainMessage();
                        hdmsg.what = 1111;
                        hdmsg.obj = msg;
                        msghandler.sendMessage(hdmsg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================================================================
    // 메세지 전송 스레드
    class SendThread extends Thread {
        private final Socket socket;

        DataOutputStream output;

        public SendThread(Socket socket) {
            this.socket = socket;
            try {
                // 이 소켓의 출력 스트림을 리턴
                output = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
            }
        }

        public void run() {

            try {
                if (output != null) {
                    if (messageData != null) {
                        Log.d(TAG, "==================================================================");
                        Log.d(TAG, "SendThread에서 서버로 메세지를 보낸다.");
                        Log.d(TAG, "==================================================================");
                        Log.d("SEND THREAD", "보낸 메시지 값 : " + messageData);
                        // 메시지를 보낸다.
                        output.writeUTF(messageData);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
    }

    // =========================================================================================================
    public static void createNotificationChannel(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                // NotificationChannel 초기화
                NotificationChannel notificationChannel = new NotificationChannel("777", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel
                notificationChannel.setDescription("푸시알림");
                notificationChannel.enableLights(true); // 화면활성화 설정
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500}); // 진동패턴 설정
                notificationChannel.enableVibration(true); // 진동 설정
                notificationManager.createNotificationChannel(notificationChannel); // channel 생성
            }
        } catch (NullPointerException nullException) {
            // notificationManager null 오류 raise
            Toast.makeText(context, "푸시 알림 채널 생성에 실패했습니다. 앱을 재실행하거나 재설치해주세요.", Toast.LENGTH_SHORT).show();
            nullException.printStackTrace();
        }
    }

    // =========================================================================================================
    // 채팅룸 리스트 가져오기 (알림 뿌려주기 위한 roomID 구분)
    private void getChatroom(String uid, String roomID, String message) {

        Call<List<ChatroomItem>> call = mServiceApi.getChatroom(uid);
        call.enqueue(new Callback<List<ChatroomItem>>() {
            @Override
            public void onResponse(Call<List<ChatroomItem>> call, Response<List<ChatroomItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mChatroomList = response.body();
                    for (int i = 0; i < mChatroomList.size(); i++) {
                        if (mChatroomList.get(i).getChatroom_id().equals(roomID)) {
                            getNotifyInfo(message, roomID);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<List<ChatroomItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    private void getNotifyInfo(final String content, final String room_number) {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                try {
                    Bitmap bitmap = Glide.with(getApplicationContext()).asBitmap().load("https://petcam.s3.ap-northeast-2.amazonaws.com/assets/pet-icon.png").into(100, 100).get();

                    Log.d(TAG, "==================================================================");
                    Log.d(TAG, "알림이 작동하는 곳");
                    Log.d(TAG, "==================================================================");

                    // 알람을 클릭했을 때, 특정 액티비티를 활성화시킬 인텐트 객체 준비
                    Intent intent = new Intent(ChattingService.this, ChattingActivity.class);
                    intent.putExtra(NOTIFI_ROOM_ID, room_number);
                    PendingIntent pendingIntent = PendingIntent.getActivity(ChattingService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    notifiMessage = new NotificationCompat.Builder(getApplicationContext(), "777")
                            .setContentTitle(userName) // 알림의 상단바 (제목) 설정
                            .setContentText(content) // 알림의 하단바 (내용) 설정
                            .setLargeIcon(bitmap)
                            .setSmallIcon(R.drawable.pet_icon)
                            .setTicker("Petcam") // 알림이 뜰 때 잠깐 표시되는 메세지
                            .setWhen(System.currentTimeMillis()) // 알림이 표시되는 시간 설정
                            .setContentIntent(pendingIntent) // 알람 클릭 시 반응
                            .setAutoCancel(true) // 클릭하면 자동으로 알람이 사라지도록
                            .build();

                    notifiMessage.defaults = Notification.DEFAULT_VIBRATE; // 소리 or 진동 추가
                    notifiMessage.flags = Notification.FLAG_ONLY_ALERT_ONCE; // 알림 소리를 한번만 내도록 한다.
                    notifiMessage.flags = Notification.FLAG_AUTO_CANCEL; // 확인하면 자동으로 알림이 제거 되도록 한다.


                    if (notificationManager != null) {
                        notificationManager.notify(0, notifiMessage);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}