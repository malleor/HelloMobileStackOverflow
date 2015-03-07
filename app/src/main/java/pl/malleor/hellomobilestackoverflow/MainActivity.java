package pl.malleor.hellomobilestackoverflow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "StackSearch";

    private EditText mQueryInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        mQueryInput = null;

        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        mQueryInput = (EditText)this.findViewById(R.id.query_input);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            return rootView;
        }
    }

    /// Receives a response from SO and triggers switching to the list view
    private class RequestClient implements StackRequest.Client
    {
        @Override
        public void onSuccess(JSONObject result) {
            try {
                JSONArray items = result.getJSONArray("items");
                int num_items = items.length();
                Log.d(TAG, String.format("Got %d results:", num_items));

                for(int i=0; i<items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String title = item.getString("title");

                    Log.d(TAG, String.format("#%d title: %s", i, title));
                }
            } catch (JSONException e) {
                onFailure(e.getMessage());
            }
        }

        @Override
        public void onFailure(String reason) {
            Log.e(TAG, String.format("request failed due to the following reason: '%s'", reason));
        }
    }

    /// The user hits 'Search'
    public void onSearchButton(View v)
    {
        // fetch the query from UI
        String query = mQueryInput.getText().toString();
        Log.d(TAG, String.format("the user needs to search for '%s'", query));

        // query StackOverflow
        // TODO: store the request to be able to cancel it (as soon as Cancel is implemented)
        new StackRequest(query, new RequestClient());
    }
}
