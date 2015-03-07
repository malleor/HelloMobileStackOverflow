package pl.malleor.hellomobilestackoverflow;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/// Makes a HTTP request to StackOverflow asynchronously.
/// Calls client callbacks upon success or failure *in its own thread*.
public class StackRequest extends AsyncTask<String, String, JSONObject> {

    private static final String TAG = "StackRequest";

    /// Request client
    ///
    /// If successful, receives a response from the server.
    /// In unsuccessful, receives notification.
    ///
    /// @note Methods called in the pl.malleor.hellomobilestackoverflow.StackRequest's thread!
    public interface Client
    {
        public abstract void onSuccess(JSONObject result);
        public abstract void onFailure(String reason);
    }


    private Client mClient = null;

    public StackRequest(String query, Client client)
    {
        // remember who the client is
        mClient = client;

        // form an URL according to http://api.stackexchange.com/docs/search
        String url = formatUrl(query, 10, 1);

        // execute the async task
        this.execute(url);
    }

    private String formatUrl(String query, int page_size, int page_number) {
        String TEMPLATE = "http://api.stackexchange.com/2.2/search?order=desc&sort=activity&site=stackoverflow&" +
                            "pagesize=%d&page=%d&intitle=%s";
        return String.format(TEMPLATE, page_size, page_number, query);
    }

    // note: the stub comes from SO
    // http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
    @Override
    protected JSONObject doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();

        try {
            Log.d(TAG, String.format("will http-get '%s'", uri[0]));

            HttpResponse response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){

                HttpEntity entity = response.getEntity();
                Log.d(TAG, String.format("received %dB of response", entity.getContentLength()));
                Log.d(TAG, String.format("content-type: %s", entity.getContentType().getValue()));
                Log.d(TAG, String.format("encoding: %s", entity.getContentEncoding() == null ?
                        "??" : entity.getContentEncoding().getValue()));

                // unzip the stream
                InputStream is = entity.getContent();
                GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(is));
                BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                String result_str = builder.toString();
                Log.d(TAG, "+++++++++++++++++++++++++++++++++++++");
                Log.d(TAG, "RESULT: " + result_str);
                Log.d(TAG, "+++++++++++++++++++++++++++++++++++++");

                // parse and return json
                return new JSONObject(result_str);

            } else{
                // failed -> jump to the catch clause
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            // notify the client
            if(mClient != null)
                mClient.onFailure(e.getMessage());
        } catch (IOException e) {
            // notify the client
            if(mClient != null)
                mClient.onFailure(e.getMessage());
        } catch (JSONException e) {
            // notify the client
            if(mClient != null)
                mClient.onFailure(e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);

        // hand the result to the client
        if(mClient != null)
            mClient.onSuccess(result);
    }
}
