package com.example.tower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    /*
    Static variables loggedIn and id are updated when a user is logged in.
     */
    public static Boolean loggedIn = false;
    public static long id = 0;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /*
    Static list of textbooks and count are created in order to ensure the grid of books
    doesn't update until the user hits refresh.
     */
    static ArrayList<Textbook> textbooks = new ArrayList<>();
    static int count = 0;
    GridView gridView;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helper helper = new Helper(this);
        //helper.deleteAllBooks();
        //if (count == 0) helper.floodDatabase(25);


        DatabaseReference reference = database.getReference().child("textbooks");
        gridView = findViewById(R.id.main_grid_view);
        if (count == 0) {
            displayTextbooks(this, gridView);
        }

        else {
            gridView.setAdapter(new TextbookAdapter(this,textbooks, getLocalClassName()));
        }

        //Initialize and Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Home Selected
        bottomNavigationView.setSelectedItemId(R.id.home);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home:
                        return true;
                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(), Search.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        if(loggedIn == false) {
                            startActivity(new Intent(getApplicationContext(), Profile.class));
                            overridePendingTransition(0, 0);
                        }
                        else {
                            startActivity(new Intent(getApplicationContext(), ProfileLogin.class));
                            overridePendingTransition(0,0);
                        }
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }

                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final String uID = user.getUid();
            DatabaseReference reference1 = database.getReference().child("students");
            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        if (uID == null) break;
                        if (snapshot.child("uID").getValue() == null) break;
                        if (snapshot.child("uID").getValue().toString().equals(uID)) {
                            String id = snapshot.child("id").getValue().toString();
                            MainActivity.loggedIn = true;
                            MainActivity.id = Long.parseLong(id);

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    public void displayTextbooks(final Context context, final GridView gridView) {
        DatabaseReference reference = database.getReference().child("textbooks");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Stack<Textbook> textbookStack = new Stack<>();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Textbook textbook = snapshot.getValue(Textbook.class);
                    textbookStack.push(textbook);
                }
                int max = 0;
                while(!textbookStack.isEmpty()) {
                    if(max == 20) break;
                    textbooks.add(textbookStack.pop());
                    max++;
                }

                gridView.setAdapter(new TextbookAdapter(context, textbooks, getLocalClassName()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        count++;
    }

    public void getHID(final String uID) {
        DatabaseReference reference1 = database.getReference().child("students");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if (snapshot.child("uID").getValue().toString().equals(uID)) {
                        String id = snapshot.child("id").getValue().toString();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void refreshBooks(View view) {
        textbooks.clear();
        displayTextbooks(this, gridView);
    }


}
