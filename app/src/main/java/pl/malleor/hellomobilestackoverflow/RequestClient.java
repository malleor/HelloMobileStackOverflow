package pl.malleor.hellomobilestackoverflow;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/// Receives a response from SO and triggers switching to the list view
public class RequestClient implements StackRequest.Client
{
    private final static String TAG = "RequestClient";

    private final MainActivity mActivity;

    RequestClient(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onSuccess(JSONObject result) {
        ArrayList<SearchResult> parsed_results = new ArrayList<SearchResult>();

        // traverse the JSON
        try {
            JSONArray items = result.getJSONArray("items");
            int num_items = items.length();
            Log.d(TAG, String.format("Got %d results:", num_items));

            for(int i=0; i<items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                SearchResult sr = new SearchResult(item);

                parsed_results.add(sr);
            }
        } catch (JSONException e) {
            onFailure(e.getMessage());
        }

        // debug log
        for(SearchResult sr : parsed_results) {
            Log.d(TAG, String.format("title: %s", sr.title));
            Log.d(TAG, String.format("author: %s", sr.user_name));
            Log.d(TAG, String.format("author img: %s", sr.owner_image_url));
            Log.d(TAG, String.format("answers: %d", sr.num_answers));
        }

        // pass the results to update the UI
        mActivity.displayResultsView(parsed_results);
    }

    @Override
    public void onFailure(String reason) {
        Log.e(TAG, String.format("request failed due to the following reason: '%s'", reason));
    }
}
