package com.example.snapeditprovs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapeditprovs.adapters.ProjectAdapter;
import com.example.snapeditprovs.models.Project;
import com.example.snapeditprovs.viewmodels.ProjectViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class HomeActivity extends AppCompatActivity implements ProjectAdapter.OnProjectClickListener {

    private ProjectViewModel viewModel;
    private RecyclerView projectsRecyclerView;
    private ProjectAdapter adapter;
    private FloatingActionButton newProjectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        // Initialize RecyclerView
        projectsRecyclerView = findViewById(R.id.projectsRecyclerView);
        setupRecyclerView();

        // New Project Button
        newProjectButton = findViewById(R.id.newProjectButton);
        newProjectButton.setOnClickListener(v -> createNewProject());

        // Observe projects data
        viewModel.getAllProjects().observe(this, projects -> {
            adapter.setProjects(projects);
            
            // Toggle empty state visibility
            View emptyState = findViewById(R.id.emptyState);
            if (projects.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                projectsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                projectsRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Set up empty state button
        Button createFirstProject = findViewById(R.id.createFirstProject);
        createFirstProject.setOnClickListener(v -> createNewProject());
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        projectsRecyclerView.setLayoutManager(layoutManager);
        projectsRecyclerView.setAdapter(adapter);
    }

    private void createNewProject() {
        Intent pickVideoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickVideoIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickVideoIntent.setType("video/*");
        startActivityForResult(pickVideoIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Create new project and start editor
            viewModel.createProject(data.getData(), System.currentTimeMillis());
            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("projectId", viewModel.getLastCreatedProjectId());
            startActivity(intent);
        }
    }

    @Override
    public void onProjectClick(Project project) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("projectId", project.getId());
        startActivity(intent);
    }

    @Override
    public void onProjectLongClick(Project project, View view) {
        // Show context menu for delete, duplicate, rename
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.project_context_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_rename:
                    // Show rename dialog
                    showRenameDialog(project);
                    return true;
                case R.id.action_duplicate:
                    viewModel.duplicateProject(project);
                    return true;
                case R.id.action_delete:
                    viewModel.deleteProject(project);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void showRenameDialog(Project project) {
        // Dialog for renaming project
        EditText input = new EditText(this);
        input.setText(project.getName());
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle("Rename Project")
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    viewModel.renameProject(project, newName);
                }
            })
            .setNegativeButton("Cancel", null);
            
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Open settings
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
