package cn.edu.pku.zhangqixun.bean;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by T440P on 2017/10/13.
 * function：//公共类用于保存共有今日天气数据，
 * 存入（setXXX）和读取（getXXX）都通过面向对象的方法来处理
 */

public class TodayWeather {
    private SharedPreferences sp; //实例化SharedPreference对象，用于存储联网解析之后获取的所有数据

    private String city = "无", updatetime = "未", wendu = "无信息", shidu = "无信息",
            fengxiang = "无风向信息", fengli = "无信息", date = "无日期信息",
            high = "无", low = "无", type = "该地区没有天气信息";
    //由于部分地区不返回PM2.5数值与质量，返回的是null值，需要设初值防止空指针异常
    private String pm25 = "该地区", quality = "没有PM2.5信息";


    //通过工具提供的功能生成get方法。
    //通过工具提供的功能生成set方法。
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getWendu() {
        return wendu;
    }

    public void setWendu(String wendu) {
        this.wendu = wendu;
    }

    public String getShidu() {
        return shidu;
    }

    public void setShidu(String shidu) {
        this.shidu = shidu;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getFengxiang() {
        return fengxiang;
    }

    public void setFengxiang(String fengxiang) {
        this.fengxiang = fengxiang;
    }

    public String getFengli() {
        return fengli;
    }

    public void setFengli(String fengli) {
        this.fengli = fengli;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    //用于存储联网解析之后获取的所有数据↓
    public void saveAllData(Activity activity){
        sp = activity.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("CITY", city);
        editor.putString("UPDATETIME", updatetime);
        editor.putString("WENDU", wendu);
        editor.putString("SHIDU", shidu);
        editor.putString("PM25", pm25);
        editor.putString("QUALITY", quality);
        editor.putString("FENGLI", fengli);
        editor.putString("DATE", date);
        editor.putString("HIGH", high);
        editor.putString("LOW", low);
        editor.putString("TYPE", type);
        editor.commit();
    }

    //通过工具提供的功能生成toString方法。
    @Override
    public String toString() {
        return "TodayWeather{" +
                "city='" + city + '\'' +
                ", updatetime='" + updatetime + '\'' +
                ", wendu='" + wendu + '\'' +
                ", shidu='" + shidu + '\'' +
                ", pm25='" + pm25 + '\'' +
                ", quality='" + quality + '\'' +
                ", fengxiang='" + fengxiang + '\'' +
                ", fengli='" + fengli + '\'' +
                ", date='" + date + '\'' +
                ", high='" + high + '\'' +
                ", low='" + low + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
