package com.example.petcam.streaming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.google.android.material.appbar.AppBarLayout;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.ACCESS_KEY;
import static com.example.petcam.function.App.BROADCAST_LIVE_MSG;
import static com.example.petcam.function.App.BUCKET_NAME;
import static com.example.petcam.function.App.CHAT_DATA;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.SECRET_KEY;
import static com.example.petcam.function.App.STREAMING_ROOM_ID;
import static com.example.petcam.function.App.STREAMING_URL;
import static com.example.petcam.function.App.TAG;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * @author Dohyun(Dani)
 * @since 2021/06/15
 * @comment 실시간 스트리밍 방송을 송출하기 위한 액티비티입니다.
**/

public class StreamingActivity extends AppCompatActivity implements ConnectCheckerRtmp, SurfaceHolder.Callback {

    // 스트리밍 관련 UI
    private SurfaceView mSurfaceView;
    private RtmpCamera1 rtmpCamera1;
    private Animation mAnimation;
    private ImageView mMic;
    private TextView mCount, mViewerCount;
    private ConstraintLayout mLayoutStreamingIcon;
    private LinearLayout mLayoutBeforeStreaming;
    private AppBarLayout mLayoutStreamingChat;
    private Button mStreamingStartButton;

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

    // 방 정보 관련
    private String roomID, userID, userPhoto, userName;
    private EditText mRoomTitle;

    // 방 정보 관련 (Status)
    public static String ON = "ON";
    public static String OFF = "OFF";

    // 방송 녹화 관련 (LIVE TO VOD)
    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VOD"); // VOD 저장할 경로 설정
    private String timeStamp;

    private ServiceApi mServiceApi;
    private SharedPreferences pref;

    // 방송 시간 체크를 위한 Chronometer
    Chronometer chronometer;
    String liveTime;

    // 방송 시작 전 카운트 다운을 위한 용도
    int count = 3;
    private CountDownTimer countDownTimer;
    private static final int MILLISINFUTURE = 4 * 1000; // 카운트를 위한 총 시간 (4초)
    private static final int COUNT_DOWN_INTERVAL = 1000; // onTick()에 대한 시간 (1초)


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint({"UseCompatLoadingForDrawables", "NonConstantResourceId"})
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.btn_start_streaming: // 라이브 방송 시작 버튼

                    createStreamingRoom(userID);// 방 생성 및 DB 저장 후 방송 시작
                    break;

                case R.id.iv_finish: // 라이브 방송 종료 버튼

                    if (rtmpCamera1.isStreaming()) {
                        alertDialog(view); // 방송 종료 확인 다이알로그를 보여준다. -> 확인 시 방송 종료
                    } else {
                        finish();
                    }
                    break;

                case R.id.iv_mic: // 마이크 제어 버튼 (On, Off)

