package com.example.petcam.widget;

public class TimeString {
    /** 몇분전, 방금 전, */
    // 게시글 시간을 불러와서 현재 시간이랑 비교
    // 분,시,일,월,년 계산 후 String을 반환한다.

    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

    public static String formatTimeString(long regTime) {

        // 현재 시간
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - regTime) / 1000;
        String msg = null;

        if (diffTime < TimeString.SEC) {
            msg = "방금";
        } else if ((diffTime /= TimeString.SEC) < TimeString.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TimeString.MIN) < TimeString.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TimeString.HOUR) < TimeString.DAY) {
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= TimeString.DAY) < TimeString.MONTH) {
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }
        return msg;
    }
}