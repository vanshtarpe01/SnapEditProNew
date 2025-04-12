package com.example.snapeditprovs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.snapedit.pro.models.ExportSettings;
import com.snapedit.pro.models.Project;
import com.snapedit.pro.viewmodels.ExportViewModel;

import java.io.File;

import missing.namespace.R;

public class ExportActivity extends AppCompatActivity {

    private ExportViewModel viewModel;
    private PlayerView previewPlayerView;
    private ExoPlayer previewPlayer;
    private ProgressBar exportProgressBar;
    private TextView exportStatusText, bitrateText, framerateText;
    private Button exportButton, shareButton;
    private RadioGroup resolutionRadioGroup;
    private SeekBar bitrateSeekBar, framerateSeekBar;
    private long projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // Get project ID from intent
        projectId = getIntent().getLongExtra("projectId", -1);
        if (projectId == -1) {
            Toast.makeText(this, "Error loading project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ExportViewModel.class);
        viewModel.loadProject(projectId);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Export Video");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();
        setupExoPlayer();
        setupListeners();

        // Observe export progress
        viewModel.getExportProgress().observe(this, progress -> {
            exportProgressBar.setProgress(progress);
            exportStatusText.setText("Exporting: " + progress + "%");
        });

        // Observe export completion
        viewModel.getExportCompleted().observe(this, completed -> {
            if (completed) {
                exportProgressBar.setVisibility(View.GONE);
                exportStatusText.setText("Export completed");
                shareButton.setEnabled(true);
                
                // Show export completed dialog
                showExportCompletedDialog(viewModel.getExportedFilePath());
            }
        });

        // Observe export error
        viewModel.getExportError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                exportProgressBar.setVisibility(View.GONE);
                exportStatusText.setText("Export failed: " + error);
                exportButton.setEnabled(true);
            }
        });

        // Observe project data
        viewModel.getProject().observe(this, this::updatePreviewPlayer);
    }

    private void initViews() {
        previewPlayerView = findViewById(R.id.previewPlayerView);
        exportProgressBar = findViewById(R.id.exportProgressBar);
        exportStatusText = findViewById(R.id.exportStatusText);
        bitrateText = findViewById(R.id.bitrateText);
        framerateText = findViewById(R.id.framerateText);
        exportButton = findViewById(R.id.exportButton);
        shareButton = findViewById(R.id.shareButton);
        resolutionRadioGroup = findViewById(R.id.resolutionRadioGroup);
        bitrateSeekBar = findViewById(R.id.bitrateSeekBar);
        framerateSeekBar = findViewById(R.id.framerateSeekBar);

        // Initialize UI state
        exportProgressBar.setVisibility(View.GONE);
        shareButton.setEnabled(false);
    }

    private void setupExoPlayer() {
        previewPlayer = new SimpleExoPlayer.Builder(this).build();
        previewPlayerView.setPlayer(previewPlayer);
    }

    private void setupListeners() {
        // Resolution radio group
        resolutionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int resolution;
            if (checkedId == R.id.radio720p) {
                resolution = 720;
            } else if (checkedId == R.id.radio1080p) {
                resolution = 1080;
            } else {
                resolution = 2160; // 4K
            }
            viewModel.setExportResolution(resolution);
        });

        // Bitrate seek bar
        bitrateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int bitrate = 2000 + (progress * 100); // 2000kbps to 12000kbps
                bitrateText.setText(bitrate + " kbps");
                viewModel.setExportBitrate(bitrate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Framerate seek bar
        framerateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int framerate = 24 + progress; // 24fps to 60fps
                framerateText.setText(framerate + " fps");
                viewModel.setExportFramerate(framerate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Export button
        exportButton.setOnClickListener(v -> {
            // Start export process
            ExportSettings settings = viewModel.getExportSettings();
            String outputFileName = "SnapEdit_Export_" + System.currentTimeMillis() + ".mp4";
            
            exportButton.setEnabled(false);
            exportProgressBar.setVisibility(View.VISIBLE);
            exportStatusText.setText("Preparing export...");
            
            viewModel.exportProject(outputFileName);
        });

        // Share button
        shareButton.setOnClickListener(v -> {
            String exportedFilePath = viewModel.getExportedFilePath();
            if (exportedFilePath != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/mp4");
                Uri videoUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        new File(exportedFilePath));
                shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share video via"));
            }
        });
    }

    private void updatePreviewPlayer(Project project) {
        if (project != null && !project.getVideoClips().isEmpty()) {
            // Load the first clip for preview
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(project.getVideoClips().get(0).getPath()));
            previewPlayer.setMediaItem(mediaItem);
            previewPlayer.prepare();
            
            // Set initial values for export settings
            bitrateSeekBar.setProgress(30); // 5000 kbps
            framerateSeekBar.setProgress(6); // 30 fps
            resolutionRadioGroup.check(R.id.radio1080p); // Default to 1080p
        }
    }

    private void showExportCompletedDialog(String filePath) {
        if (filePath == null) return;
        
        new AlertDialog.Builder(this)
            .setTitle("Export Completed")
            .setMessage("Your video has been exported to:\n" + filePath)
            .setPositiveButton("Share", (dialog, which) -> {
                // Trigger share button click
                shareButton.performClick();
            })
            .setNegativeButton("OK", null)
            .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (previewPlayer != null) {
            previewPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previewPlayer != null) {
            previewPlayer.release();
            previewPlayer = null;
        }
    }
}
