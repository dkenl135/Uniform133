package com.ironfactory.donation.controllers.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ironfactory.donation.controllers.RecyclerInScrollLayoutManager;
import com.ironfactory.donation.dtos.TimelineCommentCardDto;

import java.util.ArrayList;

public class TimelineCommentRecyclerView extends RecyclerView {
    private ArrayList<TimelineCommentCardDto> timelineCommentCardDtos;
    private static final String TAG = "CommentRecyclerView";

    private TimelineCommentAdapter adapter;

    public TimelineCommentRecyclerView(Context context) {
        this(context, null);
    }

    public TimelineCommentRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimelineCommentRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        timelineCommentCardDtos = new ArrayList<>();
        setLayoutManager(new RecyclerInScrollLayoutManager(getContext()));
        adapter = new TimelineCommentAdapter(timelineCommentCardDtos);
        setAdapter(adapter);
    }

    public void setTimelineCommentCardDtos(ArrayList<TimelineCommentCardDto> timelineCommentCardDtos) {
        this.timelineCommentCardDtos.clear();
        this.timelineCommentCardDtos.addAll(timelineCommentCardDtos);
        Log.d(TAG, "setTimelineComments");
        getAdapter().notifyDataSetChanged();
    }

    private class TimelineCommentAdapter extends Adapter<TimelineCommentAdapter.ViewHolder> {
        private ArrayList<TimelineCommentCardDto> timelineCommentCardDtos;

        public TimelineCommentAdapter(ArrayList<TimelineCommentCardDto> timelineCommentCardDtos) {
            this.timelineCommentCardDtos = timelineCommentCardDtos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            TimelineCommentCardView timelineCommentCardView = new TimelineCommentCardView(getContext());
            timelineCommentCardView.setTag("TimelineCommentCardView");

            return new ViewHolder(timelineCommentCardView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            viewHolder.timelineCommentCard.setTimelineCommentCardDtos(timelineCommentCardDtos.get(position));
            viewHolder.timelineCommentCard.setOnTimelineActionCallback(new TimelineCardView.TimelineActionCallback() {
                @Override
                public void onDelete() {
                    timelineCommentCardDtos.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return timelineCommentCardDtos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TimelineCommentCardView timelineCommentCard;

            public ViewHolder(View view) {
                super(view);

                timelineCommentCard = (TimelineCommentCardView) view.findViewWithTag("TimelineCommentCardView");
            }
        }
    }
}