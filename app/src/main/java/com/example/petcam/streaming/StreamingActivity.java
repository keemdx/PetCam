package com.example.petcam.streaming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petcam.R;
import com.google.android.material.appbar.AppBarLayout;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import org.jetbrains.annotations.NotNull;

import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * @author Dohyun(Dani)
 * @since 2021/06/15
 * @comment 실시간 스트리밍 방송을 송출하기 위한 액티비티입니다.
**/

public class StreamingActivity extends AppCompatActivity implements ConnectCheckerRtmp, SurfaceHolder.Callback {

    // 스트리밍 관련 UI
    private RtmpCamera1 rtmpCamera1;
    private Animation mAnimation;
    private ImageView mMic, mCam;
    private TextView mCount;
    private ConstraintLayout mLayoutStreamingIcon;
    private LinearLayout mLayoutBeforeStreaming;
    private AppBarLayout mLayoutStreamingChat;
    private Button mStreamingStartButton;

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

                    handler.postDelayed(runnable, 2500); // 2.5초 후 방송 시작
                    countDownTimer();
                    countDownTimer.start(); // 카운트 다운 시작
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

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_finish).setOnClickListener(onClickListener);
        findViewById(R.id.iv_switch_camera).setOnClickListener(onClickListener);
        findViewById(R.id.iv_mic).setOnClickListener(onClickListener);
        findViewById(R.id.btn_start_streaming).setOnClickListener(onClickListener);

        // 스트리밍 관련 UI 선언
        mMic = (ImageView) findViewById(R.id.iv_mic);
        mCount = (TextView) findViewById(R.id.tv_count);
        mLayoutStreamingIcon = (ConstraintLayout) findViewById(R.id.layout_start_streaming);
        mLayoutStreamingChat = (AppBarLayout) findViewById(R.id.bottom_start_streaming);
        mStreamingStartButton = (Button) findViewById(R.id.btn_start_streaming);
        mLayoutBeforeStreaming = (LinearLayout) findViewById(R.id.layout_before_streaming);

        // 방송 화면을 그려주는 SurfaceView
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.view_surface);
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
                    rtmpCamera1.startStream("rtmp://15.164.220.155/live/hello"); // 라이브 스트리밍 고유 주소
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
                Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
                rtmpCamera1.stopStream();
                rtmpCamera1.stopPreview();
            }
        });
    }

    @Override
    public void onConnectionStartedRtmp(@NotNull String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Connection Started", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Connection Success", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();
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
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
            rtmpCamera1.stopPreview();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

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
                        try{
                            mAnimation = new AlphaAnimation(1.0f, 0.0f); // Fade out 애니메이션
                            mAnimation.setDuration(2000);
                            mCount.startAnimation(mAnimation);
                        }catch (Throwable t){

                        }
                        mCount.setVisibility(View.GONE);
                    }
                });
                startFadeOut.start();

                // 방송 전 UI 지우고, 라이브 방송 실행 시 UI 보여주기
                mStreamingStartButton.setVisibility(View.GONE);
                mLayoutStreamingIcon.setVisibility(View.VISIBLE);
                mLayoutStreamingChat.setVisibility(View.VISIBLE);

            }
        };
    }
}