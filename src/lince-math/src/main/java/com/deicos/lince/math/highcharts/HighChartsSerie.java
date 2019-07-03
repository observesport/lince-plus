package com.deicos.lince.math.highcharts;

import java.util.ArrayList;
import java.util.List;

/**
 * lince-scientific-base
 * com.deicos.lince.app.base.common
 * Created by Alberto Soto Fernandez in 11/04/2017.
 * Description:
 */
public class HighChartsSerie {
    String name = new String();
    String id;
    List<Double> data = new ArrayList<>();
    List<HighChartsSerieBean> dataBean = new ArrayList<>();
    boolean colorByPoint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }

    public List<HighChartsSerieBean> getDataBean() {
        return dataBean;
    }

    public void setDataBean(List<HighChartsSerieBean> dataBean) {
        this.dataBean = dataBean;
    }

    public boolean isColorByPoint() {
        return colorByPoint;
    }

    public void setColorByPoint(boolean colorByPoint) {
        this.colorByPoint = colorByPoint;
    }
}
