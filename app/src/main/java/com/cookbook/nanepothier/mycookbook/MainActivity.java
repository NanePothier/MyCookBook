package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * activity MainActivity displays the menu
 * the menu allows the user to go to the Cookbook activity,
 * the NewRecipe activity or to exit the application
 */
public class MainActivity extends AppCompatActivity {

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve data sent by other activity
        Intent receivedIntent = getIntent();
        userEmail = receivedIntent.getExtras().getString("user_email");
        String action = receivedIntent.getExtras().getString("action");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // go to cookbook activivty
        ImageView cookbookBtn = (ImageView) findViewById(R.id.cookbook_btn);
        cookbookBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(MainActivity.this, Cookbook.class);
                intent.putExtra("user_email", userEmail);
                startActivity(intent);
            }
        });

        // go to new recipe activity
        ImageView newRecipeBtn = (ImageView) findViewById(R.id.newrecipe_btn);
        newRecipeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(MainActivity.this, NewRecipe.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("StatusIndicator", "NewRecipe");
                startActivity(intent);
            }
        });

        // exit activity
        ImageView exitBtn = (ImageView) findViewById(R.id.exit_btn);
        exitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                System.exit(0);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        handleAction(action);
    }

    // display message to user depending on what activity user came from
    public void handleAction(String takeAction){

        if(takeAction.equals("save_action")){
            Snackbar.make(findViewById(R.id.main_activity_coordinator_layout), "Recipe saved successfully", Snackbar.LENGTH_LONG)
            .show();
        }

        if(takeAction.equals("cancel_action")){
            Snackbar.make(findViewById(R.id.main_activity_coordinator_layout), "Recipe not saved", Snackbar.LENGTH_LONG)
            .show();
        }
    }

    // toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_info:


            default:
                return super.onOptionsItemSelected(item);

        }

    }
}
