package pl.malleor.hellomobilestackoverflow;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class OverviewFragment extends ListFragment implements OnRefreshListener {

    private static final String TAG = "Overview";

    private PullToRefreshLayout mPullToRefreshLayout;

    public OverviewFragment() {
    }

    public void packContent(final ArrayList<SearchResult> results) {
        // pack and store results
        Bundle args = new Bundle();
        args.putParcelableArrayList("results", results);
        this.setArguments(args);
    }

    protected ArrayList<SearchResult> unpackContent() {
        // unpack stored results
        return this.getArguments().getParcelableArrayList("results");
    }

    public void updateContent(final ArrayList<SearchResult> results, ListView list_view) {
        // prepare the adapter
        final MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        ListAdapter a = new SearchResultsAdapter(activity, results);

        // populate the list
        setListAdapter(a);

        // register on click
        if(list_view == null)
            list_view = getListView();
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.displayDetailsView(results.get(position));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate
        View overviewView = inflater.inflate(R.layout.fragment_overview, container, false);
        updateContent(unpackContent(), (ListView) overviewView.findViewById(android.R.id.list));

        // setup the PullToRefreshLayout
        mPullToRefreshLayout = (PullToRefreshLayout) overviewView.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .theseChildrenArePullable(android.R.id.list)
                .listener(this)
                .setup(mPullToRefreshLayout);

        return overviewView;
    }

    @Override
    public void onRefreshStarted(View view) {
        // perform search
        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        new StackRequest(activity.getLastQuery(), new RequestClient(activity) {
            @Override
            public void onSuccess(JSONObject result) {
                super.onSuccess(result);

                // plus, resolve refresh action in PullToRefresh component
                mPullToRefreshLayout.setRefreshComplete();
            }
        });
    }
}
