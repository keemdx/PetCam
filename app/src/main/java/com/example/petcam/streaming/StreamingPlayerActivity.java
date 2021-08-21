package com.example.petcam.streaming;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.BROADCAST_LIVE_MSG;
import static com.example.petcam.function.App.CHAT_DATA;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.STREAMING_ROOM_ID;
import static com.example.petcam.function.App.TAG;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * @author Dohyun(Dani)
 * @comment 실시간 스트리밍 영상을 재생하기 위한 액티비티입니다. (ExoPlayer 사용)
 * @since 2021/06/15
 **/

public class StreamingPlayerActivity extends AppCompatActivity {

    private static final String TAG = "StreamingPlayerActivity";

    private ServiceApi mServiceApi;
    private SharedPreferences pref;

    // 영상 Play 관련 UI
    private PlayerView mPlayerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    // 스트리밍 방 관련
    private int viewer;
    private String roomID, userID, userPhoto, userName, liveTime;
    private TextView mViewerCount;

    // 채팅 관련
    private InputMethodManager input;
    private LiveChatAdapter adapter;
    private List<LiveChatItem> mLiveChatList;
    private RecyclerView mLiveChatRV;
    private EditText mEditMessage;
    private TextView mSend;

    // 브로드 캐스트 리시버 동적 생성(매니페스트 intent filter 추가 안하고)
    BroadcastReceiver broadcastReceiver; // 서비스로부터 메세지를 받기 위해 브로드 캐스트 리시버 동적 생성
    IntentFilter intentfilter;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.iv_player_finish: // 방송 나가기 버튼

                    // 유저 수 -1 해서 저장하기
                    setViewerCount("END", roomID);
                    finish(); // 액티비티 종료
                    break;

                case R.id.tv_send_chat: // 채팅 보내기 버튼

