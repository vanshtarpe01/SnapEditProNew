package com.example.snapeditprovs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snapedit.pro.R;
import com.snapedit.pro.models.VideoClip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ClipViewHolder> {
    
    private final Context context;
    private List<VideoClip> clips;
    private final OnClipClickListener listener;
    private int selectedClipPosition = -1;
    private float timelineScale = 1.0f;

    public interface OnClipClickListener {
        void onClipClick(VideoClip clip);
    }

    public TimelineAdapter(Context context, OnClipClickListener listener) {
        this.context = context;
        this.clips = new ArrayList<>();
        this.listener = listener;
    }

    public void setClips(List<VideoClip> clips) {
        this.clips = clips;
        notifyDataSetChanged();
    }

    public void setTimelineScale(float scale) {
        this.timelineScale = scale;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new ClipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClipViewHolder holder, int position) {
        VideoClip clip = clips.get(position);
        
        // Format duration as seconds
        int seconds = (int) clip.getDuration();
        holder.clipDuration.setText(String.format(Locale.getDefault(), "%ds", seconds));
        
        // Load thumbnail
        if (clip.getThumbnailPath() != null) {
            Glide.with(context)
                    .load(new File(clip.getThumbnailPath()))
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .centerCrop()
                    .into(holder.clipThumbnail);
        } else {
            holder.clipThumbnail.setImageResource(R.drawable.placeholder_thumbnail);
        }
        
        // Set item width based on duration and scale
        int baseWidth = (int) (300 * clip.getDuration() / 10.0); // 300dp for 10 seconds
        int scaledWidth = (int) (baseWidth * timelineScale);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = Math.max(150, scaledWidth); // Min width of 150dp
        holder.itemView.setLayoutParams(layoutParams);
        
        // Set selected state
        holder.itemView.setSelected(position == selectedClipPosition);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            selectedClipPosition = holder.getAdapterPosition();
            listener.onClipClick(clip);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return clips.size();
    }

    static class ClipViewHolder extends RecyclerView.ViewHolder {
        ImageView clipThumbnail;
        TextView clipDuration;

        ClipViewHolder(@NonNull View itemView) {
            super(itemView);
            clipThumbnail = itemView.findViewById(R.id.clipThumbnail);
            clipDuration = itemView.findViewById(R.id.clipDuration);
        }
    }
}
