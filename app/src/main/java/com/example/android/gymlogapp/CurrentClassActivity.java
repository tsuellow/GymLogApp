package com.example.android.gymlogapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;

import com.example.android.gymlogapp.data.ClientVisitJoin;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.utils.DateMethods;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CurrentClassActivity extends AppCompatActivity implements CurrentClassAdapter.ItemClickListener {


    public static final String SEARCH_STRING = "SEARCH_STRING";
    RecyclerView rvClients;
    GymDatabase mDb;
    CurrentClassAdapter mAdapter;
    Context mContext;
    SearchView searchView;
    Toolbar mToolbar;
    String searchString;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //normal variable instantiation
        setContentView(R.layout.activity_current_class);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_current_class);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = getApplicationContext();
        rvClients = (RecyclerView) findViewById(R.id.rv_current_class_activity);
        mAdapter = new CurrentClassAdapter(mContext, this);
        mDb = GymDatabase.getInstance(getApplicationContext());

        rvClients.setAdapter(mAdapter);
        rvClients.setLayoutManager(new LinearLayoutManager(this));
        if (savedInstanceState==null){
            searchString = "";
        }else{
            searchString=savedInstanceState.getString(SEARCH_STRING);
        }

        populateDataSource(searchString);
    }

    private void populateDataSource(String s) {
        String str = s + "%";

        Date date= DateMethods.getCurrentClassCutoff(new Date());
        Date classTime= DateMethods.getRoundedHour(new Date());
        final String fullHour=new SimpleDateFormat("h:mm a").format(classTime);

        final LiveData<List<ClientVisitJoin>> clients = mDb.clientDao().getCurrentClass(date,str);
        clients.observe(this, new Observer<List<ClientVisitJoin>>() {
            @Override
            public void onChanged(@Nullable List<ClientVisitJoin> clientEntries) {
                mAdapter.setClients(clientEntries);
                mToolbar.setSubtitle(mAdapter.getItemCount()+" "+getString(R.string.participants)+"   "+fullHour);

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_only, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setQuery(searchString,true);


        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    populateDataSource(query);
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    populateDataSource(newText);
                    searchString=newText;

                    return true;
                }
            };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(SEARCH_STRING, searchString);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onItemClickListener(int clientId) {


        showImage(clientId);


    }

    private void showImage(int clientId) {
        String imageFileName = "MEDIUM_" + clientId ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, imageFileName + ".jpg");
        String clientMedium=medium.getAbsolutePath();
        ImageView image = new ImageView(this);
        if (medium.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(clientMedium);
            image.setImageBitmap(bitmap);
        }else{
            image.setImageResource(android.R.drawable.ic_menu_camera);
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this).
                        setView(image);
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(600, 600);
    }
}
