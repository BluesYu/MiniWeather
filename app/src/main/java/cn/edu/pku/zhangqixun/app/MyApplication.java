package cn.edu.pku.zhangqixun.app;

import android.app.Application;
import android.util.Log;

/**
 * Created by T440P on 2017/10/25.
 */

public class MyApplication extends Application {
    private static final String TAG = "MyAPP";
    private static MyApplication mApplication;
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication = this;
    }
    public static MyApplication getInstance(){
        return mApplication;
    }
}
