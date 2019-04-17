package com.example.android.gymlogapp;

import android.content.Context;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.android.gymlogapp.data.VisitEntry;

import java.text.SimpleDateFormat;
import java.util.List;

public class CpVisitsAdapter extends RecyclerView.Adapter<CpVisitsAdapter.ViewHolder> {


    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mDate, mTime;
        private TextView mAccess;

        public ViewHolder(View itemView){
            super(itemView);

            mDate= (TextView) itemView.findViewById(R.id.tv_date_cp_visit);
            mTime= (TextView) itemView.findViewById(R.id.tv_time_cp_visit);
            mAccess = (TextView) itemView.findViewById(R.id.vw_access_cp_visit);
        }
    }

    private List<VisitEntry> mVisists;
    private Context mContext;

    public CpVisitsAdapter(Context context){
        mContext=context;
    }

    //define method to get and set current data source. usefull when clicking on adapter item
    public void setVisits(List<VisitEntry> visits){
        mVisists=visits;
        notifyDataSetChanged();
    }

    public List<VisitEntry> getVisits(){
        return mVisists;
    }


    @NonNull
    @Override
    public CpVisitsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View paymentView=inflater.inflate(R.layout.adapter_cp_visits_view,viewGroup,false);

        CpVisitsAdapter.ViewHolder viewHolder=new CpVisitsAdapter.ViewHolder(paymentView);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull CpVisitsAdapter.ViewHolder viewHolder, int i) {
        final VisitEntry visit = mVisists.get(i);

        Resources res = mContext.getResources();
        Drawable drawable = res.getDrawable(R.drawable.bullet_circle);

        //now find the tvs in the viewholder and assign them the correct text
        String date = new SimpleDateFormat("YYYY-MM-dd").format(visit.getTimestamp());
        String time = new SimpleDateFormat("HH:mm:ss").format(visit.getTimestamp());
        viewHolder.mDate.setText(date);
        viewHolder.mTime.setText(time);

         Integer red=mContext.getResources().getColor(R.color.colorRed);
         Integer green= mContext.getResources().getColor(R.color.colorGreen);

        if (visit.getAccess().contentEquals("D")){
            viewHolder.mAccess.setTextColor(red);
        }else{
            viewHolder.mAccess.setTextColor(green);
        }


    }




    @Override
    public int getItemCount() {
        if (mVisists==null){
            return  0;
        }else {
            return mVisists.size();
        }
    }
}
