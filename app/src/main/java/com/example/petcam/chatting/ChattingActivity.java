package com.example.petcam.chatting;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.SettingsActivity;
import com.example.petcam.profile.notice.NoticeDetailActivity;
import com.google.android.material.navigation.NavigationView;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.CHAT_DATA;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.MESSAGE_TEXT;
import static com.example.petcam.function.App.MESSAGE_TYPE;
import static com.example.petcam.function.App.NOTIFI_ROOM_ID;
import static com.example.petcam.function.App.RECEIVE_DATA;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.ROOM_NAME;
import static com.example.petcam.function.App.ROOM_STATUS;
import static com.example.petcam.function.App.ROOM_USER_COUNT;
import static com.example.petcam.function.App.NEW_CHATROOM_ID;
import static com.example.petcam.function.App.SEND_TIME;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_INVITE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_NICKNAME;
import static com.example.petcam.function.App.USER_PROFILE_URL;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: ChattingActivity
 *
 * Comment
 * 유저간 채팅이 이루어지는 채팅 액티비티입니다.
 **/

public class ChattingActivity extends AppCompatActivity {

    private static final String TAG = "ChattingActivity";

    private ServiceApi mServiceApi;
    private String userID, userName, userPhoto, currentTime, notifiChatroomNo, chatroomNo, chatroomName, chatroomUserCount, newChatroom, invite_user_name;
    private String data, room_id, sender_uid, sender_name, sender_profile_image, message_text, send_time;
    private SharedPreferences pref;
    private SimpleDateFormat simpleDateFormat;
    private ChattingAdapter adapter;
    private ChatMemberAdapter chatMemberAdapter;
    private List<String> chatUserList, resultList;
    private List<ChatroomItem> mChatroomList;


    BroadcastReceiver mReceiver;

    // 채팅방 상황 (처음 생긴 방인지, 기존 존재하던 방인지 등)
    private String status;
    private static final String START_ROOM = "START_ROOM"; // 최초 접속 ("님이 입장하셨습니다")
    private static final String CHATTING = "CHATTING"; // 채팅 진행 중 (채팅 메시지)
    private static final String EXIT_ROOM = "EXIT_ROOM"; // 멤버 나간 경우 ("님이 퇴장하셨습니다",  db 삭제)
    private static final String DATE = "DATE"; // 접속 (날짜, 시간)

    // 뷰 관련
    private DrawerLayout mDrawerLayout;
    private TextView mRoomName, mRoomUserCount;
    private ImageView mSendButton;
    private EditText mMessage;
    private RecyclerView mChatRecyclerView, mChatMemberRecyclerView;
    private List<ChatMemberItem> mMemberList;
    private List<ChattingItem> mList;
    private List<ChattingDataItem> mDataList;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    new Handler().postDelayed(new Runnable(){
                        public void run(){
                            resetNumMessage(chatroomNo, userID); // 채팅방 안 읽은 메시지 리셋
                            finish(); // 이 액티비티 화면을 닫는다.
                        }
                    },600);
                    break;

                // 버튼을 클릭했을 경우,
                case R.id.iv_open_bar:
                    getChatMember(chatroomNo, userID);
                    mDrawerLayout.openDrawer(GravityCompat.END);
                    break;

                case R.id.layout_invite:

