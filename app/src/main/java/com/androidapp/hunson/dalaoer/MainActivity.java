package com.androidapp.hunson.dalaoer;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    Button btn_button;
    LinearLayout ll_cards;
    TinyDB tinyDB;
    ImageView[] iv_cards;
    Guideline gl_table;
    String[] cardName;
    boolean[] b_cardSelected = new boolean[13];
    int i_actionBarH = 0, i_screenH = 0, i_picW = 500, i_picH = 726;
    int i_cardW = 0, i_cardH = 0, i_cardM = 0, i_cardL = 0;
    int playerID = 0;
    int i_selectH = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化tinyDB
        tinyDB = new TinyDB(MainActivity.this);
        //取得螢幕height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            i_actionBarH = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        i_screenH = displayMetrics.widthPixels - i_actionBarH;
        //findViewById
        btn_button = findViewById(R.id.button);
        ll_cards = findViewById(R.id.ll_cards);
        gl_table = findViewById(R.id.gl_table);
        //設定全螢幕及隱藏導航列
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                add(null);
            }
        }, 1);
        randomCards();
    }

    public void add(View view) {
        iv_cards = new ImageView[13];
        i_cardH = ll_cards.getMeasuredHeight() - i_selectH;
        i_cardW = i_picW * i_cardH / i_picH;
        i_cardM = (i_screenH - 16 - i_cardW) / 12;
        i_cardL = i_cardM - i_cardW;
        for (int x = 0; x < 13; x++) {
            iv_cards[x] = new ImageView(this);
//            iv_cards[x].setImageResource(getResources().getIdentifier("@drawable/s" + (x + 1), null, getPackageName()));
            iv_cards[x].setImageResource(getResources().getIdentifier("@drawable/" + cardName[x], null, getPackageName()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(i_cardW, i_cardH);
            if (x > 0) params.setMargins(i_cardL, i_selectH, 0, 0);
            else params.setMargins(0, i_selectH, 0, 0);
            iv_cards[x].setLayoutParams(params);
            iv_cards[x].setId(x);
            iv_cards[x].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCardClick(view);
                }
            });
            ll_cards.addView(iv_cards[x]);
        }
        Toast.makeText(MainActivity.this, i_cardH + "," + i_cardW + "," + i_cardH + "," + i_cardL, Toast.LENGTH_LONG).show();
    }

    public void onCardClick(View v) {
        if (b_cardSelected[v.getId()]) {
            iv_cards[v.getId()].setY(iv_cards[v.getId()].getY() + i_selectH);
            b_cardSelected[v.getId()] = false;
        } else {
            iv_cards[v.getId()].setY(iv_cards[v.getId()].getY() - i_selectH);
            b_cardSelected[v.getId()] = true;
        }
    }

    public void randomCards() {
        String[] hua = new String[]{"c", "d", "h", "s"};
        cardName = new String[52];
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 13; y++) {
                cardName[x * 13 + y] = hua[x] + (y + 1);
                Log.d("cardName", cardName[x * 13 + y]);
            }
        }
        Collections.shuffle(Arrays.asList(cardName));
    }

    public void getDBValue(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long ID = snapshot.getValue(Long.class);
                Log.d("", "Value is: " + ID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}