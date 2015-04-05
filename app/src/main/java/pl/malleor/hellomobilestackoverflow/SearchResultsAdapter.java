package pl.malleor.hellomobilestackoverflow;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.util.ArrayList;

// Custom adapter for binding search results to their view
//
// Origin: http://stackoverflow.com/a/11282200/154970
//
class SearchResultsAdapter extends ArrayAdapter<SearchResult> {
    private ArrayList<SearchResult> mResults;
    private Activity mActivity;

    public SearchResultsAdapter(Activity activity, ArrayList<SearchResult> results) {
        super(activity, R.layout.fragment_overview_item, results);

        mActivity = activity;
        mResults = results;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get the view
        SearchResultsViewHolder holder = null;
        LayoutInflater inflater = mActivity.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_overview_item, null, false);
            holder = new SearchResultsViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (SearchResultsViewHolder) convertView.getTag();
        }

        // inject data
        holder.getTitle().setText(mResults.get(position).title);
        holder.getUserName().setText(mResults.get(position).user_name);
        holder.getNumAnswers().setText(String.format("Answers: %d", mResults.get(position).num_answers));

        // delegate fetching the user's image
        Drawable default_avatar = null;
        try {
            default_avatar = Drawable.createFromStream(mActivity.getAssets().open("default_avatar.png"), "default_avatar");
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.getUserImage().setImageDrawable(default_avatar);
        new ImageRequest(holder.getUserImage(), mResults.get(position).owner_image_url);

        return convertView;
    }
}
