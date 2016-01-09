package com.sachinpatney.homeauto.tasks;

public class HomeDevice {
    public final String id;
    public final String data;

    public HomeDevice(String id){
        this(id, null);
    }
    public HomeDevice(String id, String data){
        this.id = id;
        this.data = data;
    }
}
