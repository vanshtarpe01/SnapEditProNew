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
import com.snapedit.pro.models.Project;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    
    private final Context context;
    private List<Project> projects;
    private final OnProjectClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
        void onProjectLongClick(Project project, View view);
    }

    public ProjectAdapter(Context context) {
        this.context = context;
        this.projects = new ArrayList<>();
        this.listener = (OnProjectClickListener) context;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        
        holder.projectName.setText(project.getName());
        holder.projectDate.setText(dateFormat.format(new Date(project.getLastModified())));
        
        // Format duration as mm:ss
        int minutes = (int) (project.getDuration() / 60);
        int seconds = (int) (project.getDuration() % 60);
        holder.projectDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        
        // Load thumbnail
        if (project.getThumbnailPath() != null) {
            Glide.with(context)
                    .load(new File(project.getThumbnailPath()))
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .centerCrop()
                    .into(holder.projectThumbnail);
        } else {
            holder.projectThumbnail.setImageResource(R.drawable.placeholder_thumbnail);
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(project));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onProjectLongClick(project, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        ImageView projectThumbnail;
        TextView projectName;
        TextView projectDate;
        TextView projectDuration;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectThumbnail = itemView.findViewById(R.id.projectThumbnail);
            projectName = itemView.findViewById(R.id.projectName);
            projectDate = itemView.findViewById(R.id.projectDate);
            projectDuration = itemView.findViewById(R.id.projectDuration);
        }
    }
}
