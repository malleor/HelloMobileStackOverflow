package pl.malleor.hellomobilestackoverflow;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/// Makes a HTTP request to StackOverflow asynchronously.
/// Calls client callbacks upon success or failure *in its own thread*.
public class StackRequest extends AsyncTask<String, String, String> {

    private static final String TAG = "StackRequest";

    /// Request client
    ///
    /// If successful, receives a response from the server.
    /// In unsuccessful, receives notification.
    ///
    /// @note Methods called in the pl.malleor.hellomobilestackoverflow.StackRequest's thread!
    public interface Client
    {
        public abstract void onSuccess(String response_json);
        public abstract void onFailure(String reason);
    }


    private Client mClient = null;

    public StackRequest(String query, Client client)
    {
        // remember who the client is
        mClient = client;

        // TODO: form an URL according to http://api.stackexchange.com/docs/search
        String url = "http://stackoverflow.com";

        // execute the async task
        this.execute(url);
    }

    // note: the stub comes from SO
    // http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;

        try {
            Log.d(TAG, String.format("will http-get '%s'", uri[0]));

            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){

                HttpEntity entity = response.getEntity();
                Log.d(TAG, String.format("received %dB of response", entity.getContentLength()));
                Log.d(TAG, String.format("content-type: %s", entity.getContentType().getValue()));
                Log.d(TAG, String.format("encoding: %s", entity.getContentEncoding() == null ?
                        "??" : entity.getContentEncoding().getValue()));

                // got it -> fetch the response
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                entity.writeTo(out);
                responseString = out.toString();
                out.close();
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
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // hand the result to the client
        if(mClient != null)
            mClient.onSuccess(result);
    }
}
