package pl.malleor.hellomobilestackoverflow;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "StackSearch";

    private EditText mQueryInput = null;
    private Fragment mVisibleFragment = null;

    private ArrayList<SearchResult> mSearchResults = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            displaySearchView();
        }
    }

    private void displaySearchView() {
        // change the view
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        if(mVisibleFragment != null)
            trans.remove(mVisibleFragment);
        trans.add(R.id.container, mVisibleFragment = new SearchFragment()).commit();
    }

    private void displayResultsView() {
        // change the view
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        if(mVisibleFragment != null)
            trans.remove(mVisibleFragment);
        trans.add(R.id.container, mVisibleFragment = new OverviewFragment()).commit();
        getSupportFragmentManager().executePendingTransactions();

        // update list items
        updateOverview();
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

    public static class SearchFragment extends Fragment {

        public SearchFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            return rootView;
        }
    }

    public static class OverviewFragment extends ListFragment {

        public OverviewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
            return rootView;
        }
    }

    /// Receives a response from SO and triggers switching to the list view
    private class RequestClient implements StackRequest.Client
    {
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
                    SearchResult sr = new SearchResult();

                    sr.title = item.getString("title");
                    sr.num_answers = item.getInt("answer_count");

                    JSONObject owner = item.getJSONObject("owner");
                    sr.user_name = owner.getString("display_name");
                    sr.owner_image_url = owner.getString("profile_image");

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
            mSearchResults = parsed_results;
            displayResultsView();
        }

        @Override
        public void onFailure(String reason) {
            Log.e(TAG, String.format("request failed due to the following reason: '%s'", reason));
        }
    }

    public void onA(View v) {
        updateOverview();
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

    // Custom adapter for binding search results to their view
    //
    // Origin: http://stackoverflow.com/a/11282200/154970
    //
    private class SearchResultAdapter extends ArrayAdapter<SearchResult> {
        public SearchResultAdapter() {
            super(MainActivity.this, R.layout.fragment_overview_item, mSearchResults);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the view
            SearchResultsViewHolder holder = null;
            LayoutInflater inflater = getLayoutInflater();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.fragment_overview_item, null, false);
                holder = new SearchResultsViewHolder(convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (SearchResultsViewHolder) convertView.getTag();
            }

            // inject data
            holder.getTitle().setText(mSearchResults.get(position).title);
            holder.getUserName().setText(mSearchResults.get(position).user_name);
            holder.getNumAnswers().setText(String.format("Answers: %d", mSearchResults.get(position).num_answers));

            // defer fetching the user's image
            Drawable default_avatar = null;
            try {
                default_avatar = Drawable.createFromStream(getAssets().open("default_avatar.png"), "default_avatar");
            } catch (IOException e) {
                e.printStackTrace();
            }
            holder.getUserImage().setImageDrawable(default_avatar);
            new ImageRequest(holder.getUserImage(), mSearchResults.get(position).owner_image_url);

            return convertView;
        }
    }

    public void updateOverview() {
        // prepare the adapter
        List<String> titles = new ArrayList<>();
        for(SearchResult x : mSearchResults) titles.add(x.title);
//        ListAdapter a = new ArrayAdapter<String>(this, R.layout.fragment_overview_item, titles);
        ListAdapter a = new SearchResultAdapter();

        // populate the list
        OverviewFragment frag = (OverviewFragment) getSupportFragmentManager().
                findFragmentById(R.id.container);
        frag.setListAdapter(a);
    }
}
