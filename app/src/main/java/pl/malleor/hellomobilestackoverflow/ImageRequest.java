package pl.malleor.hellomobilestackoverflow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

public class ImageRequest extends AsyncTask<String, Void, Bitmap> {
    ImageView mImage;

    public ImageRequest(ImageView bmImage, String url) {
        mImage = bmImage;

        execute(url);
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        try {
            InputStream in = new java.net.URL(url).openStream();
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Bitmap result) {
        if(result != null)
            mImage.setImageBitmap(result);
    }
}
