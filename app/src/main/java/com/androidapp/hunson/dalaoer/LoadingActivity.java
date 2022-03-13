package com.androidapp.hunson.dalaoer;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadingActivity extends AppCompatActivity {
    int roomCunt = 0, roomID = 0;
    int playerCunt = 0;
    TextView tv_loading;
    DatabaseReference database;
    TinyDB tinyDB;
    boolean b_onGame = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        tinyDB = new TinyDB(LoadingActivity.this);
        database = FirebaseDatabase.getInstance().getReference();
        tv_loading = findViewById(R.id.tv_loading);


        database.child("roomCunt").child("count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Toast.makeText(LoadingActivity.this,"datachanged!",Toast.LENGTH_LONG).show();
                Long s = snapshot.getValue(Long.class);
                roomCunt = Integer.parseInt(String.valueOf(s));
                if (b_onGame) {
                    if (roomCunt == 0) {
                        database.child("roomCunt").child("count").setValue(1);
                        roomID = roomCunt++;
                        createGameRoom();
                    } else {
                        joinGameRoom();
                    }
                }
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
                playerCunt = Integer.parseInt(String.valueOf(task.getResult().getValue())) + 1;
                database.child("room").child(roomID + "").child("playerCunt").setValue(playerCunt);
                database.child("room").child(roomID + "").child("players").child(playerCunt + "").setValue(tinyDB.getString("playerName"));                tv_loading.setText("等待玩家中…(" + playerCunt + "/4)");
                listenerGameRoom(roomID);
            }
        });
    }

    public void joinGameRoom() {
        database.child("room").child(roomID + "").child("playerCunt").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                playerCunt = Integer.parseInt(String.valueOf(task.getResult().getValue())) + 1;
                database.child("room").child(roomID + "").child("playerCunt").setValue(playerCunt);
                database.child("room").child(roomID + "").child("players").child(playerCunt + "").setValue(tinyDB.getString("playerName"));
                listenerGameRoom(roomID);
            }
        });
    }

    public void listenerGameRoom(int roomID){
        database.child("room").child(roomID+"").child("playerCunt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null)tv_loading.setText("等待玩家中…(" + snapshot.getValue() + "/4)");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        database.child("room").child(roomID + "").removeValue();
        database.child("roomCunt").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                database.child("roomCunt").child("count").setValue(Integer.parseInt(String.valueOf(task.getResult().getValue())) - 1);
                b_onGame = false;
            }
        });
        finish();
    }
}