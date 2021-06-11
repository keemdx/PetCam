package com.example.petcam.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.profile.fanboard.FanboardFragment;
import com.example.petcam.profile.notice.NoticeFragment;
import com.example.petcam.profile.vod.VODFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_STATUS;


/**
 * Class: SettingsActivity
 *
 * Comment
 * 이 액티비티는 프로필 및 설정 관련 목록을 보여줍니다.
 **/

public class ProfileFragment extends Fragment {

    private SharedPreferences pref;
    private TextView mUserName, mUserStatus;
    private CircleImageView mUserImage;
    private TabLayout tabLayout;
    private ViewPager viewPager;

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

        // UI 선언
        mUserName = view.findViewById(R.id.userName);
        mUserStatus = view.findViewById(R.id.userStatus);
        mUserImage = view.findViewById(R.id.civ_profile_image);
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 저장된 유저 정보 가져오기
        pref = getActivity().getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
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
    }
}