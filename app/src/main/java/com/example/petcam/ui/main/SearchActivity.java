package com.example.petcam.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.petcam.R;
import com.example.petcam.ui.chatting.SearchUserAdapter;
import com.example.petcam.ui.chatting.SearchUserItem;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.ui.profile.ChannelActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.CHANNEL_ID;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

public class SearchActivity extends AppCompatActivity implements SearchUserAdapter.OnListItemSelectedInterface {

    private ServiceApi mServiceApi;
    private SharedPreferences sharedPreferences;
    private EditText mUserInput;
    private RecyclerView mUserListView;
    private List<SearchUserItem> mList;
    private String userID;

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
        setContentView(R.layout.activity_search);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 쉐어드 프리퍼런스에 저장된 로그인된 사용자의 uid를 가져온다.
        sharedPreferences = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = sharedPreferences.getString(USER_UID, "");

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);

        mUserListView = findViewById(R.id.rv_users);
        mUserInput = findViewById(R.id.et_search_user);

        mList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mUserListView.setLayoutManager(linearLayoutManager);

        getUsers(userID, null);

        // EditText 입력 시 실시간으로 리사이클러뷰 띄우기
        mUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput = charSequence.toString(); // 유저가 입력한 텍스트 (닉네임)
                List<SearchUserItem> newList = new ArrayList<>(); // 검색한 닉네임 결과 저장할 리스트
                for (SearchUserItem searchUserItem : mList) {
                    if (searchUserItem.getUserName().contains(userInput)) {
                        newList.add(searchUserItem);
                        SearchUserAdapter searchUserAdapter = new SearchUserAdapter(newList, SearchActivity.this, SearchActivity.this);
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

    // 유저 리스트 가져오기
    private void getUsers(String uid, String roomID) {
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
    @Override
    public void onItemSelected(View v, int position) {
        // viewHolder 연결해서 선택된 포지션의 유저 아이디 가져오기
        SearchUserAdapter.ViewHolder viewHolder = (SearchUserAdapter.ViewHolder) mUserListView.findViewHolderForAdapterPosition(position);
        String searchUserID = viewHolder.tv_id.getText().toString();

        Intent intent = new Intent(getApplicationContext(), ChannelActivity.class);
        intent.putExtra(CHANNEL_ID, searchUserID);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }

    // =========================================================================================================
    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.anim_not_move, R.anim.anim_slide_out_right);
    }
}