package cn.edu.pku.zhangqixun.CityDB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zhangqixun.bean.City;

/**
 * Created by T440P on 2017/10/25.
 * function：城市数据库函数
 */

    public class CityDB {//这个类用于获取数据库中的城市列表所有信息
        public static final String CITY_DB_NAME = "city.db";
        private static final String CITY_TABLE_NAME = "city";
        private SQLiteDatabase db;

        public CityDB(Context context, String path) {
            //打开指定路径数据库
            db = context.openOrCreateDatabase(path, Context.MODE_PRIVATE, null);
        }

        public List<City> getAllCity() {
            //获取数据库中的所有城市信息，并返回一张列表
            List<City> list = new ArrayList<City>();
            //用游标循环遍历取数据库中的数据，这里加上order by firstpy才能让List按第一个字拼音的首字母排序，否则乱序
            Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME + " order by firstpy",null);
            while (c.moveToNext()) {
                String province = c.getString(c.getColumnIndex("province"));
                String city = c.getString(c.getColumnIndex("city"));
                String number = c.getString(c.getColumnIndex("number"));
                String allPY = c.getString(c.getColumnIndex("allpy"));
                String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
                String firstPY = c.getString(c.getColumnIndex("firstpy"));
                City item = new City(province, city, number, firstPY, allPY,allFirstPY);
                list.add(item);
            }
            return list;
        }
    }
