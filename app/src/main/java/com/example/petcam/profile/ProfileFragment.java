package com.example.petcam.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.chatting.ChattingActivity;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.fanboard.FanboardFragment;
import com.example.petcam.profile.notice.NoticeFragment;
import com.example.petcam.profile.vod.VODFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.NEW_CHATROOM_ID;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_STATUS;
import static com.example.petcam.function.App.USER_UID;


/**
 * Class: SettingsActivity
 *
 * Comment
 * 이 액티비티는 프로필 및 설정 관련 목록을 보여줍니다.
 **/

public class ProfileFragment extends Fragment {

    private int FAN_CODE = 0;
    private int FOLLOWING_CODE = 1;

    private ServiceApi mServiceApi;
    private SharedPreferences pref;
    private TextView mUserName, mUserStatus, mFanCount, mFollowingCount;
    private CircleImageView mUserImage;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String userID;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 메뉴 버튼을 클릭했을 경우,
                case R.id.iv_settings:
                    // 설정 화면으로 이동한다.
                    Intent settingIntent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(settingIntent);
                    break;

                // 프로필 수정 아이콘을 클릭했을 경우,
                case R.id.iv_edit_profile:
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    startActivity(intent);
                    break;

                case R.id.tv_fan_count:
                        Intent fanIntent = new Intent(getActivity(), FollowListActivity.class);
                        fanIntent.putExtra(USER_UID, userID);
                        fanIntent.putExtra("CODE", FAN_CODE);
                        startActivity(fanIntent);
                    break;

                case R.id.tv_following_count:
                        Intent followingIntent = new Intent(getActivity(), FollowListActivity.class);
                        followingIntent.putExtra(USER_UID, userID);
                        followingIntent.putExtra("CODE", FOLLOWING_CODE);
                        startActivity(followingIntent);
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // UI 선언
        mUserName = view.findViewById(R.id.userName);
        mUserStatus = view.findViewById(R.id.userStatus);
        mUserImage = view.findViewById(R.id.civ_profile_image);
        mFanCount = view.findViewById(R.id.tv_fan_count);
        mFollowingCount = view.findViewById(R.id.tv_following_count);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.profile_view_pager); // ViewPager

        // ViewPager 페이지 개수
        viewPager.setOffscreenPageLimit(3);

        // tabLayout에 ViewPager 연결
        tabLayout.setupWithViewPager(viewPager);

        // Fragment 생성
        NoticeFragment noticeFragment = new NoticeFragment();
        VODFragment VODFragment = new VODFragment();
        FanboardFragment fanboardFragment = new FanboardFragment();

        // ViewPagerAdapter을 사용하여 Fragment를 연결
        ChannelViewPagerAdapter viewPagerAdapter = new ChannelViewPagerAdapter(getChildFragmentManager(), 0);
        viewPagerAdapter.addFragment(noticeFragment, "공지");
        viewPagerAdapter.addFragment(VODFragment, "동영상");
        viewPagerAdapter.addFragment(fanboardFragment, "팬보드");
        viewPager.setAdapter(viewPagerAdapter);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        view.findViewById(R.id.iv_settings).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_edit_profile).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_fan_count).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_following_count).setOnClickListener(onClickListener);

        return view;
    }
    // =========================================================================================================
    public String sendData() {
        return userID;
    }
    // =========================================================================================================

    @Override
    public void onStart() {
        super.onStart();
        // 저장된 유저 정보 가져오기
        pref = getActivity().getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 아이디
        String userPhoto = pref.getString(USER_IMAGE,""); // 유저 프로필 이미지
        String userName = pref.getString(USER_NAME,""); // 유저 닉네임
        String userStatus = pref.getString(USER_STATUS,""); // 유저 닉네임
        mUserName.setText(userName);
        mUserStatus.setText(userStatus);

        if (userPhoto != null) {
            // 현재 유저 프로필 이미지 보여주기
            Glide.with(this).load(userPhoto).centerCrop().into(mUserImage);

        } else {
            // 현재 유저 프로필 이미지 보여주기
            Glide.with(this).load(R.drawable.ic_user).centerCrop().into(mUserImage);
        }
        getFollow(userID); // 팔로잉, 팬 수 가져오기
    }

    // =========================================================================================================
    // 팔로잉, 팬 수 가져오기
    private void getFollow(String userID){
        mServiceApi.getFollow(userID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                String followingCount= result.getResult(); // 채팅룸 유저
                String fanCount = result.getMessage(); //채팅룸 유저 수

                mFanCount.setText(fanCount);
                mFollowingCount.setText(followingCount);
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(getContext(), "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("getChatroomInfo 에러 발생", t.getMessage());
            }
        });
    }
}