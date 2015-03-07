package pl.malleor.hellomobilestackoverflow;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

// TODO: add caching if lags
public class SearchResultsViewHolder {
    private View mContainer;

    public SearchResultsViewHolder(View container) {
        mContainer = container;
    }

    public TextView getTitle() {
        return (TextView) mContainer.findViewById(R.id.title);
    }

    public ImageView getUserImage() {
        return (ImageView) mContainer.findViewById(R.id.user_image);
    }

    public TextView getUserName() {
        return (TextView) mContainer.findViewById(R.id.user_name);
    }

    public TextView getNumAnswers() {
        return (TextView) mContainer.findViewById(R.id.answers);
    }
}
