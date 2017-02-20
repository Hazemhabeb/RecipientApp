package appzone.com.recipientapp.Helper;


import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

public class PubNubManager {

    public final static String TAG = "PUBNUB";

    public static Pubnub startPubnub() {
        Log.d(TAG, "Initializing PubNub");
        return new Pubnub("pub-c-c799d7df-6867-487b-85e8-57b1a9a20894", "sub-c-f5bf4a9e-f290-11e6-bba1-0619f8945a4f");
    }

    public static void subscribe(Pubnub mPubnub, String channelName, Callback subscribeCallback) {
        // Subscribe to channel
        try {
            mPubnub.subscribe("location", subscribeCallback);
            Log.d(TAG, "Subscribed to Channel");
        } catch (PubnubException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void broadcastLocation(Pubnub pubnub, String channelName, double latitude,
                                         double longitude, double altitude) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", latitude);
            message.put("lng", longitude);
            message.put("alt", altitude);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        Log.d(TAG, "Sending JSON Message: " + message.toString());
        pubnub.publish("location", message, publishCallback);
    }

    public static Callback publishCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object response) {
            Log.d("PUBNUB", "Sent Message: " + response.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.d("PUBNUB", error.toString());
        }
    };
}
