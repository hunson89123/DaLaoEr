package com.androidapp.hunson.dalaoer;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.Guideline;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    Button btn_button;
    LinearLayout ll_cards;
    TinyDB tinyDB;
    ImageView[] iv_cards;
    Guideline gl_table;
    String[] cardName;
    TextView tv_debug, tv_cardType;
    TreeMap<Integer, String> m_cardNameTemp, m_cardName;
    TreeMap<Integer, String> m_cardHandA, m_cardHandB, m_cardHandC, m_cardHandD;
    List<Map.Entry<Integer, String>> l_cardNameA;
    List<Integer> l_cardNameKeys;
    List<String> l_cardSelected;
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
        //自動開啟darkMode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
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
        tv_debug = findViewById(R.id.tv_debug);
        tv_cardType = findViewById(R.id.tv_cardType);
        //設定全螢幕及隱藏導航列
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //delay後產生卡牌
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
        //初始化卡牌樣式
        iv_cards = new ImageView[13];
        i_cardH = ll_cards.getMeasuredHeight() - i_selectH;
        i_cardW = i_picW * i_cardH / i_picH;
        i_cardM = (i_screenH - 16 - i_cardW) / 12;
        i_cardL = i_cardM - i_cardW;
        //初始化卡牌順序
        l_cardNameA = new ArrayList<>(m_cardHandA.entrySet());
