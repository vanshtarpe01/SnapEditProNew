package com.example.snapeditprovs.viewmodels;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.snapedit.pro.database.ProjectDao;
import com.snapedit.pro.models.ExportSettings;
import com.snapedit.pro.models.Project;
import com.snapedit.pro.utils.FFmpegUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportViewModel extends AndroidViewModel {
    private static final String TAG = "ExportViewModel";
    
    private ProjectDao projectDao;
    private MutableLiveData<Project> project;
    private MutableLiveData<Integer> exportProgress;
    private MutableLiveData<Boolean> exportCompleted;
    private MutableLiveData<String> exportError;
    private long projectId;
    private ExecutorService executor;
    private ExportSettings exportSettings;
    private String exportedFilePath;

    public ExportViewModel(@NonNull Application application) {
        super(application);
        projectDao = new ProjectDao(application);
        project = new MutableLiveData<>();
        exportProgress = new MutableLiveData<>(0);
        exportCompleted = new MutableLiveData<>(false);
        exportError = new MutableLiveData<>("");
        executor = Executors.newSingleThreadExecutor();
        exportSettings = new ExportSettings();
        
        // Set up FFmpeg progress callback
        Config.enableStatisticsCallback(statistics -> {
            // Calculate progress based on time
            if (project.getValue() != null) {
                double duration = project.getValue().getDuration();
                double currentTime = statistics.getTime() / 1000.0; // Convert to seconds
                int progress = (int) (currentTime / duration * 100);
                // Ensure progress is between 0-100
                progress = Math.min(100, Math.max(0, progress));
                exportProgress.postValue(progress);
            }
        });
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

    public LiveData<Integer> getExportProgress() {
        return exportProgress;
    }

    public LiveData<Boolean> getExportCompleted() {
        return exportCompleted;
    }

    public LiveData<String> getExportError() {
        return exportError;
    }

    public ExportSettings getExportSettings() {
        return exportSettings;
    }

    public void setExportResolution(int resolution) {
        exportSettings.setResolution(resolution);
    }

    public void setExportBitrate(int bitrate) {
        exportSettings.setBitrate(bitrate);
    }

    public void setExportFramerate(int framerate) {
        exportSettings.setFramerate(framerate);
    }

    public String getExportedFilePath() {
        return exportedFilePath;
    }

    public void exportProject(String outputFilename) {
        Project currentProject = project.getValue();
        if (currentProject == null) {
            exportError.postValue("No project loaded");
            return;
        }
        
        executor.execute(() -> {
            try {
                // Set up output directory in Movies folder
                File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                File snapEditDir = new File(moviesDir, "SnapEdit Pro");
                if (!snapEditDir.exists()) {
                    snapEditDir.mkdirs();
                }
                
                // Set export settings output path
                exportSettings.setOutputPath(snapEditDir.getAbsolutePath());
                exportSettings.setOutputFilename(outputFilename);
                
                // Generate FFmpeg command for project export
                String ffmpegCommand = FFmpegUtils.generateExportCommand(currentProject, exportSettings);
                Log.d(TAG, "FFmpeg command: " + ffmpegCommand);
                
                // Reset progress and status
                exportProgress.postValue(0);
                exportCompleted.postValue(false);
                exportError.postValue("");
                
                // Execute FFmpeg command
                int returnCode = FFmpeg.execute(ffmpegCommand);
                
                if (returnCode == RETURN_CODE_SUCCESS) {
                    exportedFilePath = exportSettings.getFullOutputPath();
                    exportCompleted.postValue(true);
                    exportProgress.postValue(100);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    exportError.postValue("Export cancelled");
                } else {
                    exportError.postValue("Export failed with code: " + returnCode);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error during export", e);
                exportError.postValue("Export error: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
