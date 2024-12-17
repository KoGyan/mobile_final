package com.example.phogoviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<Bitmap> imageList = new ArrayList<>();
    private static final String API_URL = "http://10.0.2.2:8000/api_root/Post/"; // Django 서버 주소

    private myPictureView myPictureView;
    private Button btnHide, btnShow, btnToggleZoom, btnLoad, btnRotate, zoomControlsBT;
    private boolean isImageExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, PackageManager.PERMISSION_GRANTED);


        // UI 초기화
        initializeUI();


        // RecyclerView 설정
        adapter = new ImageAdapter(imageList, this::showImageInView);
        recyclerView.setAdapter(adapter);

        // 이미지 동기화 버튼
        btnLoad.setOnClickListener(v -> new ImageFetchTask().execute());

        // 이미지 숨기기
        btnHide.setOnClickListener(v -> {
            recyclerView.setVisibility(View.GONE);
            myPictureView.setVisibility(View.GONE);
            Toast.makeText(this, "이미지 숨김", Toast.LENGTH_SHORT).show();
        });
        ZoomControls zoomControls = findViewById(R.id.zoomControls);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPictureView.setPivotX(0);
                myPictureView.setPivotY(0);
                myPictureView.setScaleX(myPictureView.getScaleX() + 0.1f);
                myPictureView.setScaleY(myPictureView.getScaleY() + 0.1f);
                //Toast.makeText(MainActivity.this, "이미지 확대", Toast.LENGTH_SHORT).show();
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPictureView.setPivotX(0);
                myPictureView.setPivotY(0);
                myPictureView.setScaleX(myPictureView.getScaleX() - 0.1f);
                myPictureView.setScaleY(myPictureView.getScaleY() - 0.1f);
                //Toast.makeText(MainActivity.this, "이미지 축소", Toast.LENGTH_SHORT).show();
            }
        });

        // 이미지 보기
        btnShow.setOnClickListener(v -> {
            recyclerView.setVisibility(View.VISIBLE);
            myPictureView.setVisibility(View.VISIBLE);
            //Toast.makeText(this, "이미지 보기", Toast.LENGTH_SHORT).show();
        });

        // 이미지 회전 버튼
        btnRotate.setOnClickListener(v -> {
            myPictureView.setRotation(myPictureView.getRotation() + 90);
            //Toast.makeText(this, "이미지 회전", Toast.LENGTH_SHORT).show();
        });

        // 이미지 확대/축소 버튼
        btnToggleZoom.setOnClickListener(v -> toggleImageView());
    }

    private void initializeUI() {
        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myPictureView = findViewById(R.id.myPictureView1);

        btnHide = findViewById(R.id.imagehide);
        btnShow = findViewById(R.id.imageseen);
        btnToggleZoom = findViewById(R.id.btn_toggle_zoom);
        btnLoad = findViewById(R.id.btn_load);
        btnRotate = findViewById(R.id.btn_rotate);
    }

    private void showImageInView(Bitmap bitmap) {
        recyclerView.setVisibility(View.GONE);
        myPictureView.setVisibility(View.VISIBLE);
        myPictureView.setImageBitmap(bitmap);
        btnToggleZoom.setVisibility(View.VISIBLE);
        btnToggleZoom.setText("더보기");
        isImageExpanded = true;
    }

    private void toggleImageView() {
        if (isImageExpanded) {
            myPictureView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            btnToggleZoom.setVisibility(View.GONE);
            isImageExpanded = false;
        }
    }

    // 서버에서 이미지 목록을 가져오는 비동기 작업
    private class ImageFetchTask extends AsyncTask<Void, Void, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(Void... voids) {
            List<Bitmap> bitmaps = new ArrayList<>();
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                StringBuilder response = new StringBuilder();
                int data;
                while ((data = inputStream.read()) != -1) {
                    response.append((char) data);
                }
                inputStream.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonImageObject = jsonArray.getJSONObject(i);
                    String imageUrl = jsonImageObject.getString("image").replace("127.0.0.1", "10.0.2.2");
                    Bitmap bitmap = downloadImage(imageUrl);
                    if (bitmap != null) {
                        bitmaps.add(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(List<Bitmap> result) {
            if (!result.isEmpty()) {
                imageList.addAll(result);
                adapter.notifyDataSetChanged();
                //Toast.makeText(MainActivity.this, "이미지 로드 성공", Toast.LENGTH_SHORT).show();
            } else {
                textView.setText("불러올 이미지가 없습니다.");
            }
        }

        private Bitmap downloadImage(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
