package com.example.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class Country extends DataSupport {
    private int id;
    private String countryName;
    private String weatherID;
    private int cityID;

    public int getId() {
        return id;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getWeatherID() {
        return weatherID;
    }

    public int getCityID() {
        return cityID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setWeatherID(String weatherID) {
        this.weatherID = weatherID;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }
}
