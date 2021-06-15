package com.example.petcam.streaming;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.petcam.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * @author Dohyun(Dani)
 * @since 2021/06/15
 * @comment 실시간 스트리밍 영상을 재생하기 위한 액티비티입니다. (ExoPlayer 사용)
**/

public class StreamingPlayerActivity extends AppCompatActivity {

    // 영상 Play 관련 UI
    private PlayerView mPlayerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

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

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_player_finish).setOnClickListener(onClickListener);

    }

    // =========================================================================================================

    private void initializePlayer() { // 비디오 플레이어 초기화

        // 영상 play 를 위한 player view 생성
        mPlayerView = (PlayerView) findViewById(R.id.view_streaming_player);
        mPlayerView.requestFocus();

        Uri dashUri = Uri.parse("http://15.164.220.155/stream/dash/hello.mpd");

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
    }

    // =========================================================================================================
}