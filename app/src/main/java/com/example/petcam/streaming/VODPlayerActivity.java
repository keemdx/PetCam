package com.example.petcam.streaming;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcam.R;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.STREAMING_ROOM_ID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * @author Dohyun(Dani)
 * @since 2021/06/26
 * @comment 녹화된 VOD 영상을 재생하기 위한 액티비티입니다. (ExoPlayer 사용)
 *
**/

public class VODPlayerActivity extends AppCompatActivity {

    private static final String TAG = "VODPlayerActivity";

    private ServiceApi mServiceApi;
    private String roomID;

    // 영상 Play 관련 UI
    public boolean isPlay;
    public SimpleExoPlayer player;
    private PlayerView mPlayerView;
    private DefaultTrackSelector trackSelector;
    private boolean autoPlay;

    // 채팅 관련
    private Handler mHandler;
    private LiveChatAdapter adapter;
    private List<LiveChatItem> mDataList, mVodChatList;
    private RecyclerView mVodChatRV;

    // 채팅 시간, 채팅 데이터를 담아둘 HashMap
    HashMap<Integer, LiveChatItem> hashMap = new HashMap<Integer, LiveChatItem>();
    SendMessageHandler messageHandler; // 재생 시간에 채팅 데이터가 존재할 경우 UI 변경을 위해 handler 생성
    int playTime = 0; // 현재 재생 중인지 아닌지 구분하기 위한 변수

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

        mHandler = new Handler();
        messageHandler = new SendMessageHandler(); // 메인 핸들러 설정

        initializePlayer(); // 영상 재생
        isPlay = true;

        getLiveChat(roomID); // 해당 룸에 해당하는 채팅 데이터 가져오기

        // 핸들러로 전달할 runnable 객체
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                // 현재 재생 중인 영상의 재생 시간을 가져오는 곳 (1초마다 재생 시간을 가져오게 구현)
                if (player != null) {
                    Log.d(TAG, "VOD 시청 중");

                    int currentTime = (int) player.getCurrentPosition() / 1000;
                    Log.d(TAG, "방송 시간 : " + currentTime);

                    if (currentTime != 0) {
                        messageHandler.sendEmptyMessage(currentTime); // 핸들러로 메시지 보내기
                    }
                } else {
                    Log.d(TAG, "VOD 종료");
                }
            }
        };

        // 새로운 스레드 실행 코드 ->  1초 단위로 요청한다.
        class NewRunnable implements Runnable {

            @Override
            public void run() {
                while (isPlay) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace() ;
                    }
                    mHandler.post(runnable) ;
                }
            }
        }

        NewRunnable nr = new NewRunnable() ;
        Thread thread = new Thread(nr) ;
        thread.start(); // 스레드 시작
    }

    // =========================================================================================================

    private void initializePlayer() { // 비디오 플레이어 초기화

        autoPlay = true;

        // 영상 play 를 위한 player view 생성
        mPlayerView = (PlayerView) findViewById(R.id.view_vod_player);
        mPlayerView.requestFocus();

        // Create a player instance.
        player = new SimpleExoPlayer.Builder(getApplicationContext()).build(); // 플레이어 연결
        mPlayerView.setPlayer(player);

        // HttpDataSource 는 인터넷 상의 mp3, mp4 파일 재생을 도와준다.
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory();

        Uri dashUri = Uri.parse("http://15.164.220.155/stream/dash/" + roomID + ".mpd");

        // Create a DASH media source pointing to a DASH manifest uri.
        MediaSource mediaSource =
                new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(dashUri)); // 미디어 소스 초기화

        player.setPlayWhenReady(autoPlay);

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
    protected void onPause() {
        super.onPause();
        isPlay = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    // =========================================================================================================

    // VOD 에 해당하는 채팅 가져오기 (DB)
    private void getLiveChat(String roomID){
        Call<List<LiveChatItem>> call = mServiceApi.getLiveChat(roomID);
        call.enqueue(new Callback<List<LiveChatItem>>() {
            @Override
            public void onResponse(Call<List<LiveChatItem>> call, Response<List<LiveChatItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mDataList= response.body();

                    for (int i = 0; i < mDataList.size(); i++) {

                        // 서버에서 받아온 메세지 리스트를 가져온다.
                        String senderID = mDataList.get(i).getUserId();
                        String senderName = mDataList.get(i).getUserName();
                        String senderImage = mDataList.get(i).getUserProfileImage();
                        String message = mDataList.get(i).getMessage();
                        String sendTime = mDataList.get(i).getLiveTime();

                        int intSendTime = Integer.parseInt(sendTime);

                       // 객체화 후 해쉬맵에 채팅 시간(key), 채팅 데이터(value)를 저장한다.
                        LiveChatItem liveChatItem = new LiveChatItem(senderID, senderName, senderImage, message);
                        hashMap.put(intSendTime, liveChatItem);
                    }

                    // Iterator 를 활용해서 모든 값을 가져온다.
                    Iterator<Integer> keySetIterator = hashMap.keySet().iterator();

                    // 요소가 있는지 없는지 체크 후 읽을 요소가 없을 때까지 가져온다. (있으면 true, 없으면 false)
                    while (keySetIterator.hasNext()) {
                        Integer key = keySetIterator.next();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<LiveChatItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // ==========================================================================================================

    // 현재 영상 재생 시간 값을 받아서 메시지가 있을 경우 추가한다.
    class SendMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Log.d(TAG, "가져온 시간 값 : " + msg.what);
            int nowTime = msg.what; // 현재 시간 값

            // 해당 재생 시간에 추가할 채팅 목록이 없는 경우
            if(hashMap.get(nowTime) == null) {
                playTime = nowTime;

            } else {  // 해당 재생 시간에 추가할 채팅 목록이 있는 경우

                // 재생 중지 버튼을 클릭했을 경우
                if(playTime == nowTime) {
                    playTime = nowTime;
                } else if(nowTime - playTime >= 2 || playTime - nowTime >= 2) { // 스크롤 바를 앞 뒤로 이동했을 경우

                    // recycler view 클리어 시킨 다음 다시 서버에서 값을 받아온다.
                    mVodChatList.clear();
                    getLiveChat(roomID);

                } else {

                    Log.d(TAG, "현재 시간에 채팅 데이터가 있습니다 -> 채팅 추가");

                    String sender_id = hashMap.get(nowTime).getUserId();
                    String sender_name = hashMap.get(nowTime).getUserName();
                    String sender_profile = hashMap.get(nowTime).getUserProfileImage();
                    String message = hashMap.get(nowTime).getMessage();

                    mVodChatList.add(new LiveChatItem(sender_id, sender_name, sender_profile, message));
                    adapter = new LiveChatAdapter(mVodChatList, VODPlayerActivity.this);
                    mVodChatRV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    focusCurrentMessage(); // 최신 메시지 보여주기
                    playTime = nowTime;
                }
            }
        }
    }

    // =========================================================================================================

    // 마지막 RecyclerView 에 포커싱 (최신 메시지 보여주기)
    public void focusCurrentMessage() {
        mVodChatRV.scrollToPosition(adapter.getItemCount() - 1);
    }
}