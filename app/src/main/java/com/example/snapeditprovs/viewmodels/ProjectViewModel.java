package com.example.snapeditprovs.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.snapeditprovs.database.ProjectDao;
import com.example.snapeditprovs.models.Project;
import com.example.snapeditprovs.models.VideoClip;
import com.example.snapeditprovs.utils.VideoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectViewModel extends AndroidViewModel {
    private static final String TAG = "ProjectViewModel";
    
    private ProjectDao projectDao;
    private MutableLiveData<List<Project>> projects;
    private long lastCreatedProjectId = -1;
    private ExecutorService executor;

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        projectDao = new ProjectDao(application);
        projects = new MutableLiveData<>();
        executor = Executors.newSingleThreadExecutor();
        loadProjects();
    }

    private void loadProjects() {
        executor.execute(() -> {
            List<Project> projectList = projectDao.getAllProjects();
            projects.postValue(projectList);
        });
    }

    public LiveData<List<Project>> getAllProjects() {
        return projects;
    }

    public void createProject(Uri videoUri, long timestamp) {
        executor.execute(() -> {
            try {
                ContentResolver resolver = getApplication().getContentResolver();
                
                // Create new project
                Project project = new Project();
                project.setName("Project " + timestamp);
                project.setCreatedAt(timestamp);
                project.setLastModified(timestamp);
                
                // Get video metadata
                VideoUtils.VideoMetadata metadata = VideoUtils.getVideoMetadata(getApplication(), videoUri);
                project.setWidth(metadata.width);
                project.setHeight(metadata.height);
                project.setDuration(metadata.duration);
                
                // Copy video file to app's storage
                String videoPath = copyVideoToStorage(videoUri, "project_" + timestamp + ".mp4");
                
                // Create initial video clip
                VideoClip clip = new VideoClip();
                clip.setPath(videoPath);
                clip.setStartTime(0);
                clip.setEndTime(metadata.duration);
                clip.setTimelinePosition(0);
                clip.setWidth(metadata.width);
                clip.setHeight(metadata.height);
                
                // Generate thumbnail
                String thumbnailPath = generateThumbnail(videoUri, "thumbnail_" + timestamp + ".jpg");
                clip.setThumbnailPath(thumbnailPath);
                project.setThumbnailPath(thumbnailPath);
                
                // Add clip to project
                project.addVideoClip(clip);
                
                // Save project to database
                long projectId = projectDao.insertProject(project);
                lastCreatedProjectId = projectId;
                
                // Refresh projects list
                loadProjects();
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating project", e);
            }
        });
    }

    public void deleteProject(Project project) {
        executor.execute(() -> {
            try {
                // Delete all project files
                for (VideoClip clip : project.getVideoClips()) {
                    new File(clip.getPath()).delete();
                    if (clip.getThumbnailPath() != null) {
                        new File(clip.getThumbnailPath()).delete();
                    }
                }
                
                // Delete project thumbnail
                if (project.getThumbnailPath() != null) {
                    new File(project.getThumbnailPath()).delete();
                }
                
                // Delete from database
                projectDao.deleteProject(project.getId());
                
                // Refresh projects list
                loadProjects();
                
            } catch (Exception e) {
                Log.e(TAG, "Error deleting project", e);
            }
        });
    }

    public void duplicateProject(Project project) {
        executor.execute(() -> {
            try {
                // Create deep copy of project
                Project copy = project.duplicate();
                
                // Save to database
                long newId = projectDao.insertProject(copy);
                
                // Refresh projects list
                loadProjects();
                
            } catch (Exception e) {
                Log.e(TAG, "Error duplicating project", e);
            }
        });
    }

    public void renameProject(Project project, String newName) {
        executor.execute(() -> {
            try {
                project.setName(newName);
                project.setLastModified(System.currentTimeMillis());
                
                // Update in database
                projectDao.updateProject(project);
                
                // Refresh projects list
                loadProjects();
                
            } catch (Exception e) {
                Log.e(TAG, "Error renaming project", e);
            }
        });
    }

    private String copyVideoToStorage(Uri uri, String filename) {
        try {
            ContentResolver resolver = getApplication().getContentResolver();
            File videoDir = new File(getApplication().getFilesDir(), "videos");
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }
            
            File outputFile = new File(videoDir, filename);
            
            // Copy content from uri to file
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            try (java.io.InputStream inputStream = resolver.openInputStream(uri);
                 java.io.OutputStream outputStream = new FileOutputStream(outputFile)) {
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error copying video file", e);
            return null;
        }
    }

    private String generateThumbnail(Uri videoUri, String filename) {
        try {
            Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
                    getApplication().getContentResolver(),
                    Long.parseLong(videoUri.getLastPathSegment()),
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null);
            
            if (thumbnail == null) {
                // Fall back to media metadata retriever
                thumbnail = VideoUtils.extractVideoThumbnail(getApplication(), videoUri);
            }
            
            if (thumbnail != null) {
                File thumbnailDir = new File(getApplication().getFilesDir(), "thumbnails");
                if (!thumbnailDir.exists()) {
                    thumbnailDir.mkdirs();
                }
                
                File outputFile = new File(thumbnailDir, filename);
                
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }
                
                return outputFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail", e);
        }
        
        return null;
    }

    public long getLastCreatedProjectId() {
        return lastCreatedProjectId;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
