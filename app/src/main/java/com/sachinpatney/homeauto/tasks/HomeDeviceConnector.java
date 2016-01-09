package com.sachinpatney.homeauto.tasks;

import android.util.Log;
import com.sachinpatney.homeauto.HomeTempStorage;
import org.json.JSONObject;

public class HomeDeviceConnector {
    public static void runAsync(HomeDevice device, TaskHandler<JSONObject> handler){
        JSONObject jsonObject = new JSONObject();
        try {
            if(device.data!=null) {
                jsonObject.put("data", device.data);
            }
            jsonObject.put("id", device.id);
            new PostMessageTask(handler).execute(HomeTempStorage.getFlaskUrl()
                            + "/device", jsonObject.toString());
        }catch (Exception ex) {
            Log.e("ErrorBack", ex.getMessage());
        }
    }
}