//        List<Map.Entry<Integer,String>> l_cardNameB = new ArrayList<>(m_cardName.subMap(13,26).entrySet());
//        List<Map.Entry<Integer,String>> l_cardNameC = new ArrayList<>(m_cardName.subMap(26,39).entrySet());
//        List<Map.Entry<Integer,String>> l_cardNameD = new ArrayList<>(m_cardName.subMap(39,52).entrySet());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            l_cardNameA.sort(Map.Entry.comparingByKey());
        }

        for (int x = 0; x < 13; x++) {
            iv_cards[x] = new ImageView(this);
//            iv_cards[x].setImageResource(getResources().getIdentifier("@drawable/s" + (x + 1), null, getPackageName()));
            iv_cards[x].setImageResource(getResources().getIdentifier("@drawable/" + l_cardNameA.get(x).getValue(), null, getPackageName()));
            Log.d("cardTable", l_cardNameA.get(x) + "");
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
//        Toast.makeText(MainActivity.this, i_cardH + "," + i_cardW + "," + i_cardH + "," + i_cardL, Toast.LENGTH_LONG).show();
    }

    public void onCardClick(View v) {
        l_cardSelected = new ArrayList<>();
        if (b_cardSelected[v.getId()]) {
            iv_cards[v.getId()].setY(iv_cards[v.getId()].getY() + i_selectH);
            b_cardSelected[v.getId()] = false;
            l_cardSelected.remove(l_cardNameA.get(v.getId()).getValue());
        } else {
            iv_cards[v.getId()].setY(iv_cards[v.getId()].getY() - i_selectH);
            b_cardSelected[v.getId()] = true;
        }
        for (int x = 0; x < 13; x++) {
            if (b_cardSelected[x]) l_cardSelected.add(l_cardNameA.get(x).getValue());
        }
        tv_debug.setText("你選取的卡:" + l_cardSelected.toString().replace("[", "").replace("]", ""));
        tv_cardType.setText(getCardType(l_cardSelected));
    }

    public void randomCards() {
        //產生整副牌
        m_cardNameTemp = new TreeMap<>();
        String[] hua = new String[]{"c", "d", "h", "s"};
        int[] num = new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 1, 2};
        cardName = new String[52];
        for (int x = 0; x < 13; x++) {
            for (int y = 0; y < 4; y++) {
                cardName[x * 4 + y] = hua[y] + num[x];
                m_cardNameTemp.put(x * 4 + y, cardName[x * 4 + y]);
                Log.d("cardRndCards", m_cardNameTemp.get(x * 4 + y) + "");
            }

        }
        //洗牌
        l_cardNameKeys = new ArrayList<>(m_cardNameTemp.keySet());
        Collections.shuffle(l_cardNameKeys);

        //分牌
        m_cardHandA = new TreeMap<>();
        for (int x = 0; x < 13; x++) {
            m_cardHandA.put(l_cardNameKeys.get(x), m_cardNameTemp.get(l_cardNameKeys.get(x)));
            Log.d("cardHandA", l_cardNameKeys.get(x) + "=" + m_cardNameTemp.get(l_cardNameKeys.get(x)) + "");
        }
    }

    public String getCardType(List<String> selected) {
        StringBuilder s_cardType = new StringBuilder();
        List<String> selectedH = new ArrayList<>();
        List<Integer> selectedN = new ArrayList<>();
        for (String s_current : selected) {
            selectedH.add(s_current.charAt(0) + "");
            selectedN.add(Integer.parseInt(s_current.substring(1)));
        }
        String[] sa_cardType = new String[]{"單張", "對子", "順子", "葫蘆", "鐵支", "同花順"};
        boolean[] ba_cardType = new boolean[]{
                isSingle(selected, selectedH, selectedN),
                isPair(selected, selectedH, selectedN),
                isStraight(selected, selectedH, selectedN),
                isFullHouse(selected, selectedH, selectedN),
                isFourOfaKind(selected, selectedH, selectedN),
                isFlush(selected, selectedH, selectedN)
        };
        for (int x = 0; x < 6; x++)
            if (ba_cardType[x]) s_cardType.append(sa_cardType[x]);

        return s_cardType.toString();
    }

    //單張
    public boolean isSingle(List<String> selected, List<String> selectedH, List<Integer> selectN) {
        return selected.size() == 1;
    }

    //對子
    public boolean isPair(List<String> selected, List<String> selectedH, List<Integer> selectedN) {
        if (selected.size() == 2) {
            return selectedN.get(0).equals(selectedN.get(1));
        }
        return false;
    }

    //順子
    public boolean isStraight(List<String> selected, List<String> selectedH, List<Integer> selectedN) {
        if (selected.size() == 5) {
            boolean valid = true;
            for (int i = 0; i < 4; i++) {
                int current = selectedN.get(i);
                int next = selectedN.get(i + 1);

                // when the card is 'Ace' or 'Two', give it higher value according to the rule of the BigTwo game.
                if (current == 2 || current == 1) {
                    current += 13;
                }
                if (next == 2 || next == 1) {
                    next += 13;
                }
                if (current != next - 1) {
                    valid = false;
                    break;
                }
            }
            return valid;
        }
        return false;
    }

    //葫蘆
    public boolean isFullHouse(List<String> selected, List<String> selectedH, List<Integer> selectedN) {
        if (selected.size() == 5) {
            boolean valid = true;
            int flag = selectedN.get(2);
            if (selectedN.get(1) == flag) {    // when the rank of center card is equal to that of the card on left.
                if (!selectedN.get(0).equals(selectedN.get(1))) {
                    valid = false;
                }
                if (!selectedN.get(3).equals(selectedN.get(4))) {
                    valid = false;
                }
            } else if (selectedN.get(3) == flag) {    // when the rank of center card is equal to that of the card on right.
                if (!selectedN.get(3).equals(selectedN.get(4))) {
                    valid = false;
                }
                if (!selectedN.get(0).equals(selectedN.get(1))) {
                    valid = false;
                }
            } else
                valid = false;
            return valid;
        }
        return false;
    }

    //鐵支
    public boolean isFourOfaKind(List<String> selected, List<String> selectedH, List<Integer> selectedN) {
        if (selected.size() == 5) {
            int counter;
            for (int i = 0; i < selected.size(); i++) {
                counter = 0;
                for (int j = 0; j < selected.size(); j++)
                    if (selectedN.get(j).equals(selectedN.get(i)))
                        counter++;
                if (counter >= 4)
                    return true;
            }
        }
        return false;
    }

    //同花順
    public boolean isFlush(List<String> selected, List<String> selectedH, List<Integer> selectedN) {
        boolean valid = true;
        if (selected.size() != 5) return false;
        else {
            if (isStraight(selected, selectedH, selectedN)) {
                for (int i = 0; i < 4; i++)
                    if (!selectedH.get(i).equals(selectedH.get(4))) {
                        valid = false;
                        break;
                    }
            } else valid = false;
        }
        return valid;
    }

    public void shuffle(View v){
        randomCards();
        ll_cards.removeAllViews();
        tv_debug.setText("");
        tv_cardType.setText("");
        l_cardSelected = new ArrayList<>();
        b_cardSelected = new boolean[13];
        add(null);
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