package com.example.snapeditprovs.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.snapedit.pro.database.ProjectDao;
import com.snapedit.pro.models.AudioClip;
import com.snapedit.pro.models.Filter;
import com.snapedit.pro.models.Project;
import com.snapedit.pro.models.TextOverlay;
import com.snapedit.pro.models.Transition;
import com.snapedit.pro.models.VideoClip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorViewModel extends AndroidViewModel {
    private static final String TAG = "EditorViewModel";
    
    private ProjectDao projectDao;
    private MutableLiveData<Project> project;
    private MutableLiveData<VideoClip> selectedClip;
    private MutableLiveData<Float> timelineScale;
    private long projectId;
    private ExecutorService executor;
    
    private List<Filter> availableFilters;
    private List<Transition> availableTransitions;

    public EditorViewModel(@NonNull Application application) {
        super(application);
        projectDao = new ProjectDao(application);
        project = new MutableLiveData<>();
        selectedClip = new MutableLiveData<>();
        timelineScale = new MutableLiveData<>(1.0f);
        executor = Executors.newSingleThreadExecutor();
        
        // Initialize available filters and transitions
        availableFilters = new ArrayList<>(Arrays.asList(Filter.createPresetFilters()));
        availableTransitions = new ArrayList<>(Arrays.asList(Transition.createPresetTransitions()));
    }

    public void loadProject(long projectId) {
        this.projectId = projectId;
        executor.execute(() -> {
            Project loadedProject = projectDao.getProject(projectId);
            project.postValue(loadedProject);
        });
    }

    public LiveData<Project> getProject() {
        return project;
    }

    public LiveData<VideoClip> getSelectedClip() {
        return selectedClip;
    }

    public void setSelectedClip(VideoClip clip) {
        selectedClip.setValue(clip);
    }

    public LiveData<Float> getTimelineScale() {
        return timelineScale;
    }

    public void adjustTimelineScale(float scaleFactor) {
        Float currentScale = timelineScale.getValue();
        if (currentScale != null) {
            float newScale = currentScale * scaleFactor;
            // Limit scale between 0.5 and 3.0
            newScale = Math.max(0.5f, Math.min(3.0f, newScale));
            timelineScale.setValue(newScale);
        }
    }

    public void splitClipAtPosition(double position) {
        Project currentProject = project.getValue();
        VideoClip selectedClip = this.selectedClip.getValue();
        
        if (currentProject == null || selectedClip == null) {
            return;
        }
        
        // Convert player position to clip's internal position
        double clipPosition = position - selectedClip.getTimelinePosition() + selectedClip.getStartTime();
        
        // Check if position is within clip bounds
        if (clipPosition <= selectedClip.getStartTime() || clipPosition >= selectedClip.getEndTime()) {
            return;
        }
        
        // Split the clip
        VideoClip newClip = selectedClip.splitAt(clipPosition);
        
        if (newClip != null) {
            // Add the new clip to the project
            List<VideoClip> clips = currentProject.getVideoClips();
            int index = clips.indexOf(selectedClip);
            if (index >= 0) {
                clips.add(index + 1, newClip);
                
                // Update project
                currentProject.setLastModified(System.currentTimeMillis());
                project.setValue(currentProject);
                
                // Auto-save
                saveProject();
            }
        }
    }

    public void applyFilter(Filter filter) {
        Project currentProject = project.getValue();
        if (currentProject != null) {
            currentProject.setAppliedFilter(filter);
            currentProject.setLastModified(System.currentTimeMillis());
            project.setValue(currentProject);
            
            // Auto-save
            saveProject();
        }
    }

    public void addTextOverlay(String text, int color, String fontName, double position) {
        Project currentProject = project.getValue();
        if (currentProject != null) {
            // Create text overlay
            TextOverlay overlay = new TextOverlay();
            overlay.setText(text);
            overlay.setColor(color);
            overlay.setFontName(fontName);
            overlay.setStartTime(position);
            overlay.setEndTime(position + 3.0); // Default 3-second duration
            
            // Add to project
            currentProject.addTextOverlay(overlay);
            currentProject.setLastModified(System.currentTimeMillis());
            project.setValue(currentProject);
            
            // Auto-save
            saveProject();
        }
    }

    public void addAudioTrack(String audioPath, double startPosition, double duration) {
        Project currentProject = project.getValue();
        if (currentProject != null) {
            // Create audio clip
            AudioClip audioClip = new AudioClip();
            audioClip.setPath(audioPath);
            audioClip.setStartTime(0);
            
            // If duration is -1, use full duration of project
            if (duration < 0) {
                audioClip.setEndTime(currentProject.getDuration());
            } else {
                audioClip.setEndTime(duration);
            }
            
            audioClip.setTimelinePosition(startPosition);
            audioClip.setType(0); // Background music
            
            // Add to project
            currentProject.addAudioClip(audioClip);
            currentProject.setLastModified(System.currentTimeMillis());
            project.setValue(currentProject);
            
            // Auto-save
            saveProject();
        }
    }

    public void applyTransition(Transition transition) {
        Project currentProject = project.getValue();
        VideoClip selectedClip = this.selectedClip.getValue();
        
        if (currentProject != null && selectedClip != null) {
            List<VideoClip> clips = currentProject.getVideoClips();
            int index = clips.indexOf(selectedClip);
            
            // Make sure this isn't the last clip
            if (index >= 0 && index < clips.size() - 1) {
                // Set transition between this clip and the next
                transition.setClipStartId(selectedClip.getId());
                transition.setClipEndId(clips.get(index + 1).getId());
                transition.setPosition(index);
                
                // Add to project
                currentProject.addTransition(transition);
                currentProject.setLastModified(System.currentTimeMillis());
                project.setValue(currentProject);
                
                // Auto-save
                saveProject();
            }
        }
    }

    public void saveProject() {
        Project currentProject = project.getValue();
        if (currentProject != null) {
            executor.execute(() -> {
                try {
                    projectDao.updateProject(currentProject);
                } catch (Exception e) {
                    Log.e(TAG, "Error saving project", e);
                }
            });
        }
    }

    public List<Filter> getAvailableFilters() {
        return availableFilters;
    }

    public List<Transition> getAvailableTransitions() {
        return availableTransitions;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
