package cn.talkcloud.talksdkdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.talkcloud.talksdkdemo.R;
import cn.talkcloud.talksdkdemo.entity.ChatMessageModel;

/**
 * @author: Liys
 * create date: 2018/7/17
 * 描述：
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //视图
    private static final int TYPE_OTHER = 0;
    private static final int TYPE_ME = 1;

    private List<ChatMessageModel> mList;
    public ChatMessageAdapter(List<ChatMessageModel> list){
        this.mList = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_OTHER)
            return new LeftViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.left_message,parent,false));
        if (viewType == TYPE_ME)
            return new RightViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rigth_message,parent,false));
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LeftViewHolder) {
            ((LeftViewHolder) holder).tv_content.setText(mList.get(position).getChatmessage());
        }else {
            ((RightViewHolder)holder).tv_content.setText(mList.get(position).getChatmessage());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size()>0?mList.size():0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getType() == TYPE_OTHER)
            return TYPE_OTHER;
        return TYPE_ME;

    }


    class LeftViewHolder extends RecyclerView.ViewHolder{

        public ImageView iv_hand;
        public TextView tv_content;
        public LeftViewHolder(View itemView) {
            super(itemView);
            iv_hand = (ImageView) itemView.findViewById(R.id.iv_hand);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
        }

    }

    class RightViewHolder extends RecyclerView.ViewHolder{

        public ImageView iv_hand;
        public TextView tv_content;

        public RightViewHolder(View itemView) {
            super(itemView);
            iv_hand = (ImageView) itemView.findViewById(R.id.iv_hand);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);

        }

    }


}
