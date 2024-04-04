package org.techtown.lunaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnTabItemSelectedListener {
    private static final String TAG = "MainActivity";

    Fragment1 fragment1;
    Fragment2 fragment2;
    Fragment3 fragment3;
    Fragment4 fragment4;
    Fragment5 fragment5;

    BottomNavigationView bottomNavigation;

    String currentDateString;
    Date currentDate;
    SimpleDateFormat todayDateFormat;

    public static NoteDatabase mDatabase = null;

    Button button;
    TextView textView;

    List<String> textValues, textColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.randomButton);
        textView = findViewById(R.id.randomTextView);

        textValues = new ArrayList<>();
        textColors = new ArrayList<>();

        textValues.add("어떤 꿈을 꾸었나요");
        textValues.add("무엇을 할 계획인가요");
        textValues.add("연인과 무엇을 했나요");
        textValues.add("기분은 어떤가요");
        textValues.add("하고 싶은 일이 떠올랐나요");
        textValues.add("그 사람의 첫인상은 어땠나요");
        textValues.add("연인에게 반한 이유는");
        textValues.add("연인에게 고마운 일 3가지");
        textValues.add("가장 행복했던 데이트는");

        textColors.add("?");
        textColors.add("!");
        textColors.add("!?");
        textColors.add("..?");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random r = new Random();

                String randomValue = textValues.get(r.nextInt(textValues.size()));
                String randomColor = textColors.get(r.nextInt(textColors.size()));

                textView.setText("' " + randomValue +  randomColor + "  '");

            }
        });

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3();
        fragment4 = new Fragment4();
        fragment5 = new Fragment5();

        final LinearLayout notice = (LinearLayout) findViewById(R.id.notice);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab1:
                        notice.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "노트", Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fragment1).commit();
                        return true;
                    case R.id.tab2:
                        notice.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "글쓰기", Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fragment2).commit();
                        return true;
                    case R.id.tab3:
                        notice.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "데이터 코스", Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fragment3).commit();
                        return true;
                    case R.id.tab4:
                        notice.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "맛집", Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fragment4).commit();
                        return true;
                    case R.id.tab5:
                        notice.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "설정", Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fragment5).commit();
                        return true;
                    }

                return false;
            }
        });

        setPicturePath();

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("허용된 권한 갯수 : " + permissions.size());
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("거부된 권한 갯수 : " + permissions.size());
                    }
                })
                .start();


        openDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    public void openDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }

        mDatabase = NoteDatabase.getInstance(this);
        boolean isOpen = mDatabase.open();
        if (isOpen) {
            Log.d(TAG, "Note database is open.");
        } else {
            Log.d(TAG, "Note database is not open.");
        }
    }

    public void setPicturePath() {
        String folderPath = getFilesDir().getAbsolutePath();
        AppConstants.FOLDER_PHOTO = folderPath + File.separator + "photo";

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);
        if (!photoFolder.exists()) {
            photoFolder.mkdirs();
        }
    }
    public void onTabSelected(int position) {
        if (position == 0) {
            bottomNavigation.setSelectedItemId(R.id.tab1);
        } else if (position == 1) {
            fragment2 = new Fragment2();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment2).commit();
        } else if (position == 2) {
            bottomNavigation.setSelectedItemId(R.id.tab3);
        }
    }

    public void showFragment2(Note item) {

        fragment2 = new Fragment2();
        fragment2.setItem(item);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment2).commit();

    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}