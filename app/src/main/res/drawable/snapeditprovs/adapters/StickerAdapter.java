package com.example.snapeditprovs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snapedit.pro.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder> {
    
    private final Context context;
    private List<String> stickerPaths;
    private final OnStickerClickListener listener;

    public interface OnStickerClickListener {
        void onStickerClick(String stickerPath);
    }

    public StickerAdapter(Context context, OnStickerClickListener listener) {
        this.context = context;
        this.stickerPaths = new ArrayList<>();
        this.listener = listener;
    }

    public void setStickerPaths(List<String> stickerPaths) {
        this.stickerPaths = stickerPaths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false);
        return new StickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        String stickerPath = stickerPaths.get(position);
        
        // Load sticker image
        if (stickerPath.startsWith("asset://")) {
            // Load from assets (bundled stickers)
            String assetPath = stickerPath.substring(9);
            Glide.with(context)
                    .load(Uri.parse("file:///android_asset/" + assetPath))
                    .into(holder.stickerImage);
        } else {
            // Load from file
            Glide.with(context)
                    .load(new File(stickerPath))
                    .into(holder.stickerImage);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            listener.onStickerClick(stickerPath);
        });
    }

    @Override
    public int getItemCount() {
        return stickerPaths.size();
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {
        ImageView stickerImage;

        StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            stickerImage = itemView.findViewById(R.id.stickerImage);
        }
    }
}
