package com.codeclub.abu.chatcodeclub.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.*;

import com.codeclub.abu.chatcodeclub.R;
import com.codeclub.abu.chatcodeclub.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.*;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecyclerView.ViewHolder viewHolder;
    private final int COMING = 0;
    private final int GOING = 1;
    private final int LOADING = 2;


    // The items to display in your RecyclerView
    private List<Message> messagesList;

    public MessageAdapter(List<Message> messagesList) {
        this.messagesList = messagesList;
    }


    @Override
    public int getItemCount() {
        return this.messagesList.isEmpty() ? 0 : this.messagesList.size();
    }

    @Override
    public int getItemViewType(int i) {
        if (messagesList.get(i) == null)
            return LOADING;
        String fromUserId = messagesList.get(i).getFrom();
        String crrUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (fromUserId.equals(crrUserId))
            return GOING;
        else
            return COMING;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case COMING:
                View v1 = inflater.inflate(R.layout.message_single_layout_coming, viewGroup, false);
                viewHolder = new ComingVHolder(v1);
                break;
            case GOING:
                View v2 = inflater.inflate(R.layout.message_single_layout_going, viewGroup, false);
                viewHolder = new GoingVHolder(v2);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        long timestamp;
        switch (viewHolder.getItemViewType()) {
            case COMING:
                ComingVHolder vh1 = (ComingVHolder) viewHolder;
                vh1.comingMsg.setText(messagesList.get(i).getMessage());
                ////
                timestamp = messagesList.get(i).getTime();
                setTimeToView(timestamp, vh1.messageTime);
                break;

            case GOING:
                GoingVHolder vh2 = (GoingVHolder) viewHolder;
                vh2.goingMsg.setText(messagesList.get(i).getMessage());
                timestamp = messagesList.get(i).getTime();
                setTimeToView(timestamp, vh2.messageTime);
                break;

            case LOADING:
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
                loadingViewHolder.progressBar.setIndeterminate(true);
                break;
        }
    }

    private class ComingVHolder extends RecyclerView.ViewHolder {
        private TextView comingMsg;
        private TextView messageTime;

        public ComingVHolder(View v1) {
            super(v1);
            comingMsg = (TextView) v1.findViewById(R.id.message_single_text_c);
            messageTime = (TextView) v1.findViewById(R.id.message_single_layout_c_time);

        }
    }

    private class GoingVHolder extends RecyclerView.ViewHolder {
        private TextView goingMsg;
        private TextView messageTime;

        public GoingVHolder(View v2) {
            super(v2);
            goingMsg = (TextView) v2.findViewById(R.id.message_single_text_g);
            messageTime = (TextView) v2.findViewById(R.id.message_single_layout_g_time);

        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);
        }
    }

    private void setTimeToView(long timestamp, TextView view) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        String timeString = sdf.format(date);
        view.setText(timeString);
    }

}