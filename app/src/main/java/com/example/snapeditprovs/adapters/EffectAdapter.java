package com.example.snapeditprovs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snapeditprovs.R;
import com.example.snapeditprovs.model.Effect;

import java.io.File;
import java.util.List;

/**
 * Adapter for displaying video effects
 */
public class EffectAdapter extends RecyclerView.Adapter<EffectAdapter.EffectViewHolder> {
    
    private final Context context;
    private final List<Effect> effects;
    private OnEffectClickListener listener;
    
    public interface OnEffectClickListener {
        void onEffectClick(Effect effect);
    }
    
    public EffectAdapter(Context context, List<Effect> effects) {
        this.context = context;
        this.effects = effects;
    }
    
    public void setOnEffectClickListener(OnEffectClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EffectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_effect, parent, false);
        return new EffectViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EffectViewHolder holder, int position) {
        Effect effect = effects.get(position);
        holder.bind(effect);
    }
    
    @Override
    public int getItemCount() {
        return effects.size();
    }
    
    class EffectViewHolder extends RecyclerView.ViewHolder {
        
        private final CardView cardView;
        private final ImageView thumbnailImageView;
        private final TextView nameTextView;
        
        public EffectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.effect_card);
            thumbnailImageView = itemView.findViewById(R.id.effect_thumbnail);
            nameTextView = itemView.findViewById(R.id.effect_name);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEffectClick(effects.get(position));
                }
            });
        }
        
        public void bind(Effect effect) {
            nameTextView.setText(effect.getName());
            
            // Load effect thumbnail
            if (effect.getThumbnailPath() != null && !effect.getThumbnailPath().isEmpty()) {
                Glide.with(context)
                        .load(new File(effect.getThumbnailPath()))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_effect)
                        .error(R.drawable.placeholder_effect)
                        .into(thumbnailImageView);
            } else {
                // Use a default image for this effect
                thumbnailImageView.setImageResource(R.drawable.placeholder_effect);
            }
        }
    }
}
