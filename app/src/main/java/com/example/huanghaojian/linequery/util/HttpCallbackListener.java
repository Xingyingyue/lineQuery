package com.example.huanghaojian.linequery.util;

/**
 * Created by huanghaojian on 16/10/24.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}

