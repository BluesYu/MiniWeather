package cn.edu.pku.zhangqixun.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zhangqixun.CityDB.CityDB;
import cn.edu.pku.zhangqixun.bean.City;

/**
 * Created by T440P on 2017/10/25.
 * function：控制线程，读取数据库。
 */

public class MyApplication extends Application {
    //系统组件之一，生命周期即整个程序的生命周期，onCreate方法在Activity的onCreate之前
    private static final String TAG = "MyAPP";
    private static MyApplication mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication = this;
        mCityDB = openCityDB();//打开数据库
        initCityList();      //初始化城市列表
    }
    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            // 读取数据库属于耗时操作，需要在非主线程中完成
            @Override
            public void run() {
//TODO Auto-generated method stub
                prepareCityList();//准备城市列表
                Log.d(TAG,"citylist ready");
            }

        }).start();
    }
    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();//调用CityDB类，获取城市列表所有信息
//        int i=0;
//        for (City city : mCityList) { //遍历城市列表打印看看是否获取到数据库中的城市名字和代码
//            i++;
//            String cityName = city.getCity();
//            String cityCode = city.getNumber();
//            Log.d(TAG,cityCode+":"+cityName);
//        }
//        Log.d(TAG,"i="+i);
        Log.d(TAG,"prepareCityList ready");
        return true;
    }
    public List<City> getCityList() {//获取citylist
        return mCityList;
    }

    public static MyApplication geiInstance(){
        return mApplication;
    }

    private CityDB openCityDB() {
        //打开指定路径下的数据库，如果路径不存在则从assets中读取数据库并写入指定路径
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath
                ()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }
}


