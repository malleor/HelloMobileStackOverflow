package pl.malleor.hellomobilestackoverflow;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class OverviewFragment extends ListFragment implements OnRefreshListener {

    private PullToRefreshLayout mPullToRefreshLayout;

    public OverviewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate
        View overviewView = inflater.inflate(R.layout.fragment_overview, container, false);

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
