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

public class SendReminderAdapter extends RecyclerView.Adapter<SendReminderAdapter.ViewHolder> {



    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mFirstName;
        private TextView mLastName;
        private TextView mPhone;
        private ImageView mProfileImage;
        //private int mClientId;

        public ViewHolder(View itemView){
            super(itemView);

            mFirstName= (TextView) itemView.findViewById(R.id.tv_first_name_rem);
            mLastName= (TextView) itemView.findViewById(R.id.tv_last_name_rem);
            mPhone=(TextView) itemView.findViewById(R.id.tv_tel_rem);
            mProfileImage=(ImageView) itemView.findViewById(R.id.iv_profile_image_rem);
            mPhone.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            String clientPhone=mClients.get(getAdapterPosition()).getPhone();
            String clientFirstName=mClients.get(getAdapterPosition()).getFirstName();
            mItemClickListener.onItemClickListener(clientPhone, clientFirstName);
            mPhone.setTextColor(mContext.getResources().getColor(R.color.colorLight));
        }
    }

    private List<ClientEntry> mClients;
    private Context mContext;
    final private SendReminderAdapter.ItemClickListener mItemClickListener;
    public SendReminderAdapter(Context context, SendReminderAdapter.ItemClickListener itemClickListener){
        mItemClickListener=itemClickListener;
        mContext=context;
    }

    public interface ItemClickListener {
        void onItemClickListener(String phone, String firstName);
    }

    //define method to get and set current data source. usefull when clicking on adapter item
    public void setClients(List<ClientEntry> clients){
        mClients=clients;
        notifyDataSetChanged();
    }

    public List<ClientEntry> getClients(){
        return mClients;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View clientView=inflater.inflate(R.layout.adapter_send_reminder_view,viewGroup,false);

        SendReminderAdapter.ViewHolder viewHolder=new SendReminderAdapter.ViewHolder(clientView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final ClientEntry client=mClients.get(i);

        //now find the tvs in the viewholder and assign them the correct text
        viewHolder.mPhone.setText(client.getPhone());
        viewHolder.mPhone.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
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
            viewHolder.mProfileImage.setImageResource(android.R.drawable.ic_menu_camera);
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
