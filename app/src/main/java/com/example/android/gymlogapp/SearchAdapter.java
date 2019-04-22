package com.example.android.gymlogapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.android.gymlogapp.data.ClientEntry;

import java.io.File;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {




    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mFirstName;
        private TextView mLastName;
        private TextView mIdSearch;
        private TextView mOptionsMenu;
        private ImageView mProfileImage;
        private LinearLayout mBackground;

        public ViewHolder(View itemView){
            super(itemView);

            mFirstName= (TextView) itemView.findViewById(R.id.tv_first_name);
            mLastName= (TextView) itemView.findViewById(R.id.tv_last_name);
            mIdSearch = (TextView) itemView.findViewById(R.id.tv_id_search);
            mOptionsMenu=(TextView) itemView.findViewById(R.id.tv_options_menu);
            mProfileImage=(ImageView) itemView.findViewById(R.id.iv_profile_image);
            mBackground=(LinearLayout) itemView.findViewById(R.id.ly_background_search);
        }
    }

    //now define the data source create a method to supply data from outside the adapter
    private List<ClientEntry> mClients;
    private Context mContext;
    public SearchAdapter(Context context){
        //mClients=clients;
        mContext=context;
    }

    //define method to get and set current data source. usefull when clicking on adapter item
    public void setClients(List<ClientEntry> clients){
        mClients=clients;
        notifyDataSetChanged();
    }

    public List<ClientEntry> getClients(){
        return mClients;
    }


    //now override the main methods
    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View clientView=inflater.inflate(R.layout.adapter_search_view,viewGroup,false);

        ViewHolder viewHolder=new ViewHolder(clientView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchAdapter.ViewHolder viewHolder, int i) {
        final ClientEntry client=mClients.get(i);
        final int clientId=client.getId();

        //now find the tvs in the viewholder and assign them the correct text
        viewHolder.mFirstName.setText(client.getFirstName());
        viewHolder.mLastName.setText(client.getLastName());
        viewHolder.mIdSearch.setText("ID: "+client.getId());

        String idPart = String.valueOf(client.getId());
        String imageFileName = "THUMB_" + idPart ;
        String medFileName = "MEDIUM_" + idPart ;
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File thumbnail = new File(storageDir, imageFileName + ".jpg");
        final File medium = new File(storageDir, medFileName + ".jpg");
        if (thumbnail.exists()) {
            String thumb = thumbnail.getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(thumb);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            viewHolder.mProfileImage.setImageDrawable(roundedBitmapDrawable);
        }
        else{
            viewHolder.mProfileImage.setImageResource(R.drawable.camera);
        }

        viewHolder.mOptionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup=new PopupMenu(mContext,viewHolder.mOptionsMenu);
                //inflate the created menu resource
                popup.inflate(R.menu.mod_pay_options_menu);
                //define what to do on each item click
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.opt_modify:{
                                //Toast.makeText(mContext,"you want to modify this profile",Toast.LENGTH_LONG).show();
                                Intent i = new Intent(mContext,ModifyClientActivity.class);
                                i.putExtra("CLIENT_ID",clientId);
                                mContext.startActivity(i);
                                break;
                            }
                            case R.id.opt_pay:{
                                //Toast.makeText(mContext,"this guy wants to pay",Toast.LENGTH_LONG).show();
                                Intent i = new Intent(mContext,PayClientActivity.class);
                                i.putExtra("CLIENT_ID",clientId);
                                mContext.startActivity(i);
                                break;
                            }
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
        //test
        viewHolder.mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String clientMedium=medium.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(clientMedium);
                ImageView image = new ImageView(mContext);
                image.setImageBitmap(bitmap);

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext).
                                setView(image);
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(600, 600);
            }
        });

        viewHolder.mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext,"background clicked", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(mContext,ClientProfileActivity.class);
                i.putExtra("CLIENT_ID",clientId);
                mContext.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mClients==null){
            return  0;
        }else {
            return mClients.size();
        }
    }




}
