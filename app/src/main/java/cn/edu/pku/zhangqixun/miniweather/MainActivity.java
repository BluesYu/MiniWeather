package cn.edu.pku.zhangqixun.miniweather;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.edu.pku.zhangqixun.bean.TodayWeather;
import cn.edu.pku.zhangqixun.util.NetUtil;

/**
 * Created by yxx on 2017/10/6.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private SharedPreferences sp; //实例化SharedPreference对象，用于存取天气数据
    private static final int UPDATE_TODAY_WEATHER = 1;
    private TextView cityTv, timeTv,temperatureTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
             climateTv, windTv, city_name_Tv,Current_temperatureTv;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private ProgressBar mUpdatePgb;
    private ImageView weatherImg, pmImg;

    private TextView yesterdayDateTv, yesterdayHighTv, yesterdayLowTv, yesterdayTypeTv,
            todayDateTv, todayHighTv, todayLowTv, todayTypeTv,
            tomorrowDateTv, tomorrowHighTv, tomorrowLowTv, tomorrowTypeTv,
            future2DateTv, future2HighTv, future2LowTv, future2TypeTv,
            future3DateTv, future3HighTv, future3LowTv, future3TypeTv,
            future4DateTv, future4HighTv, future4LowTv, future4TypeTv;
    private ImageView yesterdayImg, todayImg, tomorrowImg, future2Img, future3Img, future4Img;
    private List<View> futureViewList = new ArrayList<View>();  //六日天气信息数据源
    private List<String> futureTitles = new ArrayList<String>();      //六日天气标题数据源
    private ViewPager futureViewPager;
    private PagerAdapter futurePagAdapter;
    private LayoutInflater futureInflater;
    //    private PagerTabStrip futurePagTitle;       //ViewPager的标题
    private ImageView[] futureDots;
    private int[] futureIndicator = {R.id.future_indicator_first, R.id.future_indicator_second};

    Calendar cDate = Calendar.getInstance();        //获取月份加入日期数据中显示
    String mMonth = String.valueOf(cDate.get(Calendar.MONTH) + 1);

    private Handler mHandler = new Handler(){   //线程间消息处理机制，MessageQueue是一个存放消息对象的队列
        public void handleMessage(Message msg){
            switch (msg.what){      //消息对象的属性.what只能放数字，用作判断是哪个非主线程传来的消息
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj); //在主线程中更新控件信息
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //调用父类的onCreate构造函数，savedInstanceState是保存当前Activity的状态信息
        super.onCreate(savedInstanceState);
        //将主Activity与资源文件weather_info.xml布局文件绑定
        setContentView(R.layout.weather_info);

        sp = getSharedPreferences("config", MODE_PRIVATE);  //获得sp实例对象
        testOnlineAndGet(null);
        initView();     //初始化控件显示
       mUpdateBtn.setOnClickListener(this);     //为刷新的图片控件设置监听事件
       mCitySelect.setOnClickListener(this);   //为显示城市列表的图片控件设置监听事件
    }

    @Override
    public void onClick(View view) //单击事件
    {
//如果点击到刷新，取出保存的城市代码（默认北京）去获取网络的天气信息
        if (view.getId() == R.id.title_update_btn){
            mUpdatePgb.setVisibility(View.VISIBLE);
            view.setVisibility(View.INVISIBLE);
            String cityCode = sp.getString("MAIN_CITY_CODE","101010100");
            Log.d("myWeather",cityCode);
            //通过SharedPreferences读取城市id，如果没有定义则缺省为101010100（北京城市ID
            testOnlineAndGet(cityCode);
        }

        if(view.getId()==R.id.title_city_manager){//如果点击到显示城市列表，则弹出城市列表界面到前端
            Intent i=new Intent(this,SelectCity.class);
            Log.d("myWeather", "单击");
            startActivityForResult(i,1);//接收返回的数据。
        }

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            //queryWeatherCode(cityCode);
        }else
        {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }
    }
    /**
     * 初始化控件内容函数，可以读取之前保存的天气信息↓
     * *@param void
     */
    void initView(){
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);   //绑定刷新的图片控件
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager); //绑定显示城市列表的图片控件
        mUpdatePgb = (ProgressBar) findViewById(R.id.title_update_progress);    //绑定更新天气才显示的ProgressBar控件

        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        Current_temperatureTv = (TextView) findViewById(R.id.Current_temperature);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);


        String cityname = sp.getString("CITY", "N/A");
        String updatetime = sp.getString("UPDATETIME", "N/A");
        String tempernow = sp.getString("WENDU", "N/A");
        String humidity = sp.getString("SHIDU", "N/A");
        String pmdata = sp.getString("PM25", "N/A");
        String pmquality = sp.getString("QUALITY", "N/A");
        String week = sp.getString("DATE", "N/A");
        String temperaturehigh = sp.getString("HIGH", "N/A");
        String temperaturelow = sp.getString("LOW", "N/A");
        String climate = sp.getString("TYPE", "N/A");
        String wind = sp.getString("FENGLI", "N/A");
        Log.d("myWeather", "从SP中读到的天气数据：" + cityname + ", " + updatetime + ", "  + tempernow + ", "  +
                humidity + ", "  + pmdata + ", "  + pmquality + ", "  + week + ", "  + temperaturehigh + ", "  +
                temperaturelow + ", "  + climate + ", "  + wind);

        city_name_Tv.setText(cityname + "天气");
        cityTv.setText(cityname);
        timeTv.setText(updatetime + "发布");
        Current_temperatureTv.setText("温度：" + tempernow + "℃");
        humidityTv.setText("湿度：" + humidity);
        pmDataTv.setText(pmdata);
        pmQualityTv.setText(pmquality);
        weekTv.setText(mMonth + "月" + week);
        temperatureTv.setText(temperaturehigh + "~" + temperaturelow);
        climateTv.setText(climate);
        windTv.setText("风力：" + wind);

        //绑定六日天气信息的ViewPager
        futureViewPager = (ViewPager) findViewById(R.id.future_viewpager);
        futureInflater = LayoutInflater.from(this);
        View future11Days = futureInflater.inflate(R.layout.future__1_1_days, null);    //获取未来-1~1天的view
        View future24Days = futureInflater.inflate(R.layout.future_2_4_days, null);     //获取未来2~4天的view
        futureViewList.add(future11Days);       //将上面2个View加入到集合
        futureViewList.add(future24Days);
        futureTitles.add("昨日今明天气");
        futureTitles.add("未来四日天气");
        futurePagAdapter = new ViewPagAdapter(futureViewList, futureTitles);      //实例化适配器
        futureViewPager.setAdapter(futurePagAdapter);       //设置适配器

        yesterdayDateTv = (TextView) futureViewList.get(0).findViewById(R.id.yesterday_date);//ViewPager里的控件必须通过View的List的get(index)先找到再find绑定
        yesterdayHighTv = (TextView) futureViewList.get(0).findViewById(R.id.yesterday_high);
        yesterdayLowTv = (TextView) futureViewList.get(0).findViewById(R.id.yesterday_low);
        yesterdayTypeTv = (TextView) futureViewList.get(0).findViewById(R.id.yesterday_type);
        todayDateTv = (TextView) futureViewList.get(0).findViewById(R.id.future_zero_date);
        todayHighTv = (TextView) futureViewList.get(0).findViewById(R.id.future_zero_high);
        todayLowTv = (TextView) futureViewList.get(0).findViewById(R.id.future_zero_low);
        todayTypeTv = (TextView) futureViewList.get(0).findViewById(R.id.future_zero_type);
        tomorrowDateTv = (TextView) futureViewList.get(0).findViewById(R.id.future_one_date);
        tomorrowHighTv = (TextView) futureViewList.get(0).findViewById(R.id.future_one_high);
        tomorrowLowTv = (TextView) futureViewList.get(0).findViewById(R.id.future_one_low);
        tomorrowTypeTv = (TextView) futureViewList.get(0).findViewById(R.id.future_one_type);
        future2DateTv = (TextView) futureViewList.get(1).findViewById(R.id.future_two_date);
        future2HighTv = (TextView) futureViewList.get(1).findViewById(R.id.future_two_high);
        future2LowTv = (TextView) futureViewList.get(1).findViewById(R.id.future_two_low);
        future2TypeTv = (TextView) futureViewList.get(1).findViewById(R.id.future_two_type);
        future3DateTv = (TextView) futureViewList.get(1).findViewById(R.id.future_three_date);
        future3HighTv = (TextView) futureViewList.get(1).findViewById(R.id.future_three_high);
        future3LowTv = (TextView) futureViewList.get(1).findViewById(R.id.future_three_low);
        future3TypeTv = (TextView) futureViewList.get(1).findViewById(R.id.future_three_type);
        future4DateTv = (TextView) futureViewList.get(1).findViewById(R.id.future_four_date);
        future4HighTv = (TextView) futureViewList.get(1).findViewById(R.id.future_four_high);
        future4LowTv = (TextView) futureViewList.get(1).findViewById(R.id.future_four_low);
        future4TypeTv = (TextView) futureViewList.get(1).findViewById(R.id.future_four_type);
        yesterdayImg = (ImageView) futureViewList.get(0).findViewById(R.id.yesterday_img);
        todayImg = (ImageView) futureViewList.get(0).findViewById(R.id.future_zero_img);
        tomorrowImg = (ImageView) futureViewList.get(0).findViewById(R.id.future_one_img);
        future2Img = (ImageView) futureViewList.get(1).findViewById(R.id.future_two_img);
        future3Img = (ImageView) futureViewList.get(1).findViewById(R.id.future_three_img);
        future4Img = (ImageView) futureViewList.get(1).findViewById(R.id.future_four_img);

        String yesterdayDate = sp.getString("YESDATE", "N/A");
        String yesterdayHigh = sp.getString("YESHIGH", "N/A");
        String yesterdayLow = sp.getString("YESLOW", "N/A");
        String yesterdayType = sp.getString("YESTYPE", "N/A");
        String tomorrowDate = sp.getString("TOMDATE", "N/A");
        String tomorrowHigh = sp.getString("TOMHIGH", "N/A");
        String tomorrowLow = sp.getString("TOMLOW", "N/A");
        String tomorrowType = sp.getString("TOMTYPE", "N/A");
        String future2Date = sp.getString("FU2DATE", "N/A");
        String future2High = sp.getString("FU2HIGH", "N/A");
        String future2Low = sp.getString("FU2LOW", "N/A");
        String future2Type = sp.getString("FU2TYPE", "N/A");
        String future3Date = sp.getString("FU3DATE", "N/A");
        String future3High = sp.getString("FU3HIGH", "N/A");
        String future3Low = sp.getString("FU3LOW", "N/A");
        String future3Type = sp.getString("FU3TYPE", "N/A");
        String future4Date = sp.getString("FU4DATE", "N/A");
        String future4High = sp.getString("FU4HIGH", "N/A");
        String future4Low = sp.getString("FU4LOW", "N/A");
        String future4Type = sp.getString("FU4TYPE", "N/A");

        yesterdayDateTv.setText(yesterdayDate);
        yesterdayHighTv.setText(yesterdayHigh);
        yesterdayLowTv.setText(yesterdayLow);
        yesterdayTypeTv.setText(yesterdayType);
        todayDateTv.setText(week);
        todayHighTv.setText(temperaturehigh);
        todayLowTv.setText(temperaturelow);
        todayTypeTv.setText(climate);
        tomorrowDateTv.setText(tomorrowDate);
        tomorrowHighTv.setText(tomorrowHigh);
        tomorrowLowTv.setText(tomorrowLow);
        tomorrowTypeTv.setText(tomorrowType);
        future2DateTv.setText(future2Date);
        future2HighTv.setText(future2High);
        future2LowTv.setText(future2Low);
        future2TypeTv.setText(future2Type);
        future3DateTv.setText(future3Date);
        future3HighTv.setText(future3High);
        future3LowTv.setText(future3Low);
        future3TypeTv.setText(future3Type);
        future4DateTv.setText(future4Date);
        future4HighTv.setText(future4High);
        future4LowTv.setText(future4Low);
        future4TypeTv.setText(future4Type);

        updateWeatherImg(climate, weatherImg);
        updateWeatherImg(yesterdayType, yesterdayImg);
        updateWeatherImg(climate, todayImg);
        updateWeatherImg(tomorrowType, tomorrowImg);
        updateWeatherImg(future2Type, future2Img);
        updateWeatherImg(future3Type, future3Img);
        updateWeatherImg(future4Type, future4Img);

        futureDots = new ImageView[futureViewList.size()];
        for (int i=0; i < futureViewList.size(); i++){
            futureDots[i] = (ImageView) findViewById(futureIndicator[i]);
        }
        futureViewPager.setOnPageChangeListener(this);
    }//初始化显示

    /**
     * 用于接收SelectCity Activity返回的数据，重写的Activity的onActivityResult方法↓
     * @param requestCode, resultCode, Intent data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");//通过意图对象获取到SelectCity传来的新的城市代码
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            testOnlineAndGet(newCityCode);      //根据新的城市代码刷新天气信息
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("MAIN_CITY_CODE", newCityCode);    //保存新的城市代码
            editor.commit();
        }
    }
    /**
     *function:检测网络连接状态并弹窗提示，若接入Internet则通过HTTP的GET请求获取天气信息↓
     * @param cityCode
     */
    private void testOnlineAndGet(String cityCode){
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("myWeather", "网络OK！");
            if (cityCode != null){
                if(NetUtil.getNetworkState(this) == NetUtil.NETWORN_MOBILE)
                    Toast.makeText(MainActivity.this, "使用移动网络为您更新数据", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "使用WiFi为您更新数据", Toast.LENGTH_SHORT).show();
                queryWeatherCode(cityCode);
            }
            else
                Toast.makeText(MainActivity.this, "已取出上次保存的天气！" + "\n" + "现在可点击右上角更新！", Toast.LENGTH_SHORT).show();
        }
        else{
            Log.d("myWeather", "网络Offline！");
            Toast.makeText(MainActivity.this, "亲，连不上网络哟~请检查网络设置~" ,Toast.LENGTH_LONG).show();
        }
    }
    /**
     * function:获取网络（天气）数据函数↓
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey="+ cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            //联网操作属于耗时操作，在非主线程里完成
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather = null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    if (con.getResponseCode() == 200) {   //状态码200为正常
                        InputStream is = con.getInputStream(); //打开输入流读取数据
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            response.append(str);
                            Log.d("myWeather", str);
                        }
                        String responseStr = response.toString();
                        Log.d("myWeather", responseStr);

                        todayWeather = parseXML(responseStr);//解析获取到的数据，保存到todayWeather的公有属性中
                        if (todayWeather != null) {
                            Log.d("myWeather", todayWeather.toString());

                            Message msg = new Message(); //在非主线程中获取到的天气信息数据传递给主线程更新UI控件
                            msg.what = UPDATE_TODAY_WEATHER;
                            msg.obj = todayWeather;
                            mHandler.sendMessage(msg);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }
    /**
     * function:XML解析函数↓
     * @param xmldata
     */
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0; //FLAG判断来精确获取到今日天气
        int yestodayTypeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML开始");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }
                            //下面是解析昨日未来天气信息
                            else if (xmlPullParser.getName().equals("date_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayDate(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("high_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayHigh(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("low_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayLow(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("type_1") && yestodayTypeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setYesterdayType(xmlPullParser.getText());
                                yestodayTypeCount++;
                            }           //下面是解析明日未来天气信息
                            else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTomorrowDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTomorrowHigh(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTomorrowLow(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTomorrowType(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }           //下面是解析后天（Future2）未来天气信息
                            else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture2Date(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture2High(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture2Low(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture2Type(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 5) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }          //下面是解析未来两日（大后天Future3）未来天气信息
                            else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture3Date(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture3High(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture3Low(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 6) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture3Type(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 7) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }          //下面是解析未来三日（大大后天Future4）未来天气信息
                            else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture4Date(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture4High(xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture4Low(xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 8) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFuture4Type(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 9) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
            todayWeather.saveAllData(this);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }
    /**
     * function:updateTodayWeather函数用于更新UI中的控件↓
     * *@object TodayWeather
     */
    void updateTodayWeather(TodayWeather todayWeather){
        int pm25State= 0;
        String weatherType = todayWeather.getType();
        if (todayWeather.getPm25().equals("该地区")){  //用于判断是否获取到了PM2.5数值与质量，没有判断会出现异常
            Toast.makeText(MainActivity.this, "该地区无法获取到PM2.5及相关信息！", Toast.LENGTH_SHORT).show();
        }
        else{
            pm25State = Integer.parseInt(todayWeather.getPm25());
        }

        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        Current_temperatureTv.setText("温度：" + todayWeather.getWendu() + "℃");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(mMonth + "月" + todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(weatherType);
        windTv.setText("风力：" + todayWeather.getFengli());
        yesterdayDateTv.setText(todayWeather.getYesterdayDate());
        yesterdayHighTv.setText(todayWeather.getYesterdayHigh());
        yesterdayLowTv.setText(todayWeather.getYesterdayLow());
        yesterdayTypeTv.setText(todayWeather.getYesterdayType());
        todayDateTv.setText(todayWeather.getDate());
        todayHighTv.setText(todayWeather.getHigh());
        todayLowTv.setText(todayWeather.getLow());
        todayTypeTv.setText(weatherType);
        tomorrowDateTv.setText(todayWeather.getTomorrowDate());
        tomorrowHighTv.setText(todayWeather.getTomorrowHigh());
        tomorrowLowTv.setText(todayWeather.getTomorrowLow());
        tomorrowTypeTv.setText(todayWeather.getTomorrowType());
        future2DateTv.setText(todayWeather.getFuture2Date());
        future2HighTv.setText(todayWeather.getFuture2High());
        future2LowTv.setText(todayWeather.getFuture2Low());
        future2TypeTv.setText(todayWeather.getFuture2Type());
        future3DateTv.setText(todayWeather.getFuture3Date());
        future3HighTv.setText(todayWeather.getFuture3High());
        future3LowTv.setText(todayWeather.getFuture3Low());
        future3TypeTv.setText(todayWeather.getFuture3Type());
        future4DateTv.setText(todayWeather.getFuture4Date());
        future4HighTv.setText(todayWeather.getFuture4High());
        future4LowTv.setText(todayWeather.getFuture4Low());
        future4TypeTv.setText(todayWeather.getFuture4Type());

        updateWeatherImg(weatherType, weatherImg);
        updateWeatherImg(todayWeather.getYesterdayType(), yesterdayImg);
        updateWeatherImg(weatherType, todayImg);
        updateWeatherImg(todayWeather.getTomorrowType(), tomorrowImg);
        updateWeatherImg(todayWeather.getFuture2Type(), future2Img);
        updateWeatherImg(todayWeather.getFuture3Type(), future3Img);
        updateWeatherImg(todayWeather.getFuture4Type(), future4Img);

        if (pm25State >= 0 & pm25State <= 50)
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        else if (pm25State >= 51 & pm25State <= 100)
            pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
        else if (pm25State >= 101 & pm25State <= 150)
            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
        else if (pm25State >= 151 & pm25State <= 200)
            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
        else if (pm25State >= 201 & pm25State <= 300)
            pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
        else if (pm25State >= 301)
            pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        else
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);

        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdatePgb.setVisibility(View.GONE);

        Toast.makeText(MainActivity.this, "天气已更新成功！", Toast.LENGTH_SHORT).show();
    }
    /**
     * updateWeatherImg函数用于更新几个控件中的天气图片↓
     * *@param void
     */
    void updateWeatherImg(String typeForWeather ,ImageView imgForWeather){
        switch (typeForWeather){
            case "晴":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "暴雪":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "沙尘暴":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨夹雪":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
            default:
                imgForWeather.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < futureIndicator.length; i++){
            if (i == position){
                futureDots[i].setImageResource(R.drawable.page_indicator_focused);
            }
            else {
                futureDots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }
}