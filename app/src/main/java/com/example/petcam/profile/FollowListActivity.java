package com.example.petcam.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.petcam.R;
import com.example.petcam.chatting.ChatroomActivity;
import com.example.petcam.chatting.ChatroomAdapter;
import com.example.petcam.chatting.SearchUserAdapter;
import com.example.petcam.chatting.SearchUserItem;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.CHANNEL_ID;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

public class FollowListActivity extends AppCompatActivity implements FollowListAdapter.OnListItemSelectedInterface {

    private int FAN_CODE = 0;
    private int FOLLOWING_CODE = 1;
    private ServiceApi mServiceApi;
    private String userID;
    private int CODE;
    private TextView mTitle;
    private List<FollowListItem> mFollowingList;
    private RecyclerView mFollowRV;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.iv_close:
                   finish();
                   break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        mTitle = (TextView) findViewById(R.id.tv_follow);
        mFollowRV = (RecyclerView) findViewById(R.id.rv_follow);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mFollowRV.setLayoutManager(manager); // LayoutManager 등록


        Intent intent = getIntent();
        userID = intent.getStringExtra(USER_UID);
        CODE = intent.getIntExtra("CODE",0);

        if(CODE == FAN_CODE) {
            mTitle.setText("Fans");
            getFanList(userID);
        } else if(CODE == FOLLOWING_CODE) {
            mTitle.setText("Following");
            getFollowingList(userID);
        }

    }

    // =========================================================================================================
    private void getFollowingList(String userID){
        Call<List<FollowListItem>> call = mServiceApi.getFollowingList(userID);
        call.enqueue(new Callback<List<FollowListItem>>() {
            @Override
            public void onResponse(Call<List<FollowListItem>> call, Response<List<FollowListItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mFollowingList= response.body();
                    FollowListAdapter followListAdapter = new FollowListAdapter(mFollowingList, FollowListActivity.this, FollowListActivity.this);
                    mFollowRV.setAdapter(followListAdapter);
                    followListAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<FollowListItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }
    // =========================================================================================================
    private void getFanList(String userID){
        Call<List<FollowListItem>> call = mServiceApi.getFanList(userID);
        call.enqueue(new Callback<List<FollowListItem>>() {
            @Override
            public void onResponse(Call<List<FollowListItem>> call, Response<List<FollowListItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mFollowingList= response.body();
                    FollowListAdapter followListAdapter = new FollowListAdapter(mFollowingList, FollowListActivity.this, FollowListActivity.this);
                    mFollowRV.setAdapter(followListAdapter);
                    followListAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<FollowListItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    @Override
    public void onItemSelected(View v, int position) {
        // viewHolder 연결해서 선택된 포지션의 유저 id 가져오기
        FollowListAdapter.ViewHolder viewHolder = (FollowListAdapter.ViewHolder) mFollowRV.findViewHolderForAdapterPosition(position);
        String channelID = viewHolder.tv_id.getText().toString();
        Intent intent = new Intent(FollowListActivity.this, ChannelActivity.class);
        intent.putExtra(CHANNEL_ID, channelID);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }
}