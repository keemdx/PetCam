package com.example.petcam.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.petcam.R;
import com.example.petcam.chatting.ChatroomActivity;


public class HomeFragment extends Fragment {

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 다이렉트 메시지 아이콘을 클릭했을 경우, 채팅 리스트 액티비티로 넘어간다.
                case R.id.iv_message:
                    Intent intent = new Intent(getContext(), ChatroomActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        view.findViewById(R.id.iv_message).setOnClickListener(onClickListener);
        return view;
    }
}