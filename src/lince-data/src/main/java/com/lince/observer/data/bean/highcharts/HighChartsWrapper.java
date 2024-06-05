package com.lince.observer.data.bean.highcharts;

import org.apache.commons.math3.util.Pair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * lince-scientific-base
 * .app.base.common
 * Created by Alberto Soto Fernandez in 11/04/2017.
 * Description:
 */
public class HighChartsWrapper {

    String title;
    String subtitle;
    List<HighChartsSerie> series = new ArrayList<>();
    List<HighChartsWrapper> drilldown = new ArrayList<>(); //PIE NAVIGATION CHART
    List<String> xSeriesLabels = new ArrayList<>();
    List<Pair<Integer, String>> ySeriesLabels = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /*public JSONObject getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title.put(TEXT,title);
    }

    public JSONObject getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.put(TEXT, subtitle);
    }*/

    public List<HighChartsWrapper> getDrilldown() {
        return drilldown;
    }

    public void setDrilldown(List<HighChartsWrapper> drilldown) {
        this.drilldown = drilldown;
    }

    public List<HighChartsSerie> getSeries() {
        return series;
    }

    public void setSeries(List<HighChartsSerie> series) {
        this.series = series;
    }

    public List<String> getxSeriesLabels() {
        return xSeriesLabels;
    }

    public void setxSeriesLabels(List<String> xSeriesLabels) {
        this.xSeriesLabels = xSeriesLabels;
    }

    public List<Pair<Integer, String>> getySeriesLabels() {
        return ySeriesLabels;
    }

    public void setySeriesLabels(List<Pair<Integer, String>> ySeriesLabels) {
        this.ySeriesLabels = ySeriesLabels;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (HighChartsSerie serie : getSeries()) {
            json.put("criteria", serie.getDataBean());
        }
        if (!getDrilldown().isEmpty()) {
            for (HighChartsWrapper wrap : getDrilldown()) {
                for (HighChartsSerie serie : wrap.getSeries()) {
                    json.put(serie.getName(), serie.getDataBean());
                }
            }
        }
        return json;
//
//        JSONObject json = new JSONObject();
//        json.put("title", title);
//        json.put("subtitle", subtitle);
//        JSONArray seriesArray = new JSONArray();
//        for (HighChartsSerie serie : series) {
//            JSONObject serieJson = new JSONObject();
//            serieJson.put("name", serie.getName());
//            serieJson.put("id", serie.getId());
//            serieJson.put("data", serie.getData());
//            serieJson.put("dataBean", serie.getDataBean());
//            serieJson.put("colorByPoint", serie.isColorByPoint());
//            seriesArray.put(serieJson);
//        }
//        json.put("series", seriesArray);
//        JSONArray drilldownArray = new JSONArray();
//        for (HighChartsWrapper drilldownItem : drilldown) {
//            drilldownArray.put(drilldownItem.toJson());
//        }
//        json.put("drilldown", drilldownArray);
//        json.put("xSeriesLabels", xSeriesLabels);
//        JSONArray ySeriesLabelsArray = new JSONArray();
//        for (Pair<Integer, String> ySeriesLabel : ySeriesLabels) {
//            JSONObject ySeriesLabelJson = new JSONObject();
//            ySeriesLabelJson.put("first", ySeriesLabel.getFirst());
//            ySeriesLabelJson.put("second", ySeriesLabel.getSecond());
//            ySeriesLabelsArray.put(ySeriesLabelJson);
//        }
//        json.put("ySeriesLabels", ySeriesLabelsArray);
//        return json;
    }
}
