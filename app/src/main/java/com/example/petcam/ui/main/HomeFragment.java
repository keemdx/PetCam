package com.example.petcam.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.petcam.R;
import com.example.petcam.databinding.FragmentHomeBinding;
import com.example.petcam.ui.chatting.ChatroomActivity;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ServiceApi mServiceApi;
    private String userID;
    private RecyclerView mFollowingRV, mPopularRV, mChartsRV;
    private List<PopularItem> mPopularList;
    private List<FollowingItem> mFollowingList;
    private List<ChartVideosItem> mVideosList;
    private RadioGroup radioGroup;
    private RadioButton mVideosRB, mChannelsRB;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.

        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 저장된 유저 정보 가져오기
        SharedPreferences pref = getActivity().getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 이미지

        binding.ivMessage.setOnClickListener(v -> startChatRoomActivity());
        binding.ivSearch.setOnClickListener(v -> startSearchActivity());

        radioGroup = binding.rcGroup;
        mVideosRB = binding.rbVideos;
        mChannelsRB = binding.rbChannels;
        mVideosRB.setChecked(true);

        // 라이브 방송 중인 팔로잉 유저 (친구) 리스트
        mFollowingRV = binding.rvFollowing;
        LinearLayoutManager fManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mFollowingRV.setLayoutManager(fManager); // LayoutManager 등록

        getLiveNowFriends(userID); // 라이브 중인 친구 리스트 가져오기

        // 현재 인기 실시간 방송 영상 리스트
        mPopularRV = binding.rvPopular;
        LinearLayoutManager pManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mPopularRV.setLayoutManager(pManager); // LayoutManager 등록


        // 차트 리스트
        mChartsRV = binding.rvCharts;
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mChartsRV.setLayoutManager(manager); // LayoutManager 등록

        getHotLiveRooms(); // 실시간 방송 뷰어(시청자) 순으로 가져오기

        getVODChart("video"); // 채널 차트 가져오기

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    // 채널 2개 버튼
                    case R.id.rb_videos: // 비디오(vod) 차트

                        getVODChart("video"); // 비디오(vod) 차트 가져오기
                        break;

                    case R.id.rb_channels: // 채널 차트

                        getChannelChart("channel"); // 채널 차트 가져오기
                        break;

                    default:
                        break;
                }

            }
        });

        // 스와이프로 새로고침하기
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 동작이 완료 되면 새로고침 아이콘 없애기
                binding.swipeRefresh.setRefreshing(false);

                mFollowingList.clear();
                mPopularList.clear();
                mVideosList.clear();

                getLiveNowFriends(userID);
                getHotLiveRooms();
                getVODChart("video");
            }
        });
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void startSearchActivity() {
        Intent searchIntent = new Intent(getContext(), SearchActivity.class);
        startActivity(searchIntent);
        getActivity().overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_not_move);
    }

    private void startChatRoomActivity() {
        Intent intent = new Intent(getContext(), ChatroomActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }

    // =========================================================================================================

    // 라이브 중인 친구 (팔로잉) 리스트 가져오기
    private void getLiveNowFriends(String uid) {

        Call<List<FollowingItem>> call = mServiceApi.getLiveNowFriends(uid);
        call.enqueue(new Callback<List<FollowingItem>>() {
            @Override
            public void onResponse(Call<List<FollowingItem>> call, Response<List<FollowingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mFollowingList = response.body();
                    mFollowingRV.setVisibility(View.VISIBLE);
                    FollowingAdapter mFollowingAdapter = new FollowingAdapter(mFollowingList, getContext());
                    mFollowingRV.setAdapter(mFollowingAdapter);
                    mFollowingAdapter.notifyDataSetChanged();

                } else {
                    if (mFollowingRV.getVisibility() == View.VISIBLE) {
                        mFollowingRV.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FollowingItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // HOT LIVE : 현재 실시간 방송 중인 룸 조회 수 순으로 정렬해서 보여주기
    private void getHotLiveRooms() {

        Call<List<PopularItem>> call = mServiceApi.getHotLiveRooms(userID);
        call.enqueue(new Callback<List<PopularItem>>() {
            @Override
            public void onResponse(Call<List<PopularItem>> call, Response<List<PopularItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mPopularList = response.body();
                    mPopularRV.setVisibility(View.VISIBLE);
                    PopularAdapter mPopularAdapter = new PopularAdapter(mPopularList, getContext());
                    mPopularRV.setAdapter(mPopularAdapter);
                    mPopularAdapter.notifyDataSetChanged();

                } else {
                    if (mPopularRV.getVisibility() == View.VISIBLE) {
                        mPopularRV.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PopularItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // [데일리 차트] 유저 채널 차트 팔로워 순으로 가져와서 보여주기
    private void getChannelChart(String chart) {

        Call<List<ChartChannelsItem>> call = mServiceApi.getChannelChart(chart);
        call.enqueue(new Callback<List<ChartChannelsItem>>() {
            @Override
            public void onResponse(Call<List<ChartChannelsItem>> call, Response<List<ChartChannelsItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChartChannelsItem> mChannelsList = response.body();
                    ChartChannelsAdapter adapter = new ChartChannelsAdapter(mChannelsList, getContext(), getActivity());
                    adapter.notifyDataSetChanged();
                    mChartsRV.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ChartChannelsItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // [데일리 차트] -> 오늘 저장된 VOD 뷰어 순으로 가져와서 보여주기
    private void getVODChart(String chart) {

        Call<List<ChartVideosItem>> call = mServiceApi.getVODChart(chart);
        call.enqueue(new Callback<List<ChartVideosItem>>() {
            @Override
            public void onResponse(Call<List<ChartVideosItem>> call, Response<List<ChartVideosItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mVideosList = response.body();
                    ChartVideosAdapter adapter = new ChartVideosAdapter(mVideosList, getContext());
                    adapter.notifyDataSetChanged();
                    mChartsRV.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ChartVideosItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}