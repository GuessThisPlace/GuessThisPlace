package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;


import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;

import hr.etfos.glabab.guessthisplace.R;

public class ViewPhotoActivity extends Activity {

    SubsamplingScaleImageView iv;
    String fileUrl;

    private Target localTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            prepareBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Picasso.with(ViewPhotoActivity.this)
                    .load(fileUrl)
                    .error(R.drawable.error)
                    .into(targetRemote);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    private Target targetRemote = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            prepareBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        Intent intent = getIntent();
        Uri fileUri = (Uri) intent.getParcelableExtra("fileUri");
        fileUrl = intent.getStringExtra("fileUrl");
        iv = (SubsamplingScaleImageView) findViewById(R.id.view_photo_image);
        if (fileUri != null) {
            Bitmap bitmap = null;
            try {
                bitmap = UploadPhotoActivity.getBitmap(fileUri);
                prepareBitmap((bitmap));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Picasso.with(this)
                    .load(fileUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(localTarget);
        }
    }

    private void prepareBitmap(Bitmap bitmap)
    {
        if (bitmap == null)
            return;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            iv.setOrientation(SubsamplingScaleImageView.ORIENTATION_90);
        }
        iv.setImage(ImageSource.bitmap(bitmap));
        iv.setMaxScale(15);
    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
