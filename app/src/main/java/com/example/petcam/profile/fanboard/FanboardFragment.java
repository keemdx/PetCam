package com.example.petcam.profile.fanboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcam.R;

import java.util.ArrayList;

public class FanboardFragment extends Fragment {

    private RecyclerView rv_fanboard;
    private ArrayList<FanboardItem> mDataList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fanboard, container, false);
        // Inflate the layout for this fragments

        mDataList = new ArrayList<>();
        FanboardItem fanboardItem = new FanboardItem("이름", "텍스트", String.valueOf(System.currentTimeMillis()), "2", "", R.drawable.ic_gray_comment);
        mDataList.add(fanboardItem);
        FanboardItem fanboardItem2 = new FanboardItem("이름", "텍스트", String.valueOf(System.currentTimeMillis()), "2", "", R.drawable.ic_gray_comment);
        mDataList.add(fanboardItem2);
        FanboardItem fanboardItem3 = new FanboardItem("이름", "텍스트", String.valueOf(System.currentTimeMillis()), "2", "", R.drawable.ic_gray_comment);
        mDataList.add(fanboardItem3);
        FanboardItem fanboardItem4 = new FanboardItem("이름", "텍스트", String.valueOf(System.currentTimeMillis()), "2", "", R.drawable.ic_gray_comment);
        mDataList.add(fanboardItem4);

        rv_fanboard = view.findViewById(R.id.rv_fanboard);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_fanboard.setLayoutManager(linearLayoutManager); // LayoutManager 등록
        rv_fanboard.setAdapter(new FanboardAdapter(mDataList, getContext()));

        return view;
    }
}