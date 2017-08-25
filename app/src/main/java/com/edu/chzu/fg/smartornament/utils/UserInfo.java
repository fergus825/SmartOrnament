package com.edu.chzu.fg.smartornament.utils;

import cn.bmob.v3.BmobObject;

/**
 * Created by FG on 2016/11/4.
 */

public class UserInfo extends BmobObject {
    public String name;
    public String psd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPsd() {
        return psd;
    }

    public void setPsd(String psd) {
        this.psd = psd;
    }
}
