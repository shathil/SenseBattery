package ektara.com.sensebattery;

/**
 * Created by Mesbahul Islam on 1/15/2017.
 */

public class MeasurementItem {
    private String title;
    private String value;
    private String subTitle;

    public MeasurementItem(String title, String value, String subTitle) {
        this.title = title;
        this.value = value;
        this.subTitle = subTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
}
