package com.androidapp.hunson.dalaoer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {
    Button btn_singleGame, btn_multiGame, btn_setting;
    ImageView iv_background;
    TinyDB tinyDB;
    TextView tv_playerID;
    int i_picHome = 1;
    int playerID = 0;
    String playerName = "";
    int i_dbValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //自動開啟darkMode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        tinyDB = new TinyDB(HomeActivity.this);

        btn_singleGame = findViewById(R.id.btn_singleGame);
        btn_multiGame = findViewById(R.id.btn_multiGame);
        btn_setting = findViewById(R.id.btn_setting);
        iv_background = findViewById(R.id.iv_background);
        tv_playerID = findViewById(R.id.tv_playerID);

        playerID = tinyDB.getInt("playerID");
        playerName = tinyDB.getString("playerName");
        checkPlayerID();

    }

    public void singleGame(View v) {
//        checkPlayerName();
        startActivity(new Intent(HomeActivity.this,MainActivity.class));
//        startActivity(new Intent(HomeActivity.this, MainActivity.class));
    }

    public void multiGame(View v) {
        checkPlayerName();
        if (!tinyDB.getString("playerName").equals("")) startActivity(new Intent(HomeActivity.this, LoadingActivity.class));
    }

    public void setting(View v) {

        iv_background.setImageResource(getResources().getIdentifier("@drawable/pic_home" + i_picHome, null, getPackageName()));
        if (i_picHome < 9) i_picHome++;
        else i_picHome = 0;
    }

    public void showAppStruct(View v){

    }
    public void clearTinyDB(View v) {
//        tinyDB.clear();
//        Toast.makeText(this, "clear!", Toast.LENGTH_LONG).show();
    }

    public void checkPlayerID() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("playerCunt/count");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long s = snapshot.getValue(Long.class);
                i_dbValue = Integer.parseInt(String.valueOf(s));
                Log.d("",s+","+tinyDB.getInt("getDBValue"));

                if (tinyDB.getInt("playerID") != 0) {
                    if (!playerName.equals("")) {
                        playerName = tinyDB.getString("playerName");
                    } else playerName = "逆命" + playerID + "號";
                    tv_playerID.setText(playerName + "(" + playerID+")");
                } else {
                    playerID = i_dbValue + 1;
                    tinyDB.putInt("playerID", playerID);
                    Toast.makeText(HomeActivity.this, playerID + "?", Toast.LENGTH_LONG).show();
                    writeDBValue("playerCunt/count", Long.valueOf(String.valueOf(i_dbValue + 1)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void checkPlayerName() {
        if (tinyDB.getString("playerName").equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("First time?輸入玩家名稱吧~");
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    playerName = input.getText().toString();
                    tinyDB.putString("playerName", playerName);
                    writeDBString("player/" + playerID + "/name", playerName);
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(getResources().getColor(R.color.white));
            negativeButton.setTextColor(getResources().getColor(R.color.white));

        }
    }

    public void getDBValue(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long s = snapshot.getValue(Long.class);
                i_dbValue = Integer.parseInt(String.valueOf(s));
                tinyDB.putInt("getDBValue",i_dbValue);
                Log.d("",s+","+tinyDB.getInt("getDBValue"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void writeDBValue(String path, Long value) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);

        myRef.setValue(value);
    }

    public void writeDBString(String path, String str) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);

        myRef.setValue(str);
    }
}