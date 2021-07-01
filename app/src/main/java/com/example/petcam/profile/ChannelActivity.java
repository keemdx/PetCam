package com.example.petcam.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.fanboard.FanboardFragment;
import com.example.petcam.profile.notice.NoticeFragment;
import com.example.petcam.profile.vod.VODFragment;
import com.google.android.material.tabs.TabLayout;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.CHANNEL_ID;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.makeStatusBarBlack;

public class ChannelActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private ServiceApi mServiceApi;
    private SharedPreferences pref;
    private String channelID, userID;
    private TextView mUserName, mUserStatus, mFanCount, mFollowingCount;
    private CircleImageView mUserPhoto;
    private CheckBox mFollowStatus;
    private TabLayout tabLayout;
    private ViewPager viewPager;

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
        setContentView(R.layout.activity_channel);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 저장된 유저 정보 가져오기
        pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 아이디

        Intent intent = getIntent();
        channelID = intent.getStringExtra(CHANNEL_ID);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.iv_close).setOnClickListener(onClickListener);

        mUserName = findViewById(R.id.userName);
        mUserStatus = findViewById(R.id.userStatus);
        mUserPhoto = findViewById(R.id.civ_profile_image);
        mFanCount = findViewById(R.id.tv_fan_count);
        mFollowingCount = findViewById(R.id.tv_following_count);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.profile_view_pager); // ViewPager
        mFollowStatus = findViewById(R.id.cb_following);
        mFollowStatus.setOnCheckedChangeListener(this);

        // ViewPager 페이지 개수
        viewPager.setOffscreenPageLimit(3);

        // tabLayout에 ViewPager 연결
        tabLayout.setupWithViewPager(viewPager);

        getChannel(channelID, userID); // 채널 정보 가져오기

        // Fragment 생성
        NoticeFragment noticeFragment = new NoticeFragment();
        VODFragment VODFragment = new VODFragment();
        FanboardFragment fanboardFragment = new FanboardFragment();

        // ViewPagerAdapter을 사용하여 Fragment를 연결
        ChannelViewPagerAdapter viewPagerAdapter = new ChannelViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(noticeFragment, "Notice");
        viewPagerAdapter.addFragment(VODFragment, "Videos");
        viewPagerAdapter.addFragment(fanboardFragment, "Fan Board");
        viewPager.setAdapter(viewPagerAdapter);

    }

    // =========================================================================================================
    public String sendData() {
        return channelID;
    }
    // =========================================================================================================

    @Override
    protected void onStart() {
        super.onStart();
    }
    // =========================================================================================================

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) { // 체크했을 경우 '팬' -> DB 팔로잉 등록
            isChecked();
            saveFollow(userID, channelID);
        } else { // 체크 안 했을 경우 클릭 '팔로잉' 하기 -> DB 에서 팔로잉 지우기
            isNotChecked();
            saveUnfollow(userID, channelID);
        }
    }
    // =========================================================================================================

    // 해당 유저 채널 정보 가져오기
    private void getChannel(String channelID, String userID){
        mServiceApi.getChannel(channelID, userID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();

                String channelUserName= result.getUserName();
                String channelUserPhoto = result.getUserPhoto();
                String channelUserStatus = result.getUserStatus();
                String fanCount= result.getResult();
                String followingCount = result.getMessage();
                boolean followStatus = result.isFollowStatus();

                mUserName.setText(channelUserName);
                mUserStatus.setText(channelUserStatus);
                mFanCount.setText(fanCount);
                mFollowingCount.setText(followingCount);

                Log.e(ACTIVITY_SERVICE,channelUserName + ", " + channelUserPhoto + ", " + channelUserStatus);

                if (channelUserPhoto != null) {
                    Glide.with(ChannelActivity.this).load(channelUserPhoto).centerCrop().into(mUserPhoto);
                } else {
                    Glide.with(ChannelActivity.this).load(R.drawable.ic_user).centerCrop().into(mUserPhoto);
                }

                if(followStatus) {
                    isChecked();

                } else {
                    isNotChecked();
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(ChannelActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // [팔로우] 팔로우 당하는 사람(to_id), 팔로우 거는 사람(from_id) 필요

    private void saveFollow(String userID, String channelID) {

        mServiceApi.saveFollow(userID, channelID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                if(result.getResult().equals("success")) {
                    getChannel(channelID, userID); // 채널 정보 가져오기
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

    // [언팔로우] 팔로우 삭제 당하는 사람(to_id), 하는 사람(from_id) 필요
    private void saveUnfollow(String userID, String channelID) {

        mServiceApi.saveUnfollow(userID, channelID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                if(result.getResult().equals("success")) {
                    getChannel(channelID, userID); // 채널 정보 가져오기
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
    // UI 관련

    private void isChecked() { // 체크했을 경우 UI
        mFollowStatus.setChecked(true);
        mFollowStatus.setText("Fan");
        mFollowStatus.setPadding(40,0, 40, 0);
        mFollowStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private void isNotChecked() { // 체크 해제할 경우 UI
        mFollowStatus.setChecked(false);
        mFollowStatus.setText("Following");
        @SuppressLint("UseCompatLoadingForDrawables") Drawable img = getResources().getDrawable(R.drawable.ic_add_small);
        img.setTint(Color.WHITE);
        mFollowStatus.setPadding(40,0, 28, 0);
        mFollowStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
    }

    // =========================================================================================================
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}