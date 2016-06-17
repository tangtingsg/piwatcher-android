package sg.nus.tangting.PiWatcher.Activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sg.nus.tangting.PiWatcher.Movement;
import sg.nus.tangting.PiWatcher.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    private LayoutInflater mInflater;
    private List<Movement> mList = null;

    public RecyclerAdapter(Context context, List<Movement> dataList) {
        this.mInflater = LayoutInflater.from(context);
        this.mList = dataList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_tv;
        public ViewHolder(View view){
            super(view);
            item_tv = (TextView)view.findViewById(R.id.textView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_recycler_layout, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        holder.item_tv.setText(mList.get(position).getMessage());
    }

    public void addItems(List<Movement> newList) {
        mList.addAll(0,newList);
        notifyDataSetChanged();
    }

    public boolean isEmpty(){
        return mList.isEmpty();
    }

    public long getLatestUpdateTime(){
        if (mList.isEmpty()){
            return -1;
        }else{
            return mList.get(0).getTimestamp();
        }
    }

}
