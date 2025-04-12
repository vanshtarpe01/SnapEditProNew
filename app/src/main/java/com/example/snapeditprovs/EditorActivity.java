package com.example.snapeditprovs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapeditprovs.adapters.FilterAdapter;
import com.example.snapeditprovs.adapters.TextStyleAdapter;
import com.example.snapeditprovs.adapters.TimelineAdapter;
import com.example.snapeditprovs.adapters.TransitionAdapter;
import com.example.snapeditprovs.models.Project;
import com.example.snapeditprovs.viewmodels.EditorViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditorActivity extends AppCompatActivity {

    private EditorViewModel viewModel;
    private PlayerView playerView;
    private ExoPlayer player;
    private RecyclerView timelineRecyclerView;
    private TimelineAdapter timelineAdapter;
    private ImageButton cutButton, filterButton, textButton, audioButton, effectButton;
    private FloatingActionButton playPauseButton, exportButton;
    private long projectId;
    private Project currentProject;
    
    private BottomSheetBehavior<View> timelineSheetBehavior;
    private View timelineSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get project ID from intent
        projectId = getIntent().getLongExtra("projectId", -1);
        if (projectId == -1) {
            Toast.makeText(this, "Error loading project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        viewModel.loadProject(projectId);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Project");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();
        setupBottomSheets();
        setupExoPlayer();
        setupTimeline();
        
        // Set up toolbar buttons
        setupToolbarButtons();

        // Observe project data
        viewModel.getProject().observe(this, project -> {
            currentProject = project;
            updateUI(project);
        });
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        timelineRecyclerView = findViewById(R.id.timelineRecyclerView);
        
        cutButton = findViewById(R.id.cutButton);
        filterButton = findViewById(R.id.filterButton);
        textButton = findViewById(R.id.textButton);
        audioButton = findViewById(R.id.audioButton);
        effectButton = findViewById(R.id.effectButton);
        
        playPauseButton = findViewById(R.id.playPauseButton);
        exportButton = findViewById(R.id.exportButton);
        
        timelineSheet = findViewById(R.id.timelineBottomSheet);
        timelineSheetBehavior = BottomSheetBehavior.from(timelineSheet);
    }

    private void setupExoPlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        
        playPauseButton.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                player.play();
                playPauseButton.setImageResource(R.drawable.ic_pause);
            }
        });
    }

    private void setupTimeline() {
        timelineAdapter = new TimelineAdapter(this, clip -> {
            // Handle clip selection
            viewModel.setSelectedClip(clip);
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        timelineRecyclerView.setLayoutManager(layoutManager);
        timelineRecyclerView.setAdapter(timelineAdapter);
        
        // Add pinch-to-zoom gesture
        ScaleGestureDetector scaleDetector = new ScaleGestureDetector(this,
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float scaleFactor = detector.getScaleFactor();
                    viewModel.adjustTimelineScale(scaleFactor);
                    return true;
                }
            }
        );
        
        timelineRecyclerView.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            return false;
        });
    }

    private void setupToolbarButtons() {
        cutButton.setOnClickListener(v -> {
            if (viewModel.getSelectedClip().getValue() != null) {
                long currentPosition = player.getCurrentPosition();
                viewModel.splitClipAtPosition(currentPosition);
            } else {
                Toast.makeText(this, "Select a clip first", Toast.LENGTH_SHORT).show();
            }
        });

        filterButton.setOnClickListener(v -> showFilterBottomSheet());
        textButton.setOnClickListener(v -> showTextBottomSheet());
        audioButton.setOnClickListener(v -> showAudioBottomSheet());
        effectButton.setOnClickListener(v -> showTransitionBottomSheet());
        
        exportButton.setOnClickListener(v -> {
            // Save project before exporting
            viewModel.saveProject();
            
            Intent intent = new Intent(this, ExportActivity.class);
            intent.putExtra("projectId", projectId);
            startActivity(intent);
        });
    }

    private void setupBottomSheets() {
        // Configure timeline sheet behavior
        timelineSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.timeline_peek_height));
        timelineSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        timelineSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Handle state changes
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Adjust UI based on slide offset if needed
            }
        });
    }

    private void showFilterBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filters, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomSheetView);
        
        RecyclerView filtersRecyclerView = bottomSheetView.findViewById(R.id.filtersRecyclerView);
        FilterAdapter adapter = new FilterAdapter(this, filter -> {
            // Apply selected filter
            viewModel.applyFilter(filter);
            dialog.dismiss();
        });
        
        filtersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filtersRecyclerView.setAdapter(adapter);
        
        // Set filter data
        adapter.setFilters(viewModel.getAvailableFilters());
        
        dialog.show();
    }

    private void showTextBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_text, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomSheetView);
        
        // Text input field
        EditText textInput = bottomSheetView.findViewById(R.id.textInput);
        
        // Font selection RecyclerView
        RecyclerView fontsRecyclerView = bottomSheetView.findViewById(R.id.fontsRecyclerView);
        TextStyleAdapter adapter = new TextStyleAdapter(this, style -> {
            // Apply font style
        });
        
        fontsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fontsRecyclerView.setAdapter(adapter);
        
        // Color selector
        ColorPickerView colorPicker = bottomSheetView.findViewById(R.id.colorPicker);
        
        // Add text button
        Button addTextButton = bottomSheetView.findViewById(R.id.addTextButton);
        addTextButton.setOnClickListener(v -> {
            String text = textInput.getText().toString();
            if (!text.isEmpty()) {
                int color = colorPicker.getColor();
                viewModel.addTextOverlay(text, color, adapter.getSelectedStyle(), player.getCurrentPosition());
                dialog.dismiss();
            } else {
                textInput.setError("Text cannot be empty");
            }
        });
        
        dialog.show();
    }

    private void showAudioBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_audio, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomSheetView);
        
        // Audio track selection
        Button selectMusicButton = bottomSheetView.findViewById(R.id.selectMusicButton);
        selectMusicButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            startActivityForResult(intent, 2);
            dialog.dismiss();
        });
        
        // Voice recording
        Button recordVoiceButton = bottomSheetView.findViewById(R.id.recordVoiceButton);
        recordVoiceButton.setOnClickListener(v -> {
            // Start voice recording UI
            // For brevity, implementation details omitted
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void showTransitionBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_transitions, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomSheetView);
        
        RecyclerView transitionsRecyclerView = bottomSheetView.findViewById(R.id.transitionsRecyclerView);
        TransitionAdapter adapter = new TransitionAdapter(this, transition -> {
            // Apply selected transition
            viewModel.applyTransition(transition);
            dialog.dismiss();
        });
        
        transitionsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        transitionsRecyclerView.setAdapter(adapter);
        
        // Set transition data
        adapter.setTransitions(viewModel.getAvailableTransitions());
        
        dialog.show();
    }

    private void updateUI(Project project) {
        if (project != null && !project.getVideoClips().isEmpty()) {
            // Update timeline
            timelineAdapter.setClips(project.getVideoClips());
            
            // Update ExoPlayer with the main clip
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(project.getVideoClips().get(0).getPath()));
            player.setMediaItem(mediaItem);
            player.prepare();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            // Handle audio selection
            Uri audioUri = data.getData();
            viewModel.addAudioTrack(audioUri.toString(), 0, -1); // Add for entire duration
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
        // Auto save project when pausing
        viewModel.saveProject();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
