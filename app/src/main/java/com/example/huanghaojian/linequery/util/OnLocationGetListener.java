package com.example.huanghaojian.linequery.util;

import com.example.huanghaojian.linequery.model.Address;

/**
 * Created by huanghaojian on 16/12/19.
 */

public interface OnLocationGetListener {

    public void onLocationGet(Address entity);

    public void onRegecodeGet(Address entity);
}
