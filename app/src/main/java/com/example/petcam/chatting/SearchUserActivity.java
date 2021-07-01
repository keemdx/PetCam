package com.example.petcam.chatting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.notice.NoticeCommentAdapter;
import com.example.petcam.profile.notice.NoticeDetailActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.CHAT_DATA;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.MESSAGE_TEXT;
import static com.example.petcam.function.App.NEW_CHATROOM_ID;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.ROOM_STATUS;
import static com.example.petcam.function.App.SEND_TIME;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_INVITE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_NICKNAME;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: SearchUserActivity
 *
 * Comment
 * 채팅 최초 생성 및 유저 초대에 필요한 유저 검색 액티비티입니다.
 **/

public class SearchUserActivity extends AppCompatActivity implements SearchUserAdapter.OnListItemSelectedInterface {

    private static final String TAG = "SearchUserActivity";
    private static final String START_ROOM = "START_ROOM"; // 최초 접속 (초대)

    private ServiceApi mServiceApi;
    private String uid, userName, userID, roomID, userPhoto, inviteUserName;
    private ImageView mSendButton;
    private ChipGroup mChipGroup;
    private EditText mUserInput;
    private RecyclerView mUserListView;
    private TextView mUserTitle;
    private List<SearchUserItem> mList;
    private List<String> mChipList;
    private SharedPreferences sharedPreferences;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 버튼을 클릭했을 경우,
                case R.id.iv_close:
                    finish(); // 이 액티비티 화면을 닫는다.
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        mUserListView = findViewById(R.id.rv_users);
        mUserInput = findViewById(R.id.et_search_user);
        mSendButton = findViewById(R.id.iv_send);
        mUserTitle = findViewById(R.id.tv_chat_name);
        mChipGroup = findViewById(R.id.chipGroup);
        mChipList = new ArrayList<>();

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);

        // 초대를 위해 Chatting Activity 에서 넘겨받음
        roomID = null;
        Intent intent = getIntent();
        userID = intent.getStringExtra(USER_UID);
        roomID = intent.getStringExtra(ROOM_ID);

        if(roomID != null){
            mUserTitle.setText("Invite");
        }

        // 메시지 전송 버튼 작동하지 않도록 설정
        mSendButton.setEnabled(false);
        // 메시지 전송 버튼
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 룸 번호가 이미 있다면, 초대하기 (기존 룸에 유저 추가 후 해당 룸 Chatting Activity 로 이동)
                if(roomID != null) {

                    inviteChatroom(roomID);

                } else {
                    mChipList.add(userName);
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < mChipList.size(); i++) {
                        map.put("userName" + i, mChipList.get(i));
                    }
                    // DB에 채팅 룸 생성해서 저장, 채팅 룸 번호를 가져온다.
                    createChatroom(map);
                }
            }
        });

        mList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mUserListView.setLayoutManager(linearLayoutManager);

        // 쉐어드 프리퍼런스에 저장된 로그인된 사용자의 uid를 가져온다.
        sharedPreferences = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        uid = sharedPreferences.getString(USER_UID,"");
        userName = sharedPreferences.getString(USER_NAME,"");
        userPhoto = sharedPreferences.getString(USER_IMAGE, ""); // 유저 프로필 이미지

        // 새로운 메시지를 보낼 유저 리스트를 가져온다.
        getUsers(uid, roomID);

        // EditText 입력 시 실시간으로 리사이클러뷰 띄우기
        mUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput = charSequence.toString(); // 유저가 입력한 텍스트 (닉네임)
                List<SearchUserItem> newList = new ArrayList<>(); // 검색한 닉네임 결과 저장할 리스트
                for(SearchUserItem searchUserItem : mList) {
                    if (searchUserItem.getUserName().contains(userInput)){
                        newList.add(searchUserItem);
                        SearchUserAdapter searchUserAdapter = new SearchUserAdapter(newList, SearchUserActivity.this, SearchUserActivity.this);
                        mUserListView.setAdapter(searchUserAdapter);
                        searchUserAdapter.notifyDataSetChanged();
                        if (userInput.isEmpty()) {
                         newList.clear();
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    // =========================================================================================================
    private void getUsers(String uid, String roomID){
        Call<List<SearchUserItem>> call = mServiceApi.getUsers(uid, roomID);
        call.enqueue(new Callback<List<SearchUserItem>>() {
            @Override
            public void onResponse(Call<List<SearchUserItem>> call, Response<List<SearchUserItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mList = response.body();
                }
            }
            @Override
            public void onFailure(Call<List<SearchUserItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // 개설 된 채팅룸 DB에 저장 -> 선택된 멤버로 이미 개설된 방이 없을 경우, 방 개설

    @SuppressLint("SimpleDateFormat")
    private void createChatroom(Map<String, Object> map) {

        mServiceApi.createChatroom(map).enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                roomID = result.getMessage();
                Log.e("CREATE-ROOM NO. : ", roomID);
                Intent intent = new Intent(SearchUserActivity.this, ChattingActivity.class);
                intent.putExtra(NEW_CHATROOM_ID, roomID);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(SearchUserActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }
    // =========================================================================================================

    @SuppressLint("ResourceAsColor")
    @Override
    public void onItemSelected(View v, int position) {

        // 아이콘 컬러 변경을 위한 ColorStateList
        @SuppressLint("UseCompatLoadingForColorStateLists") ColorStateList colorPrimary = getResources().getColorStateList(R.color.colorPrimary);
        @SuppressLint("UseCompatLoadingForColorStateLists") ColorStateList colorGray = getResources().getColorStateList(R.color.darkGray);

        // viewHolder 연결해서 선택된 포지션의 유저 이름 가져오기
        SearchUserAdapter.ViewHolder viewHolder = (SearchUserAdapter.ViewHolder) mUserListView.findViewHolderForAdapterPosition(position);
        String userName = viewHolder.tv_name.getText().toString();

        // 만약 chip 리스트에 지금 선택된 유저 이름이 존재한다면, "이미 선택된 사용자입니다."
        if(mChipList.contains(userName)) {
            Toast.makeText(SearchUserActivity.this, "이미 선택된 사용자입니다.", Toast.LENGTH_SHORT);

            // 존재하지 않는다면, chip 생성, chip 리스트에도 담는다.
        } else {
            mChipList.add(userName);
            Chip chip = new Chip(this);
            chip.setText(userName);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setClickable(false);
            mChipGroup.addView(chip);
            mChipGroup.setVisibility(View.VISIBLE);

            // 만약 chip 리스트가 비어있지 않다면, 보내기 아이콘 컬러를 바꿔준다. (선택된 유저가 있으므로 메시지 보내기 가능)
            if (!mChipList.isEmpty()) {
                mSendButton.setImageTintList(colorPrimary);
                mSendButton.setEnabled(true);
            }
            // chip 닫기를 누를 경우,
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // chip 리스트에서 해당 유저 이름을 삭제한다.
                    mChipList.remove(chip.getText());
                    // 선택된 chip을 삭제한다.
                    mChipGroup.removeView(chip);
                    // 만약 삭제 후 chip 리스트가 비어있다면, 보내기 아이콘 컬러를 그레이로 바꾼다. (보내기 불가)
                    if (mChipList.isEmpty()) {
                        mSendButton.setImageTintList(colorGray);
                        mSendButton.setEnabled(false);
                    }
                }
            });
        }

    }

    @SuppressLint("SimpleDateFormat")
    private void inviteChatroom(String roomID) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일_hh:mm a", Locale.KOREA);
        String fullTimeNow = simpleDateFormat.format(new Date());

        Map<String, Object> inviteMap = new HashMap<>();
        for (int i = 0; i < mChipList.size(); i++) {
            inviteMap.put("userName" + i, mChipList.get(i));
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            inviteUserName = String.join(", ", mChipList);
        }

        mServiceApi.inviteChatroom(inviteMap, roomID).enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                try {

                    Log.d(TAG, "**************************************************");
                    Log.d(TAG, "전송 버튼 클릭 시 메세지를 서비스로 날린다.");
                    Log.d(TAG, "**************************************************");

                    // 메세지를 서비스로 보내는 곳
                    JSONObject object = new JSONObject();
                    object.put(ROOM_ID, roomID);
                    object.put(ROOM_STATUS, START_ROOM);
                    object.put(USER_UID, userID);
                    object.put(USER_NICKNAME, userName);
                    object.put(USER_IMAGE, userPhoto);
                    object.put(USER_INVITE, inviteUserName);
                    object.put(MESSAGE_TEXT, "INVITE");
                    object.put(SEND_TIME, fullTimeNow);
                    String messageData = object.toString();

                    Log.d(TAG, messageData);

                    Intent messageIntent = new Intent(SearchUserActivity.this, ChattingService.class); // 액티비티 ㅡ> 서비스로 메세지 전달
                    messageIntent.putExtra(CHAT_DATA, messageData);
                    startService(messageIntent);
                    finish();

                } catch (
                        JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(SearchUserActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}