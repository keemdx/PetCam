package com.example.petcam.streaming;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import static com.example.petcam.function.App.STREAMING_ROOM_ID;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * @author Dohyun(Dani)
 * @since 2021/06/26
 * @comment 녹화된 VOD 영상을 재생하기 위한 액티비티입니다. (ExoPlayer 사용)
**/

public class VODPlayerActivity extends AppCompatActivity {

    private static final String TAG = "VODPlayerActivity";

    private ServiceApi mServiceApi;
    private String roomID;

    // 영상 Play 관련 UI
    private PlayerView mPlayerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    // 채팅 관련
    private LiveChatAdapter adapter;
    private List<LiveChatItem> mVodChatList;
    private RecyclerView mVodChatRV;


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.iv_vod_finish: // 방송 나가기 버튼

                    finish(); // 액티비티 종료
                    break;

            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_player);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 리사이클러뷰에서 받은 룸 아이디 가져오기
        Intent intent = getIntent();
        roomID = intent.getStringExtra(STREAMING_ROOM_ID);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_vod_finish).setOnClickListener(onClickListener);

        // 채팅 관련 UI 선언
        mVodChatRV = (RecyclerView) findViewById(R.id.rv_vod_chat);
        mVodChatList = new ArrayList<>();
        mVodChatRV.setHasFixedSize(true);
        mVodChatRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new LiveChatAdapter(mVodChatList, VODPlayerActivity.this);
        mVodChatRV.setAdapter(adapter);

    }

    // =========================================================================================================

    private void initializePlayer() { // 비디오 플레이어 초기화

        // 영상 play 를 위한 player view 생성
        mPlayerView = (PlayerView) findViewById(R.id.view_vod_player);
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

        player.seekTo(1000); // vod 재생 시 1초부터 재생하기

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


    // ==========================================================================================================

    // 마지막 RecyclerView 에 포커싱 (최신 메시지 보여주기)
    public void focusCurrentMessage() {
        mVodChatRV.scrollToPosition(adapter.getItemCount() - 1);
    }
}