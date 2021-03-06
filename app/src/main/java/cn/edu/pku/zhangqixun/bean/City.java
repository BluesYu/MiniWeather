package cn.edu.pku.zhangqixun.bean;

/**
 * Created by T440P on 2017/10/25.
 * function：城市信息。
 * 公共类用于保存共有城市列表数据，
 * 存入（setXXX）和读取（getXXX）都通过面向对象的方法来处理。
 */

public class City {
    private String province;
    private String city;
    private String number;
    private String firstPY;
    private String allPY;
    private String allFristPY;

    public City(String province, String city, String number, String
            firstPY, String allPY, String allFristPY) {
        this.province = province;
        this.city = city;
        this.number = number;
        this.firstPY = firstPY;
        this.allPY = allPY;
        this.allFristPY = allFristPY;
    }

    public String getProvince() {
        return province;
    }
    public String getCity() {
        return city;
    }
    public String getNumber() {
        return number;
    }
    public String getFirstPY() {
        return firstPY;
    }
    public String getAllPY() {
        return allPY;
    }
    public String getAllFristPY() {
        return allFristPY;
    }
}
