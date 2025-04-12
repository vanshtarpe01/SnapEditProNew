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
import com.snapedit.pro.models.Filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {
    
    private final Context context;
    private List<Filter> filters;
    private final OnFilterClickListener listener;
    private int selectedFilterPosition = 0; // Default to "Normal" filter (position 0)

    public interface OnFilterClickListener {
        void onFilterClick(Filter filter);
    }

    public FilterAdapter(Context context, OnFilterClickListener listener) {
        this.context = context;
        this.filters = new ArrayList<>();
        this.listener = listener;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        Filter filter = filters.get(position);
        
        holder.filterName.setText(filter.getName());
        
        // Load filter preview thumbnail if available
        if (filter.getThumbnailPath() != null) {
            Glide.with(context)
                    .load(new File(filter.getThumbnailPath()))
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .centerCrop()
                    .into(holder.filterThumbnail);
        } else {
            // Use default filter preview thumbnail
            holder.filterThumbnail.setImageResource(R.drawable.placeholder_thumbnail);
            
            // Apply filter to thumbnail (in a real app this would show a preview)
            // This is just a placeholder for filter visualization
            if (filter.getName().equals("Black & White")) {
                holder.filterThumbnail.setColorFilter(0x88000000);
            } else if (filter.getName().equals("Warm")) {
                holder.filterThumbnail.setColorFilter(0x33FFAA00);
            } else if (filter.getName().equals("Cool")) {
                holder.filterThumbnail.setColorFilter(0x330088FF);
            } else {
                holder.filterThumbnail.clearColorFilter();
            }
        }
        
        // Set selected state
        holder.itemView.setSelected(position == selectedFilterPosition);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            selectedFilterPosition = holder.getAdapterPosition();
            listener.onFilterClick(filter);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView filterThumbnail;
        TextView filterName;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            filterThumbnail = itemView.findViewById(R.id.filterThumbnail);
            filterName = itemView.findViewById(R.id.filterName);
        }
    }
}