                    try {
                        if (!rtmpCamera1.isAudioMuted()) {
                            mMic.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_streaming_mic_off));
                            rtmpCamera1.disableAudio();
                        } else {
                            mMic.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_streaming_mic));
                            rtmpCamera1.enableAudio();
                        }
                    } catch (final CameraOpenException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                    break;

                case R.id.iv_switch_camera: // 카메라 앞, 뒤 전환 버튼

                    try {
                        rtmpCamera1.switchCamera();
                    } catch (final CameraOpenException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
        setContentView(R.layout.activity_streaming);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 액티비티 시작후 키보드 감추기
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 키보드 관련
        input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 브로드 캐스트 관련
        intentfilter = new IntentFilter(); // 인텐트 필터 생성
        intentfilter.addAction(BROADCAST_LIVE_MSG); // 인텐트 필터에 액션 추가

        // 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 아이디
        userPhoto = pref.getString(USER_IMAGE,""); // 유저 프로필 이미지
        userName = pref.getString(USER_NAME,""); // 유저 닉네임

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_finish).setOnClickListener(onClickListener);
        findViewById(R.id.iv_switch_camera).setOnClickListener(onClickListener);
        findViewById(R.id.iv_mic).setOnClickListener(onClickListener);
        findViewById(R.id.btn_start_streaming).setOnClickListener(onClickListener);
        findViewById(R.id.tv_send_chat).setOnClickListener(onClickListener);

        // 스트리밍 관련 UI 선언
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        mMic = (ImageView) findViewById(R.id.iv_mic);
        mViewerCount = (TextView) findViewById(R.id.tv_viewer_count);
        mCount = (TextView) findViewById(R.id.tv_count);
        mLayoutStreamingIcon = (ConstraintLayout) findViewById(R.id.layout_start_streaming);
        mLayoutStreamingChat = (AppBarLayout) findViewById(R.id.bottom_start_streaming);
        mStreamingStartButton = (Button) findViewById(R.id.btn_start_streaming);
        mLayoutBeforeStreaming = (LinearLayout) findViewById(R.id.layout_before_streaming);
        mRoomTitle = (EditText) findViewById(R.id.et_title); // 스트리밍 제목

        // 방송 화면을 그려주는 SurfaceView
        mSurfaceView = (SurfaceView) findViewById(R.id.view_surface);
        mSurfaceView.setBackgroundColor(Color.TRANSPARENT);
        mSurfaceView.getHolder().addCallback(this);
        rtmpCamera1 = new RtmpCamera1(mSurfaceView, this);

        // 채팅 관련 UI 선언
        mLiveChatRV = (RecyclerView) findViewById(R.id.rv_live_chat_streamer);
        mEditMessage = (EditText) findViewById(R.id.et_send_message);
        mSend = (TextView) findViewById(R.id.tv_send_chat);
        mEditMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(mEditMessage.isFocused()) {
                    if(s.length() > 0) { // 메시지 전송 가능한 상태
                        mSend.setText("Send");
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
        adapter = new LiveChatAdapter(mLiveChatList, StreamingActivity.this);
        mLiveChatRV.setAdapter(adapter);

    }
    // =========================================================================================================

    // 라이브 스트리밍 시작 핸들러
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (!rtmpCamera1.isStreaming()) { // 현재 스트리밍 중이 아니라면, 실행시킨다.

                if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) { // 비디오, 오디오 값이 null 이 아니라면 스트리밍을 시작한다.
                    rtmpCamera1.startStream("rtmp://15.164.220.155/live/" + roomID); // 라이브 스트리밍 고유 주소
                    try {
                        Thread.sleep(3000);
                        startRecord();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                }
            } else {
                rtmpCamera1.stopStream();
                rtmpCamera1.stopPreview();
            }

        }

    };
    // =========================================================================================================
    // [RTMP] RTMP 서버와의 연결을 확인한다.

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Auth Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Auth Success", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(@NotNull String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(ACTIVITY_SERVICE, "RTMP Connection Failed");
                rtmpCamera1.stopStream();
                rtmpCamera1.stopPreview();
                stopRecord();
            }
        });
    }

    @Override
    public void onConnectionStartedRtmp(@NotNull String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(ACTIVITY_SERVICE, "RTMP Connection Started");
            }
        });
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(ACTIVITY_SERVICE, "RTMP Connection Success");
                // RTMP 커넥션 연결에 성공하면 1분에 한번씩 썸네일을 저장해 준다. (실시간 반영)
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
            }
        });
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(ACTIVITY_SERVICE, "Disconnected");

                if (rtmpCamera1.isRecording()) { // 방송 중이라면, 종료한다.
                    stopRecord(); // 녹화 종료
                }
                saveRoomStatus(roomID);
            }
        });
    }

    @Override
    public void onNewBitrateRtmp(long l) {

    }

    // =========================================================================================================

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopRecord();

        if (rtmpCamera1.isStreaming()) { // 방송 중이라면, 종료한다.
            rtmpCamera1.stopStream();
            rtmpCamera1.stopPreview();
            saveRoomStatus(roomID);
        }
    }
    // =========================================================================================================

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) { //  SurfaceView 생성 시 호출한다.
        rtmpCamera1.startPreview(); // 카메라 프리뷰 화면을 보여준다. [방송 시작 전]
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        rtmpCamera1.stopPreview();
    }

    // =========================================================================================================

    // 라이브 방송 시간을 1초 간격으로 전송한다. (스트리머 -> 서버 -> 클라이언트)
    // 녹화된 VOD 영상과 실시간 채팅의 sync 를 위해 필요하다.
    private void sendTimeToClient() {

        // 방송 시작 시 타이머를 시작한다.
        chronometer.setBase(SystemClock.elapsedRealtime()); // 초기화
        chronometer.start(); // Chronometer 시작 -> 틱마다 이벤트를 발생시킨다.

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                try {
                    liveTime = (String) chronometer.getText(); // 현재 방송 시간 가져오기
                    liveTime = liveTime.replaceAll(":",""); // 0001, 0002 ...

                    JSONObject object = new JSONObject();

                    object.put("type", "time");
                    object.put("liveTime", liveTime);
                    object.put("room_id", roomID);

                    String data = object.toString();

                    Intent intent = new Intent(StreamingActivity.this, LiveChatService.class); // 액티비티 ㅡ> 서비스로 메세지 전달
                    intent.putExtra(CHAT_DATA, data);
                    startService(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // =========================================================================================================

    // 녹화를 시작한다. (VOD)
    public void startRecord() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!rtmpCamera1.isRecording()) { // 레코딩 중이 아니라면, 폴더 생성해서 vod를 저장한다.
                try {
                    // 폴더가 없다면 생성한다.
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yy" + "MM" + "dd" + "HH" + "mm" + "ss");
                    timeStamp = format.format(new Date());
                    rtmpCamera1.startRecord(folder.getAbsolutePath() + "/" + timeStamp + ".mp4");

                } catch (IOException e) {
                    rtmpCamera1.stopRecord();
                    Log.d(ACTIVITY_SERVICE, "Stop Recording");
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else { // 레코딩 중이라면 녹화를 중단한다.
                stopRecord();
            }
        } else {
            Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecord() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord(); // 녹화를 중단한다.
            timeStamp = "";
        }
    }

    // =========================================================================================================

    // 방송 종료 시 한번 더 확인하는 용도의 다이알로그
    public void alertDialog(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(StreamingActivity.this);
        alert.setMessage("Are you sure you want to end your live video?");
        alert.setPositiveButton("END", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (rtmpCamera1.isStreaming()) { // 방송 중이라면, 종료한다.
                    screenShot(); // 현재 화면 스크린샷!
                    rtmpCamera1.stopStream();
                    rtmpCamera1.stopPreview();
                }
                saveRoomStatus(roomID); // 룸 상태 바꾸고 정보 넘기기

                try {

                    JSONObject object = new JSONObject();
                    object.put("type", "liveOff");
                    object.put("room_id", roomID);
                    String data = object.toString();

                    Intent finishIntent = new Intent(StreamingActivity.this, LiveChatService.class); // 액티비티 ㅡ> 서비스로 메세지 전달
                    finishIntent.putExtra(CHAT_DATA, data);
                    startService(finishIntent);
                    Log.d(TAG, data);

                    // 방송이 끝난 후 정보 넘기기
                    Intent intent = new Intent(getApplicationContext(), StreamingResultActivity.class);
                    intent.putExtra(STREAMING_ROOM_ID, roomID); // 방 번호
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // 취소
            }
        });
        alert.create().show();
    }

    // =========================================================================================================

    // 방송 종료 : DB 와 연결해서 roomStatus 를 (ON -> OFF)로 바꾼다.

    @SuppressLint("SimpleDateFormat")
    private void saveRoomStatus(String roomID) {

        mServiceApi.saveRoomStatus(roomID, "OFF").enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(StreamingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // 생성된 방 정보 DB 저장 후 스트리밍 시작!
    private void createStreamingRoom(String userID) {

        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yy" + "MM" + "dd" + "HH" + "mm" + "ss");
        String today = format.format(date);

        roomID = userID + today; // 룸 ID 생성
        String streamerID = userID; // 스트리머 UID
        String roomTitle = mRoomTitle.getText().toString(); // 룸 생성 시 입력한 타이틀
        String roomStatus = ON; // 현재 방송 상태
        String createAt =  String.valueOf(System.currentTimeMillis());
        int viewer = 0; // 시청자 수 (1 이상일 경우만 표시)

        mServiceApi.createStreamingRoom(roomID, streamerID, roomTitle, roomStatus, viewer, createAt).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();

                if (result.getResult().equals("success")) { // DB 저장 정상적으로 되었다면, 방송 시작!
                    handler.postDelayed(runnable, 3000); // 3초 후 방송 시작
                    countDownTimer();
                    countDownTimer.start(); // 카운트 다운 시작
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // 방송 전 카운트 다운 진행 (3, 2, 1, Start!)
    public void countDownTimer() {
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long l) {

                mLayoutBeforeStreaming.setVisibility(View.GONE);
                mCount.setVisibility(View.VISIBLE);

                if (count == 3) {
                    mCount.setText("3");
                    count--;

                } else if (count == 2) {
                    mCount.setText("2");
                    count--;

                } else if (count == 1) {
                    mCount.setText("1");

                }
            }

            @Override
            public void onFinish() { // 카운트 다운이 끝난 후 실행한다.

                mCount.setText("Start!");

                Thread startFadeOut = new Thread(new Runnable() { // 카운트 다운이 끝나면, Start! 표시와 함께 Fade out 시킨다.
                    public void run() {
                        try {
                            mAnimation = new AlphaAnimation(1.0f, 0.0f); // Fade out 애니메이션
                            mAnimation.setDuration(2000);
                            mCount.startAnimation(mAnimation);
                        } catch (Throwable t) {

                        }
                        if(mCount.getVisibility() == View.VISIBLE) {
                            mCount.setVisibility(View.GONE);
                        }
                    }
                });
                startFadeOut.start();

                // 방송 전 UI 지우고, 라이브 방송 실행 시 UI 보여주기
                mStreamingStartButton.setVisibility(View.GONE);
                mLayoutStreamingIcon.setVisibility(View.VISIBLE);
                mLayoutStreamingChat.setVisibility(View.VISIBLE);
                mLiveChatRV.setVisibility(View.VISIBLE);

                if (rtmpCamera1.isStreaming()) {
                    screenShot(); // 현재 화면 스크린샷!
                }

                // Service 시작! (background)
                Intent intent = new Intent(StreamingActivity.this, LiveChatService.class);
                startService(intent);

                broadcastReceiver(); // 리시버 등록하는 함수 작동
                sendTimeToClient(); // 클라이언트로 방송 시간 보내는 함수 작동
            }
        };
    }

    // =========================================================================================================

    // Thumbnail 을 위해 SurfaceView 스크린샷 찍어서 저장하기

    public void screenShot(){ // SurfaceView 스크린샷 찍기

        // SurfaceView 를 BitMap 으로 복사한다. (PixelCopy 사용)
        Bitmap bitmap = Bitmap.createBitmap(mSurfaceView.getWidth(), mSurfaceView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mSurfaceView.draw(canvas);

        // 이미지 처리를 오프로드하는 핸들러 스레드를 만듭니다.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(mSurfaceView, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) { // 복사가 성공적이라면, 파일을 png 파일로 변환해서 db로 저장한다.
                    String fileName = "image_screenshot_" + System.currentTimeMillis();
                    File file = new File(Environment.getExternalStorageDirectory(), fileName);
                    FileOutputStream os = null;
                    try{
                        os = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);   // 비트맵을 PNG 파일로 변환한다.
                        os.close();
                        uploadImageFile(file, fileName); // 이미지 파일 업로드
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                } else { // 복사 실패 시
                    Toast toast = Toast.makeText(getApplication(),
                            "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                    toast.show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        }
    }

    // AWS s3 스토리지에 캡쳐된 이미지 업로드
    public void uploadImageFile(File file, String fileName) {

        // AWS s3 스토리지 설정
        AWSCredentials awsCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(StreamingActivity.this).build();
        TransferNetworkLossHandler.getInstance(StreamingActivity.this);

        // streaming 경로에 이미지 업로드
        TransferObserver uploadObserver = transferUtility.upload(BUCKET_NAME + "/streaming", fileName, file);
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "onStateChanged: " + id + ", " + state.toString());
                String streamingThumbnail = STREAMING_URL + fileName; // 스토리지에 저장 후 수정된 이미지 url
                saveThumbnail(streamingThumbnail , roomID); // 찍은 스크린샷 저장 (리사이클러뷰에 뿌려주기 위함)
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;
                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        });
    }

    // DB 유저 프로필 수정 요청 (서버 통신)
    @SuppressLint("SimpleDateFormat")
    private void saveThumbnail(String uri, String roomID) {

        mServiceApi.saveThumbnail(uri, roomID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Log.d(ACTIVITY_SERVICE, "Thumbnail 저장 성공 : " + result.getMessage());
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(StreamingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // 메시지를 보내는 곳
    private void sendMessage() {
        String message = mEditMessage.getText().toString();

        mEditMessage.setText("");
        input.hideSoftInputFromWindow(mEditMessage.getWindowToken(), 0);

        // 채팅 RecyclerView 에 내가 보낸 메시지 추가
        mLiveChatList.add(new LiveChatItem(userID, userName, userPhoto, message));
        adapter = new LiveChatAdapter(mLiveChatList, StreamingActivity.this);
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

            Intent intent = new Intent(StreamingActivity.this, LiveChatService.class); // 액티비티 ㅡ> 서비스로 메세지 전달
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
                if(result.getResult().equals("success")) {
                    Log.d(ACTIVITY_SERVICE, "채팅 저장 성공 : " + result.getMessage());
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(StreamingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // 동적 리시버 생성 -> 브로드 캐스트 메시지를 받아온다.
    private void broadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                String room_id = intent.getStringExtra("room_id");

                if(room_id.equals(roomID)) {
                    // 일반적인 메세지를 받았을 때 실행한다.
                    if (type.equals("message")) {
                        Log.d(TAG, "message 작동");

                        String sender_id = String.valueOf(intent.getIntExtra("id", 1));
                        String sender_name = intent.getStringExtra("name");
                        String sender_profile = intent.getStringExtra("profile");
                        String sender_message = intent.getStringExtra("message");

                        // 받아온 메시지 데이터를 RecyclerView 에 추가한다.
                        mLiveChatList.add(new LiveChatItem(sender_id, sender_name, sender_profile, sender_message));
                        adapter = new LiveChatAdapter(mLiveChatList, StreamingActivity.this);
                        mLiveChatRV.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        focusCurrentMessage(); // 최신 메시지 보여주기

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

    // 서버에서 현재 viewer 수 가져오기
    @SuppressLint("SimpleDateFormat")
    private void getViewerCount(String roomID) {

        mServiceApi.getViewerCount(roomID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                mViewerCount.setText(result.getMessage());

            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(StreamingActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
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