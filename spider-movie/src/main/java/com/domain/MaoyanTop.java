package com.domain;

import org.jsoup.select.Elements;

/**
 * @Author: WK
 * @Data: 2019/7/15 13:30
 * @Description: com.dao
 */
public class MaoyanTop {
    private String titles;
    private String types;
    private String local;
    private String time;
    private String infos;
    private String hour;
    private String href;


    @Override
    public String toString() {
        return "MaoyanTop{" +
                "titles='" + titles + '\'' +
                ", types='" + types + '\'' +
                ", local='" + local + '\'' +
                ", time='" + time + '\'' +
                ", infos='" + infos + '\'' +
                ", hour='" + hour + '\'' +
                ", href='" + href + '\'' +
                '}';
    }

    public String getTitles() {
        return titles;
    }

    public void setTitles(String titles) {
        this.titles = titles;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInfos() {
        return infos;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
