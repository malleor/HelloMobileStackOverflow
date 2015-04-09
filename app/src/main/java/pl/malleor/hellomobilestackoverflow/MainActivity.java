package pl.malleor.hellomobilestackoverflow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import java.util.ArrayList;


/// Main application activity
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "StackSearch";

    private Fragment mVisibleFragment = null;

    private String mQuery = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // listen to backstack changes
        // we need to update `mVisibleFragment` upon user nav'ing back
        final FragmentManager fman = getSupportFragmentManager();
        fman.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // that's not an elegant way to achieve this, but quite straightforward:
                // let's detect if the backstack has just shrunk, and put the last known head
                // of the stack (the just-removed fragment) as the new `mVisibleFragment`

                // has the backstack shrunk?
                int current_stack_size = fman.getBackStackEntryCount();
                boolean has_backstack_shrunk = current_stack_size < mLastStackSize;
                mLastStackSize = current_stack_size;
                Log.d(TAG, String.format("new backstack size: %d %s",
                        current_stack_size, has_backstack_shrunk ? "shrunk" : "extended"));

                // notify the activity about the new visible fragment
                if(has_backstack_shrunk) {
                    mVisibleFragment = mLastStackHead;
                    Log.d(TAG, "fragment restored " + mLastStackHead.getClass().getName());
                }

                // remember the backstack head
                if(current_stack_size > 0) {
                    Fragment head = fman.getFragments().get(current_stack_size - 1);
                    Log.d(TAG, "backstack new head: " + head.getClass().getName());
                    mLastStackHead = head;
                } else {
                    mLastStackHead = null;
                }
            }

            private int mLastStackSize = 0;
            private Fragment mLastStackHead = null;
        });

        // start with the search view
        if (savedInstanceState == null) {
            displaySearchView();
        }
    }

    public void displaySearchView() {
        // change the view
        commitFragment(new SearchFragment());
    }

    public void displayResultsView(ArrayList<SearchResult> results) {
        Log.d(TAG, "--------------------------------------------------------------");
        Log.d(TAG, "Current view: " + mVisibleFragment.getClass().getName());
        Log.d(TAG, "--------------------------------------------------------------");

        // change the view
        // (as long as it is not already a proper view; and it is upon refresh)
        if(OverviewFragment.class != mVisibleFragment.getClass()) {
            OverviewFragment frag = new OverviewFragment();
            frag.packContent(results);

            commitFragment(frag);
        } else {
            // or just update list items otherwise
            ((OverviewFragment) mVisibleFragment).updateContent(results, null);
        }
    }

    public void displayDetailsView(SearchResult result) {
        // change the view
        commitFragment(new DetailsFragment());
        getSupportFragmentManager().executePendingTransactions();

        // load the website
        WebView view = (WebView) mVisibleFragment.getView().findViewById(R.id.fragment_details);
        assert view != null;
        view.loadUrl(result.url);
    }

    private void commitFragment(Fragment fragment) {
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();

        // remove previous fragment
        if (mVisibleFragment != null)
            trans.remove(mVisibleFragment);

        // push to backstack (except the initial view)
        if (fragment.getClass() != SearchFragment.class)
            trans.addToBackStack(null);

        // add the fragment
        trans.add(R.id.container, mVisibleFragment = fragment);

        trans.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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

    public String getLastQuery() {
        return mQuery;
    }

    /// The user hits 'Search'
    public void onSearchButton(View v)
    {
        // fetch the query from UI
        mQuery = ((EditText)this.findViewById(R.id.query_input)).getText().toString();
        Log.d(TAG, String.format("the user needs to search for '%s'", mQuery));

        // query StackOverflow
        // TODO: store the request to be able to cancel it (as soon as Cancel is implemented)
        new StackRequest(mQuery, new RequestClient(this));
    }
}