                    if(mDrawerLayout.isDrawerOpen(GravityCompat.END)){
                        mDrawerLayout.closeDrawers();
                    }
                    Intent inviteIntent = new Intent(ChattingActivity.this, SearchUserActivity.class);
                    inviteIntent.putExtra(USER_UID, userID); /// 나의 uid
                    inviteIntent.putExtra(ROOM_ID, chatroomNo); // 방 번호
                    startActivity(inviteIntent);
                    break;
                case R.id.iv_exit:
                    alertDialog(view);
                    break;
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"HandlerLeak", "ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        // 액티비티 시작후 키보드 감추기
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);
//
        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 브로드캐스트의 액션을 등록하기 위한 인텐트 필터
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.dwfox.myapplication.SEND_BROAD_CAST");

        // 현재 날짜, 시간
        simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일_hh:mm a", Locale.KOREA);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);
        findViewById(R.id.iv_open_bar).setOnClickListener(onClickListener);
        findViewById(R.id.layout_invite).setOnClickListener(onClickListener);
        findViewById(R.id.iv_exit).setOnClickListener(onClickListener);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRoomName = (TextView) findViewById(R.id.tv_room_name);
        mRoomUserCount = (TextView) findViewById(R.id.tv_user_count);
        mSendButton = (ImageView) findViewById(R.id.iv_send_message);
        mMessage = (EditText) findViewById(R.id.et_send_message);
        mChatRecyclerView = (RecyclerView) findViewById(R.id.rv_chat);
        mChatMemberRecyclerView = (RecyclerView) findViewById(R.id.rv_chat_users);

        // SharedPreferences 에 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 아이디
        userPhoto = pref.getString(USER_IMAGE, ""); // 유저 프로필 이미지
        userName = pref.getString(USER_NAME, ""); // 유저 닉네임

        Log.e(TAG, "uid : " + userID);
        Log.e(TAG, "user photo : " + userPhoto);
        Log.e(TAG, "user name : " + userName);

        // 채팅방 정보 가져오기 (기존 생성된 방일 경우)
        Intent intent = getIntent();
        chatroomNo = intent.getStringExtra(ROOM_ID);
        notifiChatroomNo =intent.getStringExtra(NOTIFI_ROOM_ID);
        chatroomName = intent.getStringExtra(ROOM_NAME);
        chatroomUserCount = intent.getStringExtra(ROOM_USER_COUNT);
        newChatroom = intent.getStringExtra(NEW_CHATROOM_ID);
        mDataList = new ArrayList<>();

        // 채팅 RecyclerView 선언
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mChatRecyclerView.setLayoutManager(manager); // LayoutManager 등록
        mList = new ArrayList<>();

        // 채팅 유저 RecyclerView 선언
        LinearLayoutManager chatMemberManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mChatMemberRecyclerView.setLayoutManager(chatMemberManager); // LayoutManager 등록
        mMemberList = new ArrayList<>();
        // intent 로 넘어온 채팅 룸 넘버가 없다면, 방을 새로 만들고 DB에 저장한다.
//        if (chatroomNo != null) {
//            mRoomName.setText(chatroomName);
//            mRoomUserCount.setText(chatroomUserCount);
//            // 가장 최근 메시지 보여주기
//            focusCurrentMessage();

