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

import com.example.android.gymlogapp.data.ClientVisitJoin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class CurrentClassAdapter extends RecyclerView.Adapter<CurrentClassAdapter.ViewHolder> {


    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mId;
        private TextView mFirstName;
        private TextView mLastName;
        private TextView mArrivalTime;
        private ImageView mProfileImage;
        //private int mClientId;

        public ViewHolder(View itemView){
            super(itemView);

            mId=(TextView) itemView.findViewById(R.id.tv_id_currclass);
            mFirstName= (TextView) itemView.findViewById(R.id.tv_first_name_currclass);
            mLastName= (TextView) itemView.findViewById(R.id.tv_last_name_currclass);
            mArrivalTime=(TextView) itemView.findViewById(R.id.tv_arrival_time_currclass);
            mProfileImage=(ImageView) itemView.findViewById(R.id.iv_profile_image_currclass);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int clientId=mClients.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(clientId);
        }
    }

    //now define the data source create a method to supply data from outside the adapter
    private List<ClientVisitJoin> mClients;
    private Context mContext;
    final private CurrentClassAdapter.ItemClickListener mItemClickListener;
    public CurrentClassAdapter(Context context, CurrentClassAdapter.ItemClickListener itemClickListener){
        mItemClickListener=itemClickListener;
        mContext=context;
    }

    public interface ItemClickListener {
        void onItemClickListener(int clientId);
    }

    //define method to get and set current data source. usefull when clicking on adapter item
    public void setClients(List<ClientVisitJoin> clients){
        mClients=clients;
        notifyDataSetChanged();
    }

    public List<ClientVisitJoin> getClients(){
        return mClients;
    }


    @NonNull
    @Override
    public CurrentClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View clientView=inflater.inflate(R.layout.adapter_current_class_view,viewGroup,false);

        CurrentClassAdapter.ViewHolder viewHolder=new CurrentClassAdapter.ViewHolder(clientView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentClassAdapter.ViewHolder viewHolder, int i) {
        final ClientVisitJoin client=mClients.get(i);
        //mClientId=client.getId();

        //now find the tvs in the viewholder and assign them the correct text
        viewHolder.mId.setText("ID: "+client.getId());
        viewHolder.mFirstName.setText(client.getFirstName());
        viewHolder.mLastName.setText(client.getLastName());
        viewHolder.mArrivalTime.setText(new SimpleDateFormat("HH:mm:ss").format(client.getTimestamp()));

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
