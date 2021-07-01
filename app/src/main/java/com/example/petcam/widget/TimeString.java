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
            msg = "now";
        } else if ((diffTime /= TimeString.SEC) < TimeString.MIN) {
            if (diffTime > 1) {
                msg = diffTime + " minutes ago";
            } else {
                msg = diffTime + " minute ago";
            }
        } else if ((diffTime /= TimeString.MIN) < TimeString.HOUR) {
            if (diffTime > 1) {
                msg = (diffTime) + " hours ago";
            } else {
                msg = (diffTime) + " hour ago";
            }
        } else if ((diffTime /= TimeString.HOUR) < TimeString.DAY) {
            if (diffTime > 1) {
                msg = (diffTime) + " days ago";
            } else {
                msg = (diffTime) + " day ago";
            }
        } else if ((diffTime /= TimeString.DAY) < TimeString.MONTH) {
            if (diffTime > 1) {
                msg = (diffTime) + " months ago";
            } else {
                msg = (diffTime) + " month ago";
            }
        } else {
            if (diffTime > 1) {
                msg = (diffTime) + " years ago";
            } else {
                msg = (diffTime) + " year ago";
            }
        }
        return msg;
    }
}