package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * activity MainActivity displays the menu
 * the menu allows the user to go to the Cookbook activity,
 * the NewRecipe activity or to exit the application
 */
public class MainActivity extends AppCompatActivity {

    private String userEmail;
    private PopupWindow infoPopup;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve data sent by other activity
        Intent receivedIntent = getIntent();
        userEmail = receivedIntent.getExtras().getString("user_email");
        String action = receivedIntent.getExtras().getString("action");

        coordinatorLayout = findViewById(R.id.main_activity_coordinator_layout);
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
        ImageView logoutBtn = (ImageView) findViewById(R.id.exit_btn);
        logoutBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent logoutIntent = new Intent(MainActivity.this, Login.class);
                startActivity(logoutIntent);
            }
        });

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

                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View infoPopupView = inflater.inflate(R.layout.app_info_popup, null);

                ImageButton doneButton = infoPopupView.findViewById(R.id.info_button);
                TextView infoTitle = infoPopupView.findViewById(R.id.info_title);
                TextView infoText = infoPopupView.findViewById(R.id.info_text_view);
                TextView infoText2 = infoPopupView.findViewById(R.id.info_text_view2);
                TextView infoText3 = infoPopupView.findViewById(R.id.info_text_view3);
                TextView infoText4 = infoPopupView.findViewById(R.id.info_text_view4);

                infoTitle.setText(R.string.menu_info_title);
                infoText.setText(R.string.menu_info);
                infoText2.setText(R.string.enjoy);
                infoText3.setVisibility(View.GONE);
                infoText4.setVisibility(View.GONE);

                infoPopup = new PopupWindow(infoPopupView, 1100, 1000, true);
                infoPopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        infoPopup.dismiss();
                    }
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }
}
