package com.example.petcam.profile.vod;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petcam.R;
import com.example.petcam.profile.vod.VODAdapter;
import com.example.petcam.profile.vod.VODItem;

import java.util.ArrayList;

public class VODFragment extends Fragment {

    private RecyclerView rv_VOD;
    private ArrayList<VODItem> mDataList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vod, container, false);
        // Inflate the layout for this fragment

        mDataList = new ArrayList<>();
        VODItem VODItem = new VODItem("차오츄르먹방!!!",
                String.valueOf(System.currentTimeMillis()),
                "2",
                "",
                "코로나",
                "",
                R.drawable.ic_profile);
        mDataList.add(VODItem);

        rv_VOD = view.findViewById(R.id.rv_vod);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_VOD.setLayoutManager(linearLayoutManager); // LayoutManager 등록
        rv_VOD.setAdapter(new VODAdapter(mDataList, getContext()));

        return view;
    }
}