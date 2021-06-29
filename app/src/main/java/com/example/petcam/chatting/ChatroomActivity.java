package com.example.petcam.chatting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.petcam.R;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.notice.FixTopNoticeAdapter;
import com.example.petcam.chatting.ChatroomItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.MESSAGE_TEXT;
import static com.example.petcam.function.App.RECEIVE_DATA;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.ROOM_STATUS;
import static com.example.petcam.function.App.SEND_TIME;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_NICKNAME;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: ChatroomActivity
 *
 * Comment
 * 유저의 채팅룸 리스트를 보여주는 액티비티입니다.
 **/

public class ChatroomActivity extends AppCompatActivity{

    private static final String TAG = "ChatroomActivity";

    private ServiceApi mServiceApi;
    private SharedPreferences pref;
    private String userID;
    private RecyclerView rv_chatroom;
    private List<ChatroomItem> mChatroomList;
    private SwipeRefreshLayout mSwipeRefresh;

    BroadcastReceiver mReceiver;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    finish(); // 이 액티비티 화면을 닫는다.
                    break;

                // 새로운 유저 검색하러 가기
                case R.id.iv_search_user:
                    Intent intent = new Intent(ChatroomActivity.this, SearchUserActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 이미지

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);
        findViewById(R.id.iv_search_user).setOnClickListener(onClickListener);

        rv_chatroom = (RecyclerView) findViewById(R.id.rv_chatroom);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_chatroom.setLayoutManager(manager); // LayoutManager 등록

        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.dwfox.myapplication.SEND_BROAD_CAST");

        getChatroom(userID); // 채팅룸 리스트 가져오기

        // 스와이프로 새로고침하기
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 동작이 완료 되면 새로고침 아이콘 없애기
                mSwipeRefresh.setRefreshing(false);

                mChatroomList.clear();
                getChatroom(userID);
            }
        });

        // Receiver 구현
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String getMessage = intent.getStringExtra(RECEIVE_DATA);
                Log.d(TAG, "브로드 캐스트 리시버 받은 메시지 : " + getMessage);
                if(getMessage!=null) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                                getChatroom(userID); // 채팅룸 리스트 가져오기
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        };

        // Receiver 등록
        registerReceiver(mReceiver, intentfilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart 실행");
        getChatroom(userID); // 채팅룸 리스트 가져오기
    }

    // =========================================================================================================
    // 채팅룸 리스트 가져오기
    private void getChatroom(String uid){

        Call<List<ChatroomItem>> call = mServiceApi.getChatroom(uid);
        call.enqueue(new Callback<List<ChatroomItem>>() {
            @Override
            public void onResponse(Call<List<ChatroomItem>> call, Response<List<ChatroomItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mChatroomList = response.body();
                    ChatroomAdapter chatroomAdapter = new ChatroomAdapter(mChatroomList, ChatroomActivity.this, ChatroomActivity.this);
                    rv_chatroom.setAdapter(chatroomAdapter);
                    chatroomAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<ChatroomItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver); // 브로드 캐스트 리시버 끊기
    }

    // =========================================================================================================
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}