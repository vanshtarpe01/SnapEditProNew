package com.example.snapeditprovs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snapedit.pro.R;
import com.snapedit.pro.models.Transition;

import java.util.ArrayList;
import java.util.List;

public class TransitionAdapter extends RecyclerView.Adapter<TransitionAdapter.TransitionViewHolder> {
    
    private final Context context;
    private List<Transition> transitions;
    private final OnTransitionClickListener listener;
    private int selectedTransitionPosition = -1;

    public interface OnTransitionClickListener {
        void onTransitionClick(Transition transition);
    }

    public TransitionAdapter(Context context, OnTransitionClickListener listener) {
        this.context = context;
        this.transitions = new ArrayList<>();
        this.listener = listener;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transition, parent, false);
        return new TransitionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransitionViewHolder holder, int position) {
        Transition transition = transitions.get(position);
        
        holder.transitionName.setText(transition.getName());
        
        // Assign icon based on transition type
        switch (transition.getType()) {
            case "fade":
                holder.transitionIcon.setImageResource(R.drawable.ic_effect); // Using ic_effect as placeholder
                break;
            case "dissolve":
                holder.transitionIcon.setImageResource(R.drawable.ic_effect);
                break;
            case "wipe":
                holder.transitionIcon.setImageResource(R.drawable.ic_effect);
                break;
            case "slide":
                holder.transitionIcon.setImageResource(R.drawable.ic_effect);
                break;
            case "zoom":
                holder.transitionIcon.setImageResource(R.drawable.ic_effect);
                break;
            default:
                holder.transitionIcon.setImageResource(R.drawable.ic_effect);
                break;
        }
        
        // Set selected state
        holder.itemView.setSelected(position == selectedTransitionPosition);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            selectedTransitionPosition = holder.getAdapterPosition();
            listener.onTransitionClick(transition);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return transitions.size();
    }

    static class TransitionViewHolder extends RecyclerView.ViewHolder {
        ImageView transitionIcon;
        TextView transitionName;

        TransitionViewHolder(@NonNull View itemView) {
            super(itemView);
            transitionIcon = itemView.findViewById(R.id.transitionIcon);
            transitionName = itemView.findViewById(R.id.transitionName);
        }
    }
}
