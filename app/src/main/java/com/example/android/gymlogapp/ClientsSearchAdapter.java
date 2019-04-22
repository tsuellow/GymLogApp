package com.example.android.gymlogapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.gymlogapp.data.ClientEntry;

import java.io.File;
import java.util.List;

public class ClientsSearchAdapter extends RecyclerView.Adapter<ClientsSearchAdapter.ViewHolder> {



    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mFirstName;
        private TextView mLastName;
        private TextView mId;
        private ImageView mProfileImage;
        //private int mClientId;

        public ViewHolder(View itemView){
            super(itemView);
            mId=(TextView) itemView.findViewById(R.id.tv_id_search_lim);
            mFirstName= (TextView) itemView.findViewById(R.id.tv_first_name_lim);
            mLastName= (TextView) itemView.findViewById(R.id.tv_last_name_lim);
            mProfileImage=(ImageView) itemView.findViewById(R.id.iv_profile_image_lim);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int clientId=mClients.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(clientId);
        }
    }

    //now define the data source create a method to supply data from outside the adapter
    private List<ClientEntry> mClients;
    private Context mContext;
    final private ItemClickListener mItemClickListener;
    public ClientsSearchAdapter(Context context, ItemClickListener itemClickListener){
        mItemClickListener=itemClickListener;
        mContext=context;
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
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
    public ClientsSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View clientView=inflater.inflate(R.layout.adapter_client_search_view,viewGroup,false);

        ClientsSearchAdapter.ViewHolder viewHolder=new ClientsSearchAdapter.ViewHolder(clientView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ClientsSearchAdapter.ViewHolder viewHolder, int i) {
        final ClientEntry client=mClients.get(i);


        //now find the tvs in the viewholder and assign them the correct text
        viewHolder.mId.setText("ID: "+client.getId());
        viewHolder.mFirstName.setText(client.getFirstName());
        viewHolder.mLastName.setText(client.getLastName());

        String idPart = String.valueOf(client.getId());
        String imageFileName = "THUMB_" + idPart ;
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File thumbnail = new File(storageDir, imageFileName + ".jpg");
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