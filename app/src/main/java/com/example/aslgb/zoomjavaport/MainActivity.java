package com.example.aslgb.zoomjavaport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.aslgb.zoomjavaport.hqx.Hqx_2x;
import com.example.aslgb.zoomjavaport.hqx.Hqx_3x;
import com.example.aslgb.zoomjavaport.hqx.Hqx_4x;
import com.example.aslgb.zoomjavaport.hqx.RgbYuv;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private static final String imageString = "<img src=2><br><img src=3>";
    private String imageUrl = "https://ege.yandex.ru/media/b19.GIF";

    private Bitmap d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        RadioGroup rg = (RadioGroup) findViewById(R.id.radio);
        rg.setOnCheckedChangeListener(this);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            imageUrl = data.toString();
        }

        final ProgressDialog progress;
        progress = ProgressDialog.show(this, "Downloading", "", true);
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                downloadImage();
                return null;
            }

            protected void onPostExecute(Void result) {
                progress.dismiss();
            }
        };
        asyncTask.execute();

    }

    private void downloadImage() {
        URL url = null;
        try {
            url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream in = connection.getInputStream();
            d = BitmapFactory.decodeStream(in);
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reload(final int zoom) {
        final ProgressDialog progress;
        progress = ProgressDialog.show(this, "Processing", "", true);
        final TextView tv = (TextView) findViewById(R.id.textView);
        AsyncTask<Void, Void, Spanned> asyncTask = new AsyncTask<Void, Void, Spanned>() {
            @Override
            protected Spanned doInBackground(Void... params) {
                return Html.fromHtml(imageString, new ImageGetter(zoom), null);
            }

            protected void onPostExecute(Spanned result) {
                tv.setText(result);
                progress.dismiss();
            }
        };
        asyncTask.execute();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioButton:
                reload(2);
                break;
            case R.id.radioButton2:
                reload(3);
                break;
            case R.id.radioButton3:
                reload(4);
                break;
        }
    }

    private class ImageGetter implements Html.ImageGetter {

        private int zoom;

        public ImageGetter(int zoom) {
            this.zoom = zoom;
        }

        @Override
        public Drawable getDrawable(String source) {
            int w = zoom * d.getWidth();
            int h = zoom * d.getHeight();

            if (source.equals("2")) {
                Drawable dr = new BitmapDrawable(getResources(), d);
                dr.setBounds(0, 0, w, h);
                return dr;
            }

            RgbYuv.hqxInit();
            final int[] data = new int[d.getWidth() * d.getHeight()];
            d.getPixels(data, 0, d.getWidth(), 0, 0, d.getWidth(), d.getHeight());
            final int[] dataDest = new int[w * h];
            switch (zoom) {
                case 2:
                    Hqx_2x.hq2x_32_rb(data, dataDest, d.getWidth(), d.getHeight());
                    break;
                case 3:
                    Hqx_3x.hq3x_32_rb(data, dataDest, d.getWidth(), d.getHeight());
                    break;
                case 4:
                    Hqx_4x.hq4x_32_rb(data, dataDest, d.getWidth(), d.getHeight());
                    break;
            }

            Bitmap d2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            d2.setPixels(dataDest, 0, w, 0, 0, w, h);
            Drawable dr = new BitmapDrawable(getResources(), d2);
            dr.setBounds(0, 0, w, h);
            return dr;
        }
    }
}
