package pl.malleor.hellomobilestackoverflow;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


/// StackOverflow search result
///
/// Result data container.
///
public class SearchResult implements Parcelable {

    public String title;
    public String user_name;
    public String owner_image_url;
    public int num_answers;
    public String url;


    /// Parse a JSON object
    public SearchResult(JSONObject item) throws JSONException {
        title = item.getString("title");
        num_answers = item.getInt("answer_count");
        url = item.getString("link");

        JSONObject owner = item.getJSONObject("owner");
        user_name = owner.getString("display_name");
        owner_image_url = owner.getString("profile_image");
    }

    /// Unpack data
    private SearchResult(Parcel in) {
        title = in.readString();
        user_name = in.readString();
        owner_image_url = in.readString();
        num_answers = in.readInt();
        url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(user_name);
        dest.writeString(owner_image_url);
        dest.writeInt(num_answers);
        dest.writeString(url);
    }

    public static final Parcelable.Creator<SearchResult> CREATOR
            = new Parcelable.Creator<SearchResult>() {
        public SearchResult createFromParcel(Parcel in) {
            return new SearchResult(in);
        }

        public SearchResult[] newArray(int size) {
            return new SearchResult[size];
        }
    };
}