//        } else
            if (notifiChatroomNo != null) {
//                getChatroomInfo(notifiChatroomNo, userID);
                chatroomNo = notifiChatroomNo;

            } else if (newChatroom != null) {
                chatroomNo = newChatroom;
                messageTimeCheck(chatroomNo, null);
            }

        // EditText 부분을 클릭했을 경우 클릭 이벤트
        mMessage.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // 가장 최근 메시지 보여주기
                focusCurrentMessage();
                return false;
            }
        });

        // 채팅창을 클릭했을 경우 키보드를 내린다 (숨긴다)
        mChatRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(v.getWindowToken(), 0);

                return false;
            }
        });


        // 메시지 전송 버튼 클릭 이벤트
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                data = mMessage.getText().toString(); // 전송하려는 메시지 내용

                // 메시지가 없다면, '메시지를 입력해 주세요.' 띄우기
                if (data.length() == 0) {
                    Toast.makeText(getApplicationContext(), "메세지를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    mMessage.requestFocus();
                    return;
                }
                // 받은 메세지 모든 유저에게 +1 처리해 주는 메소드
                addNumMessage(chatroomNo);
                // DB 저장 후 intent 전달 후 service 시작하는 메소드
                messageTimeCheck(chatroomNo, data);

            }
        });

        // Receiver 구현
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String getMessage = intent.getStringExtra(RECEIVE_DATA);
                Log.d(TAG, "브로드 캐스트 리시버 받은 메시지 : " + getMessage);

                // 상대방이 보낸 메시지
                try {
                    JSONObject jsonObject = new JSONObject(getMessage);

                    room_id = jsonObject.getString(ROOM_ID);
                    status = jsonObject.getString(ROOM_STATUS);
                    sender_uid = jsonObject.getString(USER_UID);
                    sender_name = jsonObject.getString(USER_NICKNAME);
                    sender_profile_image = jsonObject.getString(USER_IMAGE);
                    message_text = jsonObject.getString(MESSAGE_TEXT);
                    send_time = jsonObject.getString(SEND_TIME);
                    invite_user_name = jsonObject.getString(USER_INVITE); // 초대한 유저 닉네임

                    String[] timeSplit = send_time.split("_");
                    String date = timeSplit[0];
                    String time = timeSplit[1];

                    // 해당 방이 맞을 경우,
                    if (chatroomNo.equals(room_id)) {
                        // 초대된 사람이 있을 경우, 초대 메시지를 보여준다.
                        if(invite_user_name.length() > 1) {
                            String inviteMessage = sender_name + "님이 " + invite_user_name + "님을 초대했습니다.";
                            mList.add(new ChattingItem(null, null, sender_name, null, inviteMessage, START_ROOM));
                            saveMessage(START_ROOM, chatroomNo, inviteMessage, send_time);
                            // 보낸 유저와 현재 유저가 같은 경우 (자신인 경우)
                        } else if (!sender_uid.equals(userID)) {
                            if (status.equals(START_ROOM)) { // 처음 들어온 유저의 경우 모두에게 날짜를 보낸다.
                                    mList.add(new ChattingItem(null, null, sender_name, null, null, DATE));
                                    mList.add(new ChattingItem(time, null, sender_name, null, null, START_ROOM));

                            } else if (status.equals(EXIT_ROOM)) { // 유저가 나갈 경우 모두에게 메시지를 보낸다.
                                mList.add(new ChattingItem(time, null, sender_name, null, message_text, EXIT_ROOM));
                                saveMessage(EXIT_ROOM, chatroomNo, message_text, send_time);
                                getChatroomInfo(chatroomNo, userID);
                            } else if (status.equals(CHATTING) && message_text != null) { // 정상적인 메시지일 경우, (공지X)
                                mList.add(new ChattingItem(time, sender_uid, sender_name, sender_profile_image, message_text, CHATTING));
                            }
                            adapter = new ChattingAdapter(mList, ChattingActivity.this, userID);
                            mChatRecyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            focusCurrentMessage(); // 최신 메시지 보여주기
                        }
                    }

                    } catch(JSONException e){
                        e.printStackTrace();
                    }
            }
        };

        // Receiver 등록
        registerReceiver(mReceiver, intentfilter);

    }
    // =========================================================================================================
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatroomInfo(chatroomNo, userID); // 채팅 룸 정보 가져오기 (채팅방 이름, 유저 수 등)
        getChatMessage(chatroomNo);  // 채팅 메시지 및 룸 정보 가져오기
    }
    // =========================================================================================================

    @Override
    protected void onPause() {
        super.onPause();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                resetNumMessage(chatroomNo, userID); // 채팅방 안 읽은 메시지 리셋
            }
        },600);
    }

    // =========================================================================================================
    // 채팅룸 리스트 가져오기
    private void getChatroomInfo(String roomID, String userID){
        mServiceApi.getChatroomInfo(roomID, userID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                String notiChatroomName = result.getResult(); // 채팅룸 유저
                String notiChatroomUserCount = result.getMessage(); //채팅룸 유저 수

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    if (Integer.parseInt(notiChatroomUserCount) > 2) {
                        mRoomUserCount.setVisibility(View.VISIBLE);
                        mRoomUserCount.setText(notiChatroomUserCount);
                    } else {
                        mRoomUserCount.setVisibility(View.GONE);
                    }
                }
                mRoomName.setText(notiChatroomName);
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("getChatroomInfo 에러 발생", t.getMessage());
            }
        });
    }
    // =========================================================================================================
    private void getChatMessage(String roomID) {
        mList.clear();
        Call<List<ChattingDataItem>> call = mServiceApi.getChatMessage(userID, roomID);
        call.enqueue(new Callback<List<ChattingDataItem>>() {
            @Override
            public void onResponse(Call<List<ChattingDataItem>> call, Response<List<ChattingDataItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mDataList = response.body();
                    for(int i = 0; i < mDataList.size(); i++) {
                        String getMessage  = mDataList.get(i).getMessage();
                        String getTime  = mDataList.get(i).getTime();
                        String getStatus  = mDataList.get(i).getStatus();
                        String getUserId  = mDataList.get(i).getUser_id();
                        String getUserName = mDataList.get(i).getUser_name();
                        String getUserPhoto  = mDataList.get(i).getUser_photo();

                        String[] timeSplit = getTime.split("_");
                        String date = timeSplit[0];
                        String time = timeSplit[1];

                        if(getStatus.equals(DATE)) {
                            mList.add(new ChattingItem(date, getUserId, getUserName, getUserPhoto, getMessage, getStatus));
                        } else {
                            mList.add(new ChattingItem(time, getUserId, getUserName, getUserPhoto, getMessage, getStatus));
                        }
                    }
                    adapter = new ChattingAdapter(mList, ChattingActivity.this, userID);
                    mChatRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    // 가장 최근 메시지 보여주기
                    focusCurrentMessage();
                }
            }
            @Override
            public void onFailure(Call<List<ChattingDataItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });

    }

    // =========================================================================================================
    // 채팅 멤버 리스트 가져오기
    private void getChatMember(String roomID, String userID) {

        Call<List<ChatMemberItem>> call = mServiceApi.getChatMember(roomID, userID);
        call.enqueue(new Callback<List<ChatMemberItem>>() {
            @Override
            public void onResponse(Call<List<ChatMemberItem>> call, Response<List<ChatMemberItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mMemberList = response.body();
                    chatMemberAdapter = new ChatMemberAdapter(mMemberList, ChattingActivity.this, userID);
                    mChatMemberRecyclerView.setAdapter(chatMemberAdapter);
                    chatMemberAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ChatMemberItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 해당 룸의 마지막 메시지 날짜, 시간을 가져와서 현재와 비교 -> 날짜가 다를 경우, 날짜 공지 띄워주기
    private void messageTimeCheck(String roomID, String message) {

        String fullTimeNow = simpleDateFormat.format(new Date());

        String[] timeSplit = fullTimeNow.split("_");
        String date = timeSplit[0];
        String time = timeSplit[1];

        mServiceApi.checkCurrentTime(roomID, date).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Log.d("messageTimeCheck 처리 결과 : ", result.getMessage());
                // 해당 룸 마지막 메시지의 시간과 지금 보낸 메시지 시간이 같을 경우 -> 메시지만 추가
                if (result.getMessage().equals("0")) {
                    // 내가 보낸 메시지 RecyclerView 추가
                    if(message != null && !message.equals("Create new room")) {
                        mList.add(new ChattingItem(time, userID, userName, userPhoto, message, CHATTING));
                    }

                } else if (result.getMessage().equals("1"))  { // 다를 경우 -> 메시지, 날짜, 시간 추가
                    mList.add(new ChattingItem(date, userID, userName, userPhoto, message, DATE));
                    if(message != null && !message.equals("Create new room")) {
                        mList.add(new ChattingItem(time, userID, userName, userPhoto, message, CHATTING));
                    }
                    // 메시지 (날짜) DB 저장
                    saveMessage(DATE, roomID, message, fullTimeNow);
                }

                adapter = new ChattingAdapter(mList, ChattingActivity.this, userID);
                mChatRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                    focusCurrentMessage();

                // 전송 후 EditText 초기화
                mMessage.setText("");
                if(message != null) {
                    // 메시지 DB 저장
                    saveMessage(CHATTING, roomID, message, fullTimeNow);

                    /*
                     *
                     * 메세지를 서비스로 보내는 곳
                     *
                     * */

                    try {

                        Log.d(TAG, "**************************************************");
                        Log.d(TAG, "전송 버튼 클릭 시 메세지를 서비스로 날린다.");
                        Log.d(TAG, "**************************************************");

                        // 메세지를 서비스로 보내는 곳
                        JSONObject object = new JSONObject();
                        object.put(ROOM_ID, chatroomNo);
                        object.put(ROOM_STATUS, CHATTING);
                        object.put(USER_UID, userID);
                        object.put(USER_NICKNAME, userName);
                        object.put(USER_IMAGE, userPhoto);
                        object.put(MESSAGE_TEXT, data);
                        object.put(USER_INVITE, "0");
                        object.put(SEND_TIME, fullTimeNow);
                        String messageData = object.toString();

                        Log.d(TAG, messageData);

                        Intent intent = new Intent(ChattingActivity.this, ChattingService.class); // 액티비티 ㅡ> 서비스로 메세지 전달
                        intent.putExtra(CHAT_DATA, messageData);
                        startService(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("checkCurrentTime 에러 발생", t.getMessage());
            }
        });

    }

    // =========================================================================================================
    // DB 통신 후 채팅 메시지 업로드
    private void saveMessage(String status, String roomID, String message, String fullTimeNow) {

        mServiceApi.saveMessage(status, userID, roomID, message, fullTimeNow).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                if(result.getResult().equals("success")) {
                    Log.d("saveMessage : ", result.getMessage());
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("saveMessage 에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 방 번호를 보내고 해당 방에 있는 사람들의 메세지 수 +1 씩 해 준다.
    // 메세지를 보내는 순간 NumMessage 숫자를 +1 처리해 준다.
    @SuppressLint("SimpleDateFormat")
    private void addNumMessage(String roomID) {

            mServiceApi.addMessageNum(roomID).enqueue(new Callback<ResultModel>() {
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    // 정상적으로 네트워크 통신 완료
                    ResultModel result = response.body();
                }

                @Override
                public void onFailure(Call<ResultModel> call, Throwable t) {
                    Toast.makeText(ChattingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                    Log.e("에러 발생", t.getMessage());
                }
            });
    }

    // =========================================================================================================
    // 채팅방을 벗어나는 순간 ( back 키 클릭 시 ) 0으로 초기화한다.
        @SuppressLint("SimpleDateFormat")
        private void resetNumMessage(String roomID, String userID) {

            mServiceApi.resetMessageNum(roomID, userID).enqueue(new Callback<ResultModel>() {
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    // 정상적으로 네트워크 통신 완료
                    ResultModel result = response.body();
                }

                @Override
                public void onFailure(Call<ResultModel> call, Throwable t) {
                    Toast.makeText(ChattingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                    Log.e("에러 발생", t.getMessage());
                }
            });
        }

    // =========================================================================================================
    // 채팅방 나가기 클릭 -> DB 멤버 삭제 및 나가기 메시지 날리기
    private void removeChatMember(String roomID, String userID) {

        String exitDate = simpleDateFormat.format(new Date());

        mServiceApi.removeChatMember(roomID, userID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                // 성공적으로 DB 내 공지사항 삭제를 완료했을 경우 액티비티를 닫는다.
                if(result.getResult().equals("success")) {
                    try {

                        Log.d(TAG, "**************************************************");
                        Log.d(TAG, "채팅 나가기 -> ** 님이 나갔습니다. 보내기");
                        Log.d(TAG, "**************************************************");

                        String exitMessage = userName + "님이 나갔습니다.";

                        // 메세지를 서비스로 보내는 곳
                        JSONObject object = new JSONObject();
                        object.put(ROOM_STATUS, EXIT_ROOM);
                        object.put(ROOM_ID, roomID);
                        object.put(USER_UID, userID);
                        object.put(USER_NICKNAME, userName);
                        object.put(USER_IMAGE, userPhoto);
                        object.put(MESSAGE_TEXT, exitMessage);
                        object.put(USER_INVITE, "0");
                        object.put(SEND_TIME, exitDate);

                        String exitChatData = object.toString();

                        Intent intent = new Intent(ChattingActivity.this, ChattingService.class); // 액티비티 ㅡ> 서비스로 메세지 전달
                        intent.putExtra(CHAT_DATA, exitChatData);
                        startService(intent);
                        // 메시지 (날짜) DB 저장
                        mDrawerLayout.closeDrawers();
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 채팅 나가기 - 팝업 다이알로그
    public void alertDialog(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ChattingActivity.this);
        alert.setMessage("채팅방에서 나가겠습니까?");
        alert.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeChatMember(chatroomNo, userID);
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // 취소
            }
        });
        alert.create().show();
    }

    // ==========================================================================================================
    // 마지막 recyclerview에 포커싱 (최신 메시지 보여주기)
    public void focusCurrentMessage(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatRecyclerView.scrollToPosition(adapter.getItemCount()-1);
            }
        }, 120);
    }

    // =========================================================================================================
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver); // 브로드 캐스트 리시버 끊기
    }
}

