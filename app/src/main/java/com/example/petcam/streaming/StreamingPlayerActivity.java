package com.example.petcam.streaming;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.STREAMING_ROOM_ID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * @author Dohyun(Dani)
 * @since 2021/06/15
 * @comment 실시간 스트리밍 영상을 재생하기 위한 액티비티입니다. (ExoPlayer 사용)
**/

public class StreamingPlayerActivity extends AppCompatActivity {

    private ServiceApi mServiceApi;

    // 영상 Play 관련 UI
    private PlayerView mPlayerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    // 스트리밍 방 관련
    private int viewer;
    private String roomID;
    private TextView mViewerCount;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.iv_player_finish: // 방송 나가기 버튼

                    finish(); // 액티비티 종료
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_player);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_player_finish).setOnClickListener(onClickListener);

        mViewerCount = (TextView) findViewById(R.id.tv_play_user_count);

        // 리사이클러뷰에서 받은 룸 아이디 가져오기
        Intent intent = getIntent();
        roomID = intent.getStringExtra(STREAMING_ROOM_ID);

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
        // 유저 수 -1 해서 저장하기
        setViewerCount("END", roomID);
    }

    // =========================================================================================================

    // 서버에 viewer 수 저장하기
    @SuppressLint("SimpleDateFormat")
    private void setViewerCount(String viewerStatus, String roomID) {

        mServiceApi.setViewerCount(viewerStatus, roomID).enqueue(new Callback<ResultModel>() {
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
}