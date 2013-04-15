package com.chridam.loadshedder;

/**
 * Created with IntelliJ IDEA.
 * User: Windows
 * Date: 4/15/13
 * Time: 9:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Location {
    String code = null;
    String name = null;
    String region = null;

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
}
