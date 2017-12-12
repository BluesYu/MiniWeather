package cn.edu.pku.zhangqixun.miniweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import cn.edu.pku.zhangqixun.app.MyApplication;
import cn.edu.pku.zhangqixun.bean.City;

/**
 * Created by T440P on 2017/10/18.
 * function：选择城市。
 */
public class SelectCity extends AppCompatActivity implements View.OnClickListener{

    private TextView selectTitleTv, mDialogLetterTv;
    private ClearEditText mClearEditText;
    private SideBar mSideBar;
    private SortAdapter mAdapter;
    private ListView mCityListLV;
    private List<City> mCityList;
    private List<City> filterDataList = new ArrayList<City>();
    private SharedPreferences sp; //实例化SharedPreference对象，用于读取更新SelectCityActivity的“当前城市：”标题

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        initViews();  //初始化控件显示
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:  //点击左上角返回则关掉选择城市列表页面返回主页面
//                Intent i = new Intent();
//                i.putExtra("cityCode", "101160101");//测试用的数据
//                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 初始化控件内容函数，读取城市列表并显示↓
     * *@param void
     * return void
     */
    private void initViews() {
        selectTitleTv = (TextView) findViewById(R.id.title_name);
        sp = getSharedPreferences("config", MODE_PRIVATE);  //获得sp实例对象
        String selectedcityname = sp.getString("CITY", "N/A");
        selectTitleTv.setText("当前城市：" + selectedcityname);

        //为mBackBtn 设置监听事件
        ImageView mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        mClearEditText = (ClearEditText) findViewById(R.id.search_city);
        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则显示为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mSideBar = (SideBar) findViewById(R.id.listview_sidebar);
        mDialogLetterTv = (TextView) findViewById(R.id.listview_dialog);
        mSideBar.setTextView(mDialogLetterTv);
//设置右侧触摸监听
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mCityListLV.setSelection(position);
                }
            }
        });

        MyApplication myApplication = (MyApplication) getApplication();
        mCityList = myApplication.getCityList();   //获取到在MyApplication中从数据库取得的城市列表
        //如果要更改列表每一项的显示内容（省份城市或是什么的），到SortAdapter类中找到对应的TextView修改
        for (City city : mCityList) {
            filterDataList.add(city);
        }
        mCityListLV = (ListView) findViewById(R.id.selectcity_listview);   //绑定ListView控件，绑定数据与ListView的适配器
        mAdapter = new SortAdapter(SelectCity.this, filterDataList);
        mCityListLV.setAdapter(mAdapter);

//添加ListView项的点击事件并绑定
        mCityListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent iNewCD = new Intent();
                //通过点击事件的位置判断用户点击哪个城市，并将城市代码存入意图对象
                iNewCD.putExtra("cityCode", filterDataList.get(position).getNumber());
                setResult(RESULT_OK, iNewCD);
                finish();
            }
        });
    }

    private void filterData(String filterStr) {
        Log.d("myFilter", filterStr);
        String upperFilterStr = filterStr.toUpperCase();

        if (TextUtils.isEmpty(filterStr)) {
            filterDataList.clear();
            for (City city : mCityList) {
                filterDataList.add(city);
            }
        } else {
            filterDataList.clear();
            for (City city : mCityList) {
                // 匹配中文城市名、全拼首字母和第一个字拼音首字母
                if (city.getCity().indexOf(upperFilterStr) > -1
                        || city.getFirstPY().indexOf(upperFilterStr) > -1
                        || city.getAllFristPY().indexOf(upperFilterStr) > -1) {
                    filterDataList.add(city);
                }
            }
        }
        mAdapter.updateListView(filterDataList);
        if (filterDataList.isEmpty()) {  //如果关键字找到的列表为空（即找不到对应项目）抖动ClearEditText并给出提示信息
            mClearEditText.setStartShakeAnimation();
            Toast.makeText(SelectCity.this, "找不到对应城市哟！请检查搜索词！" + "\n" + "支持中文/拼音首字母的模糊搜索~~~", Toast.LENGTH_SHORT).show();
        }
    }
}


