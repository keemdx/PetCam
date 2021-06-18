package com.example.petcam.streaming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
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
import com.example.petcam.function.App;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.google.android.material.appbar.AppBarLayout;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.ACCESS_KEY;
import static com.example.petcam.function.App.BUCKET_NAME;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.SECRET_KEY;
import static com.example.petcam.function.App.STREAMING_URL;
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

    // 방 정보 관련
    private String roomID, userID;
    private EditText mRoomTitle;

    // 방 정보 관련 (Status)
    public static String ON = "ON";
    public static String OFF = "OFF";

    // 방송 녹화 관련 (LIVE TO VOD)
    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VOD"); // VOD 저장할 경로 설정
    private String timeStamp;

    private ServiceApi mServiceApi;
    private SharedPreferences pref;

    // 방송 시작 전 카운트 다운을 위한 용도
    int count = 3;
    private CountDownTimer countDownTimer;
    private static final int MILLISINFUTURE = 4 * 1000; // 카운트를 위한 총 시간 (4초)
    private static final int COUNT_DOWN_INTERVAL = 1000; // onTick()에 대한 시간 (1초)


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("UseCompatLoadingForDrawables")
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

            }
        }
    };

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

        // 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 아이디

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_finish).setOnClickListener(onClickListener);
        findViewById(R.id.iv_switch_camera).setOnClickListener(onClickListener);
        findViewById(R.id.iv_mic).setOnClickListener(onClickListener);
        findViewById(R.id.btn_start_streaming).setOnClickListener(onClickListener);

        // 스트리밍 관련 UI 선언
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
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();

                stopRecord();

                if (rtmpCamera1.isStreaming()) { // 방송 중이라면, 종료한다.
                    rtmpCamera1.stopStream();
                    rtmpCamera1.stopPreview();
                }
                saveRoomStatus(roomID);
                finish(); // 액티비티 종료
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
                    rtmpCamera1.startRecord(folder.getAbsolutePath() + "/" + format + ".mp4");

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
        alert.setMessage("라이브 방송을 끝내시겠어요?");
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (rtmpCamera1.isStreaming()) { // 방송 중이라면, 종료한다.
                    rtmpCamera1.stopStream();
                    rtmpCamera1.stopPreview();
                }
                saveRoomStatus(roomID); // 룸 상태 바꿔주기
                finish(); // 액티비티 종료
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
                        mCount.setVisibility(View.GONE);
                    }
                });
                startFadeOut.start();

                // 방송 전 UI 지우고, 라이브 방송 실행 시 UI 보여주기
                mStreamingStartButton.setVisibility(View.GONE);
                mLayoutStreamingIcon.setVisibility(View.VISIBLE);
                mLayoutStreamingChat.setVisibility(View.VISIBLE);

                if (rtmpCamera1.isStreaming()) {
                    screenShot(); // 현재 화면 스크린샷!
                }
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
                Log.d(App.TAG, "onStateChanged: " + id + ", " + state.toString());
                String streamingThumbnail = STREAMING_URL + fileName; // 스토리지에 저장 후 수정된 이미지 url
                saveThumbnail(streamingThumbnail , roomID); // 찍은 스크린샷 저장 (리사이클러뷰에 뿌려주기 위함)
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;
                Log.d(App.TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(App.TAG, ex.getMessage());
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




}