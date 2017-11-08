package cn.edu.pku.zhangqixun.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.security.PublicKey;
import java.util.List;

import cn.edu.pku.zhangqixun.app.MyApplication;
import cn.edu.pku.zhangqixun.bean.City;

/**
 * Created by T440P on 2017/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackBtn;
    private ListView mList;
    private List<City> cityList
    private Myadapter myadapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        initViews();

    }
    private void initViews(){
        //为mBackBtn 设置监听事件
        mBackBtn = (ImageView) findViewById(R.id.title_back);

        mBackBtn.setOnClickListener(this);
        mClearEditText = (ClearEditText) findViewById(R.id.search_city);
        mList = (ListView) findViewById(R.id.title_list);
        MyApplication myApplication = (MyApplication) getApplication();
        cityList = myApplication.getCityList();
        for (City city:cityList){
            filterDataList.add(city);
        }
        myadapter = new Myadapter(SelectCity.this.cityList);
        mList.setAdapter(myadapter);
        mList.setOnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView,View view,int position,long l){
                City city = filterDateList.get(position);
                Intent i =new Intent();
                i putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,i);
                finish();
            }
        }

    }
    @Override
    public  void  onClick(View v){
            switch (v.getId()){
                case R.id.title_back:
                    Intent i = new Intent();
                    i.putExtra("cityCode", "101160101");
                    setResult(RESULT_OK, i);
                    finish();
                    break;
                default:
                    break;
        }
    }
}

