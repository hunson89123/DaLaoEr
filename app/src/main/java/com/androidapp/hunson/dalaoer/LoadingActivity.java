package com.androidapp.hunson.dalaoer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadingActivity extends AppCompatActivity {
    int roomCunt = 0, roomID = 0, playerID = 0, playerQID = 0;
    int playerCunt = 0;
    String playerName = "";
    TextView tv_loading, tv_roomID, tv_playerList;
    DatabaseReference database;
    TinyDB tinyDB;
    Button btn_exitRoom;
    boolean b_onGame = true;
    ValueEventListener exitGRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //自動開啟darkMode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        //初始化tinyDB
        tinyDB = new TinyDB(LoadingActivity.this);
        //初始化firebase
        database = FirebaseDatabase.getInstance().getReference();
        //初始化playerID、Name
        playerID = tinyDB.getInt("playerID");
        playerName = tinyDB.getString("playerName");
        //findViewById
        tv_loading = findViewById(R.id.tv_loading);
        tv_roomID = findViewById(R.id.tv_roomID);
        tv_playerList = findViewById(R.id.tv_playerList);
        btn_exitRoom = findViewById(R.id.btn_exitRoom);
        //檢查資料庫房間數量，0則創建房間，否則加入最後創建之房間
        database.child("roomCunt").child("count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Toast.makeText(LoadingActivity.this,"datachanged!",Toast.LENGTH_LONG).show();
                Long s = snapshot.getValue(Long.class);
                roomCunt = Integer.parseInt(String.valueOf(s));
                if (b_onGame) {
                    if (roomCunt == 0) {
                        database.child("roomCunt").child("count").setValue(1);
                        roomID = roomCunt + 1;
                        createGameRoom();
                    } else {
                        roomID = roomCunt;
                        joinGameRoom();
                    }
                }
                listenerGameRoom(roomID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void createGameRoom() {
        database.child("room").child(roomID + "").child("playerCunt").setValue(0);
        database.child("room").child(roomID + "").child("playerCunt").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                database.child("room").child(roomID + "").child("playerCunt").setValue(Integer.parseInt(String.valueOf(task.getResult().getValue())) + 1);
                playerQID = Integer.parseInt(String.valueOf(task.getResult().getValue()));
//                database.child("room").child(roomID + "").child("players").child(playerCunt + "").setValue(playerName);
//                database.child("room").child(roomID + "").child("players").child(playerCunt + "").setValue(tinyDB.getString("playerName"));
            }
        });
    }

    public void joinGameRoom() {
        database.child("room").child(roomID + "").child("playerCunt").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                database.child("room").child(roomID + "").child("playerCunt").setValue(Integer.parseInt(String.valueOf(task.getResult().getValue())) + 1);
                playerQID = Integer.parseInt(String.valueOf(task.getResult().getValue()));
//                listenerGameRoom(roomID);

            }
        });
    }

    //增加GameRoom監聽器，確保所有玩家獲得房間資訊
    public void listenerGameRoom(int roomID) {
        database.child("room").child(roomID + "").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (b_onGame) {
                    tv_loading.setText("等待玩家中…(" + snapshot.child("players").getChildrenCount() + "/4)");
                    tv_roomID.setText("房間號碼：" + roomID);
                    String[] sArr_playerList = new String[4];
                    database.child("room").child(roomID + "").child("players").child(playerQID + "").setValue(playerID);
                    database.child("room").child(roomID + "").child("players").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StringBuilder sb_playerNameList = new StringBuilder();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                database.child("player").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                        Toast.makeText(LoadingActivity.this, task.getResult().child(dataSnapshot.getValue() + "").child("name").getValue() + "", Toast.LENGTH_SHORT).show();
                                        sb_playerNameList.append(" " + task.getResult().child(dataSnapshot.getValue() + "").child("name").getValue());
                                        tv_playerList.setText("玩家:" + sb_playerNameList);
                                    }
                                });
                            }

//                            String str_playerList = (String.valueOf(snapshot.getValue()).replace("{", "")).replace("}", "");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void onExitRoomClick(View v) {
        finish();
    }

    public void exitGameRoom() {
        exitGRL = database.child("room").child(roomID + "").child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!b_onGame) {
                    if (snapshot.getChildrenCount() == 1) {
                        database.child("room").child(roomID + "").removeValue();
                        database.child("roomCunt").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                database.child("roomCunt").child("count").setValue(Integer.parseInt(String.valueOf(task.getResult().getValue())) - 1);
                            }
                        });
                    } else {
                        database.child("room").child(roomID + "").child("players").child(playerQID + "").removeValue();
                    }
                    database.child("room").child(roomID + "").child("players").removeEventListener(exitGRL);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        b_onGame = false;
        exitGameRoom();
    }
}