                    sendMessage();
                    break;
            }
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_player);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 키보드 관련
        input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 아이디
        userPhoto = pref.getString(USER_IMAGE, ""); // 유저 프로필 이미지
        userName = pref.getString(USER_NAME, ""); // 유저 닉네임

        // 리사이클러뷰에서 받은 룸 아이디 가져오기
        Intent intent = getIntent();
        roomID = intent.getStringExtra(STREAMING_ROOM_ID);

        // 브로드 캐스트 관련
        intentfilter = new IntentFilter(); // 인텐트 필터 생성
        intentfilter.addAction(BROADCAST_LIVE_MSG); // 인텐트 필터에 액션 추가
        broadcastReceiver(); // 리시버 등록하는 함수 작동

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_player_finish).setOnClickListener(onClickListener);
        findViewById(R.id.tv_send_chat).setOnClickListener(onClickListener);

        mViewerCount = (TextView) findViewById(R.id.tv_play_user_count);

        // 채팅 관련 UI 선언
        mLiveChatRV = (RecyclerView) findViewById(R.id.rv_live_chat_streamer);
        mEditMessage = (EditText) findViewById(R.id.et_send_message);
        mSend = (TextView) findViewById(R.id.tv_send_chat);
        mEditMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (mEditMessage.isFocused()) {
                    if (s.length() > 0) { // 메시지 전송 가능한 상태
                        mSend.setText("전송");
                        mSend.setClickable(true);
                    } else {
                        mSend.setText("···");
                        mSend.setClickable(false);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // EditText 부분을 클릭했을 경우 클릭 이벤트
        mEditMessage.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // 가장 최근 메시지 보여주기
                focusCurrentMessage();
                return false;
            }
        });

        // 채팅을 위한 RecyclerView
        mLiveChatList = new ArrayList<>();
        mLiveChatRV.setHasFixedSize(true);
        mLiveChatRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new LiveChatAdapter(mLiveChatList, StreamingPlayerActivity.this);
        mLiveChatRV.setAdapter(adapter);

        // 현재 방 유저 수 가져오기
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(1000); // 1분에 한번씩 썸네일 변경해 주기
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getViewerCount(roomID);
                            }
                        });
                    } catch (InterruptedException e) {
                        // error
                    }
            }
        }).start();

        // 유저 수 +1 해서 저장하기
        setViewerCount("START", roomID);

        // Service 시작! (background)
        Intent serviceIntent = new Intent(StreamingPlayerActivity.this, LiveChatService.class);
        startService(serviceIntent);

    }

    // =========================================================================================================

    // 동적 리시버 생성 -> 브로드 캐스트 메시지를 받아온다.
    private void broadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String type = intent.getStringExtra("type");
                String room_id = intent.getStringExtra("room_id");

                if (room_id.equals(roomID)) {
                    // 일반적인 메세지를 받았을 때 실행한다.
                    if (type.equals("message")) {
                        Log.d(TAG, "message 작동");

                        String sender_id = String.valueOf(intent.getIntExtra("id", 1));
                        String sender_name = intent.getStringExtra("name");
                        String sender_profile = intent.getStringExtra("profile");
                        String sender_message = intent.getStringExtra("message");

                        // 받아온 메시지 데이터를 RecyclerView 에 추가한다.
                        mLiveChatList.add(new LiveChatItem(sender_id, sender_name, sender_profile, sender_message));
                        adapter = new LiveChatAdapter(mLiveChatList, StreamingPlayerActivity.this);
                        mLiveChatRV.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        focusCurrentMessage(); // 최신 메시지 보여주기

                        // 라이브 스트리밍 종료 시 실행한다. (종료 페이지)
                    } else if (type.equals("liveOff")) {
                        Intent finishIntent = new Intent(getApplicationContext(), StreamingFinishActivity.class);
                        finishIntent.putExtra(STREAMING_ROOM_ID, roomID);
                        startActivity(finishIntent);
                        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                        finish();
                    } else if (type.equals("time")) { // 서버로부터 방송 시간 받기
                        liveTime = intent.getStringExtra("liveTime");
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentfilter);
        Log.d(TAG, "broadcast receiver 를 시작합니다.");
    }

    // =========================================================================================================

    // 브로드 캐스트 종료
    private void broadcastReceiverEnd() {
        if (broadcastReceiver != null) {
            this.unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
            Log.d(TAG, "broadcast receiver 를 종료합니다.");
        }
    }
    // =========================================================================================================

    // 메시지를 보내는 곳
    private void sendMessage() {
        String message = mEditMessage.getText().toString();

        mEditMessage.setText("");
        input.hideSoftInputFromWindow(mEditMessage.getWindowToken(), 0);

        // 채팅 RecyclerView 에 내가 보낸 메시지 추가
        mLiveChatList.add(new LiveChatItem(userID, userName, userPhoto, message));
        adapter = new LiveChatAdapter(mLiveChatList, StreamingPlayerActivity.this);
        mLiveChatRV.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        focusCurrentMessage(); // 최신 메시지 보여주기

        // 서버에 메시지를 보낸다. (JSON)
        try {
            JSONObject object = new JSONObject();
            object.put("type", "message");
            object.put("room_id", roomID);
            object.put("id", userID);
            object.put("name", userName);
            object.put("profile", userPhoto);
            object.put("message", message);

            String msg_data = object.toString(); // 보낼 데이터 (메시지 정보)

            Log.d(TAG, "MESSAGE : " + message);
            Log.d(TAG, "ROOM ID : " + roomID);
            Log.d(TAG, "ID : " + userID);
            Log.d(TAG, "NAME : " + userName);
            Log.d(TAG, "IMG : " + userPhoto);

            Intent intent = new Intent(StreamingPlayerActivity.this, LiveChatService.class); // 액티비티 ㅡ> 서비스로 메세지 전달
            intent.putExtra(CHAT_DATA, msg_data);
            startService(intent);

            saveLiveChat(roomID, userID, message); // 채팅 내용 DB 저장

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================================================

    // 채팅 내용 저장 (DB)
    @SuppressLint("SimpleDateFormat")
    private void saveLiveChat(String roomID, String userID, String message) {

        long now = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
        String sendTime = simpleDateFormat.format(new Date(now));

        mServiceApi.saveLiveChat(roomID, userID, message, sendTime, liveTime).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Log.d(ACTIVITY_SERVICE, "채팅 저장 성공 : " + result.getMessage());
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(StreamingPlayerActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    private void initializePlayer() { // 비디오 플레이어 초기화

        // 영상 play 를 위한 player view 생성
        mPlayerView = (PlayerView) findViewById(R.id.view_streaming_player);
        mPlayerView.requestFocus();

        Uri dashUri = Uri.parse("http://15.164.220.155/stream/dash/" + roomID + ".mpd");

        // Create a data source factory.
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(); // HttpDataSource 는 인터넷 상의 mp3, mp4 파일 재생을 도와준다.

        // Create a DASH media source pointing to a DASH manifest uri.
        MediaSource mediaSource =
                new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(dashUri)); // 미디어 소스 초기화

        // Create a player instance.
        player = new SimpleExoPlayer.Builder(getApplicationContext()).build(); // 플레이어 연결
        mPlayerView.setPlayer(player);
        player.setPlayWhenReady(true);

        // Set the media source to be played.
        player.setMediaSource(mediaSource);
        // Prepare the player.
        player.prepare();
    }

    // =========================================================================================================

    private void releasePlayer() { // 비디오 플레이어 해제 (재생 중지)
        if (player != null) {
            player.getPlayWhenReady();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    // =========================================================================================================

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
        // 브로드캐스트 종료하기
        broadcastReceiverEnd();
    }

    // =========================================================================================================

    // 서버에 viewer 수 저장하기
    @SuppressLint("SimpleDateFormat")
    private void setViewerCount(String viewerStatus, String roomID) {

        mServiceApi.setViewerCount(viewerStatus, roomID, userID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(StreamingPlayerActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }


    // =========================================================================================================

    // 서버에서 현재 viewer 수 가져오기
    @SuppressLint("SimpleDateFormat")
    private void getViewerCount(String roomID) {

        mServiceApi.getViewerCount(roomID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                viewer = Integer.parseInt(result.getMessage());
                mViewerCount.setText(result.getMessage()); // 뷰어 수 설정
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(StreamingPlayerActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // ==========================================================================================================

    // 마지막 RecyclerView 에 포커싱 (최신 메시지 보여주기)
    public void focusCurrentMessage() {
        mLiveChatRV.scrollToPosition(adapter.getItemCount() - 1);
    }
}