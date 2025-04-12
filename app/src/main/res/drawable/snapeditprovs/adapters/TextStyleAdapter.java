package com.example.snapeditprovs.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snapedit.pro.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextStyleAdapter extends RecyclerView.Adapter<TextStyleAdapter.TextStyleViewHolder> {
    
    private final Context context;
    private List<String> fontStyles;
    private final OnTextStyleClickListener listener;
    private int selectedStylePosition = 0; // Default to first style

    public interface OnTextStyleClickListener {
        void onTextStyleClick(String fontStyle);
    }

    public TextStyleAdapter(Context context, OnTextStyleClickListener listener) {
        this.context = context;
        this.listener = listener;
        // Initialize with default Google Fonts (open-source)
        this.fontStyles = new ArrayList<>(Arrays.asList(
                "Roboto", 
                "Open Sans", 
                "Lato", 
                "Montserrat", 
                "Oswald", 
                "Source Sans Pro", 
                "Raleway",
                "PT Sans",
                "Roboto Condensed",
                "Ubuntu"
        ));
    }

    public void setFontStyles(List<String> fontStyles) {
        this.fontStyles = fontStyles;
        notifyDataSetChanged();
    }

    public String getSelectedStyle() {
        if (selectedStylePosition >= 0 && selectedStylePosition < fontStyles.size()) {
            return fontStyles.get(selectedStylePosition);
        }
        return "Roboto"; // Default font
    }

    @NonNull
    @Override
    public TextStyleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_text_style, parent, false);
        return new TextStyleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextStyleViewHolder holder, int position) {
        String fontStyle = fontStyles.get(position);
        
        holder.fontStyleText.setText(fontStyle);
        
        // Try to apply the font itself if available
        try {
            Typeface typeface = Typeface.create(fontStyle, Typeface.NORMAL);
            holder.fontStyleText.setTypeface(typeface);
        } catch (Exception e) {
            // Fallback to default typeface if the font is not available
            holder.fontStyleText.setTypeface(Typeface.DEFAULT);
        }
        
        // Set selected state
        holder.itemView.setSelected(position == selectedStylePosition);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            selectedStylePosition = holder.getAdapterPosition();
            listener.onTextStyleClick(fontStyle);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return fontStyles.size();
    }

    static class TextStyleViewHolder extends RecyclerView.ViewHolder {
        TextView fontStyleText;

        TextStyleViewHolder(@NonNull View itemView) {
            super(itemView);
            fontStyleText = itemView.findViewById(R.id.fontStyleText);
        }
    }
}
