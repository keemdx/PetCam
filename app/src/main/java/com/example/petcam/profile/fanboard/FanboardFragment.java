package com.example.petcam.profile.fanboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcam.R;
import com.example.petcam.chatting.SearchUserAdapter;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.ChannelActivity;
import com.example.petcam.profile.FollowListActivity;
import com.example.petcam.profile.ProfileFragment;
import com.example.petcam.profile.notice.NoticeDetailActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.core.content.ContextCompat.getColor;
import static com.example.petcam.function.App.CHANNEL_ID;
import static com.example.petcam.function.App.FANBOARD_CONTENT;
import static com.example.petcam.function.App.FANBOARD_ID;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;

public class FanboardFragment extends Fragment implements FanboardAdapter.OnListItemSelectedInterface{

    private ServiceApi mServiceApi;
    private SharedPreferences sharedPreferences;
    private FanboardAdapter fanboardAdapter;
    private LinearLayout layout_none;
    private ConstraintLayout mFanboardcontent, layout_content_edit;
    private EditText mContent, mEditContent;
    private TextView mAddContent, mEditFanboardcontent;
    private String uid, myID, editContent, editContentID;
    private RecyclerView rv_fanboard;
    private List<FanboardItem> mDataList;
    private InputMethodManager imm;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 버튼을 클릭했을 경우,
                case R.id.iv_edit_close:
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    mEditContent.setText("");
                    mFanboardcontent.setVisibility(View.VISIBLE);
                    layout_content_edit.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fanboard, container, false);
        // Inflate the layout for this fragments

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        view.findViewById(R.id.iv_edit_close).setOnClickListener(onClickListener);

        // 선언
        layout_none = view.findViewById(R.id.layout_none);
        mFanboardcontent = view.findViewById(R.id.layout_fanboard_add);
        mContent = (EditText) view.findViewById(R.id.et_fanboard); // 팬레터 입력하는 부분
        mAddContent = (TextView) view.findViewById(R.id.tv_add_fanboard);
        layout_content_edit = view.findViewById(R.id.layout_fanboard_edit);
        mEditFanboardcontent = (TextView) view.findViewById(R.id.tv_add_fanboard_edit);
        mEditContent =  (EditText) view.findViewById(R.id.et_fanboard_edit); // 팬레터 수정하는 부분
        rv_fanboard = view.findViewById(R.id.rv_fanboard);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_fanboard.setLayoutManager(linearLayoutManager); // LayoutManager 등록

        // 쉐어드 프리퍼런스에 저장된 로그인된 사용자의 uid를 가져온다.
        sharedPreferences = getActivity().getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        myID = sharedPreferences.getString(USER_UID, "");

        if(getActivity().getClass().getSimpleName().equals("ChannelActivity")) {
            ChannelActivity channelActivity = (ChannelActivity) getActivity();
            uid = channelActivity.sendData();
        } else {
            uid = sharedPreferences.getString(USER_UID, "");
        }

        if(uid.equals(myID)) {
            mFanboardcontent.setVisibility(View.GONE);
        } else {
            mFanboardcontent.setVisibility(View.VISIBLE);
        }

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        getFanboard(uid);  // 팬보드 리스트를 가져와서 보여준다.
        contentUploadButton(myID, uid); // 팬보드 업로드 시 실행
    }

    // =========================================================================================================
    // 상단 고정 공지사항 가져오기
    private void getFanboard(String uid){

        Call<List<FanboardItem>> call = mServiceApi.getFanboard(uid);
        call.enqueue(new Callback<List<FanboardItem>>() {
            @Override
            public void onResponse(Call<List<FanboardItem>> call, Response<List<FanboardItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mDataList = response.body();
                    // 공지사항이 있다면 '등록된 글이 없습니다.' 안내문을 삭제한다.
                    if(!mDataList.isEmpty()) {
                        layout_none.setVisibility(View.GONE);
                    }
                    fanboardAdapter = new FanboardAdapter(mDataList, getActivity(), FanboardFragment.this , myID, uid);
                    rv_fanboard.setAdapter(fanboardAdapter);
                    fanboardAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<FanboardItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 코멘트 업로드 시 빈칸 여부 확인
    private void contentUploadButton(String userID, String channelID) {

        mContent.addTextChangedListener(new TextWatcher() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 공지사항 제목 입력 여부 확인
                if (s.toString().trim().length()==0) {
                    mAddContent.setTextColor(getColor(getActivity(),R.color.darkGray));
                    mAddContent.setClickable(false);
                } else {
                    mAddContent.setTextColor(getColor(getActivity(),R.color.colorPrimary));
                    mAddContent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveFanboardContent(userID, channelID);
                        }
                    });
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        mEditContent.addTextChangedListener(new TextWatcher() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 공지사항 제목 입력 여부 확인
                if (s.toString().trim().length()==0 || mEditContent.getText().toString().equals(editContent)) {
                    mEditFanboardcontent.setTextColor(getColor(getActivity(),R.color.darkGray));
                    mEditFanboardcontent.setClickable(false);
                } else {
                    mEditFanboardcontent.setTextColor(getColor(getActivity(),R.color.colorPrimary));
                    mEditFanboardcontent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editFanboardContent();
                        }
                    });
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
    }

    // =========================================================================================================
    // 팬보드 DB 통신 후 업로드
    @SuppressLint("SimpleDateFormat")
    private void saveFanboardContent(String userID, String channelID) {
        // DATE FORMAT
        String createAt =  String.valueOf(System.currentTimeMillis());
        String content = mContent.getText().toString();

        Log.e(ACTIVITY_SERVICE, userID + ", " + channelID + ", " + content + ", " + createAt);

        mServiceApi.saveFanboard(userID, channelID, content, createAt).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_SHORT).show();

                if(result.getResult().equals("success")) {
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mContent.setText("");
                    getFanboard(uid);
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(getActivity(), "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 팬보드 DB 통신 후 수정
    @SuppressLint("SimpleDateFormat")
    private void editFanboardContent() {
        // DATE FORMAT
        String content = mEditContent.getText().toString();

        mServiceApi.editFanboard(editContentID, content).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_SHORT).show();

                if(result.getResult().equals("success")) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    mEditContent.setText("");
                    mFanboardcontent.setVisibility(View.VISIBLE); // 등록 레이아웃 사라지게 한다.
                    layout_content_edit.setVisibility(View.GONE); // 수정 레이아웃 보여준다.
                    getFanboard(uid);
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(getActivity(), "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    @Override
    public void onItemSelected(View v, int position, String writerID) {

        // viewHolder 연결해서 선택된 포지션의 유저 이름 가져오기
        FanboardAdapter.ViewHolder viewHolder = (FanboardAdapter.ViewHolder) rv_fanboard.findViewHolderForAdapterPosition(position);
        editContent = viewHolder.tv_text.getText().toString();
        editContentID = viewHolder.tv_id.getText().toString();

        // Display option menu
        PopupMenu popup = new PopupMenu(getContext(),viewHolder.iv_more);
        Menu menu = popup.getMenu();

        // 공지사항 상단 고정 관련 메뉴 생성
        if(writerID.equals(myID)){
            menu.add(0, 0, 0, "수정");
        }

        // 수정, 삭제 기능 메뉴
        popup.getMenuInflater().inflate(R.menu.menu_fanboard, popup.getMenu());

            //adding click listener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        // 상단 고정 해제
                        case 0:
                            mFanboardcontent.setVisibility(View.GONE); // 등록 레이아웃 사라지게 한다.
                            layout_content_edit.setVisibility(View.VISIBLE); // 수정 레이아웃 보여준다.
                            mEditContent.setText(editContent);
                            mEditContent.requestFocus();
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                            break;

                        case R.id.menu_delete: // 팬보드 삭제
                            alertDialog(position, editContentID); // 삭제하시겠습니까? 팝업창
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
            //displaying the popup
            popup.show();
        }

    // =========================================================================================================
    // 삭제 확인 팝업 다이알로그
    public void alertDialog(final int position, String contentID) {

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage("선택한 글을 삭제하시겠습니까?");
        alert.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeComment(position, contentID);
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.create().show();
    }

    // =========================================================================================================
    // DB 연결 후 해당 댓글 삭제
    private void removeComment(int position, String contentID) {

        mServiceApi.removeFanboard(contentID).enqueue(new Callback<ResultModel>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                // 정상적으로 네트워크 통신 완료
                ResultModel result = response.body();
                Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_SHORT).show();
                // 성공적으로 DB 내 공지사항 삭제를 완료했을 경우 액티비티를 닫는다.
                if(result.getResult().equals("success")) {
                    //arraylist에서 해당 인덱스의 아이템 객체를 지워주고
                    mDataList.remove(position);
                    //어댑터에 알린다.
                    fanboardAdapter.notifyItemRemoved(position);
                    getFanboard(uid);
                }
            }
            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(getActivity(), "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }
}