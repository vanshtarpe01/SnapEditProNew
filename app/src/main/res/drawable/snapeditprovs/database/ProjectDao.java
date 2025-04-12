package com.example.snapeditprovs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.snapedit.pro.models.AudioClip;
import com.snapedit.pro.models.Filter;
import com.snapedit.pro.models.Project;
import com.snapedit.pro.models.StickerOverlay;
import com.snapedit.pro.models.TextOverlay;
import com.snapedit.pro.models.Transition;
import com.snapedit.pro.models.VideoClip;

import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
    private static final String TAG = "ProjectDao";
    
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public ProjectDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private void open() {
        database = dbHelper.getWritableDatabase();
    }

    private void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    /**
     * Insert a new project into the database
     * @param project The project to insert
     * @return The ID of the newly inserted project
     */
    public long insertProject(Project project) {
        open();
        
        try {
            database.beginTransaction();
            
            // Insert project
            ContentValues projectValues = new ContentValues();
            projectValues.put(DatabaseHelper.COLUMN_NAME, project.getName());
            projectValues.put(DatabaseHelper.COLUMN_CREATED_AT, project.getCreatedAt());
            projectValues.put(DatabaseHelper.COLUMN_LAST_MODIFIED, project.getLastModified());
            projectValues.put(DatabaseHelper.COLUMN_WIDTH, project.getWidth());
            projectValues.put(DatabaseHelper.COLUMN_HEIGHT, project.getHeight());
            projectValues.put(DatabaseHelper.COLUMN_DURATION, project.getDuration());
            projectValues.put(DatabaseHelper.COLUMN_THUMBNAIL_PATH, project.getThumbnailPath());
            
            long projectId = database.insert(DatabaseHelper.TABLE_PROJECTS, null, projectValues);
            project.setId(projectId);
            
            // Insert video clips
            for (VideoClip clip : project.getVideoClips()) {
                insertVideoClip(clip, projectId);
            }
            
            // Insert audio clips
            for (AudioClip clip : project.getAudioClips()) {
                insertAudioClip(clip, projectId);
            }
            
            // Insert text overlays
            for (TextOverlay overlay : project.getTextOverlays()) {
                insertTextOverlay(overlay, projectId);
            }
            
            // Insert sticker overlays
            for (StickerOverlay overlay : project.getStickerOverlays()) {
                insertStickerOverlay(overlay, projectId);
            }
            
            // Insert filter if exists
            if (project.getAppliedFilter() != null) {
                insertFilter(project.getAppliedFilter(), projectId);
            }
            
            // Insert transitions
            for (Transition transition : project.getTransitions()) {
                insertTransition(transition, projectId);
            }
            
            database.setTransactionSuccessful();
            
            return projectId;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting project", e);
            return -1;
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            close();
        }
    }

    /**
     * Update an existing project in the database
     * @param project The project to update
     * @return True if update successful, false otherwise
     */
    public boolean updateProject(Project project) {
        open();
        
        try {
            database.beginTransaction();
            
            // Update project
            ContentValues projectValues = new ContentValues();
            projectValues.put(DatabaseHelper.COLUMN_NAME, project.getName());
            projectValues.put(DatabaseHelper.COLUMN_LAST_MODIFIED, project.getLastModified());
            projectValues.put(DatabaseHelper.COLUMN_WIDTH, project.getWidth());
            projectValues.put(DatabaseHelper.COLUMN_HEIGHT, project.getHeight());
            projectValues.put(DatabaseHelper.COLUMN_DURATION, project.getDuration());
            projectValues.put(DatabaseHelper.COLUMN_THUMBNAIL_PATH, project.getThumbnailPath());
            
            long projectId = project.getId();
            database.update(DatabaseHelper.TABLE_PROJECTS, projectValues, 
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(projectId)});
            
            // Delete all existing elements for this project
            database.delete(DatabaseHelper.TABLE_VIDEO_CLIPS, 
                    DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)});
            database.delete(DatabaseHelper.TABLE_AUDIO_CLIPS, 
                    DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)});
            database.delete(DatabaseHelper.TABLE_TEXT_OVERLAYS, 
                    DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)});
            database.delete(DatabaseHelper.TABLE_STICKER_OVERLAYS, 
                    DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)});
            database.delete(DatabaseHelper.TABLE_FILTERS, 
                    DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)});
            database.delete(DatabaseHelper.TABLE_TRANSITIONS, 
                    DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)});
            
            // Insert all elements again
            for (VideoClip clip : project.getVideoClips()) {
                insertVideoClip(clip, projectId);
            }
            
            for (AudioClip clip : project.getAudioClips()) {
                insertAudioClip(clip, projectId);
            }
            
            for (TextOverlay overlay : project.getTextOverlays()) {
                insertTextOverlay(overlay, projectId);
            }
            
            for (StickerOverlay overlay : project.getStickerOverlays()) {
                insertStickerOverlay(overlay, projectId);
            }
            
            if (project.getAppliedFilter() != null) {
                insertFilter(project.getAppliedFilter(), projectId);
            }
            
            for (Transition transition : project.getTransitions()) {
                insertTransition(transition, projectId);
            }
            
            database.setTransactionSuccessful();
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating project", e);
            return false;
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            close();
        }
    }

    /**
     * Delete a project from the database
     * @param projectId The ID of the project to delete
     * @return True if deletion successful, false otherwise
     */
    public boolean deleteProject(long projectId) {
        open();
        
        try {
            // Due to ON DELETE CASCADE, all related records will be deleted automatically
            int deletedRows = database.delete(DatabaseHelper.TABLE_PROJECTS, 
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(projectId)});
            
            return deletedRows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting project", e);
            return false;
        } finally {
            close();
        }
    }

    /**
     * Get a project from the database by ID
     * @param projectId The ID of the project to retrieve
     * @return The project, or null if not found
     */
    public Project getProject(long projectId) {
        open();
        
        try {
            // Query project
            Cursor cursor = database.query(DatabaseHelper.TABLE_PROJECTS, null, 
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(projectId)}, 
                    null, null, null);
            
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            
            Project project = cursorToProject(cursor);
            cursor.close();
            
            // Load all related elements
            project.setVideoClips(getVideoClipsForProject(projectId));
            project.setAudioClips(getAudioClipsForProject(projectId));
            project.setTextOverlays(getTextOverlaysForProject(projectId));
            project.setStickerOverlays(getStickerOverlaysForProject(projectId));
            project.setAppliedFilter(getFilterForProject(projectId));
            project.setTransitions(getTransitionsForProject(projectId));
            
            return project;
        } catch (Exception e) {
            Log.e(TAG, "Error getting project", e);
            return null;
        } finally {
            close();
        }
    }

    /**
     * Get all projects from the database
     * @return List of all projects
     */
    public List<Project> getAllProjects() {
        open();
        
        List<Project> projects = new ArrayList<>();
        
        try {
            Cursor cursor = database.query(DatabaseHelper.TABLE_PROJECTS, null, 
                    null, null, null, null, 
                    DatabaseHelper.COLUMN_LAST_MODIFIED + " DESC");
            
            if (cursor == null) {
                return projects;
            }
            
            if (!cursor.moveToFirst()) {
                cursor.close();
                return projects;
            }
            
            do {
                Project project = cursorToProject(cursor);
                projects.add(project);
            } while (cursor.moveToNext());
            
            cursor.close();
            
            // For each project, load the thumbnail and basic info
            // (but not all elements for performance)
            for (Project project : projects) {
                long projectId = project.getId();
                
                // Get first video clip thumbnail
                Cursor videoClipCursor = database.query(DatabaseHelper.TABLE_VIDEO_CLIPS, 
                        new String[]{DatabaseHelper.COLUMN_THUMBNAIL_PATH}, 
                        DatabaseHelper.COLUMN_PROJECT_ID + " = ?", 
                        new String[]{String.valueOf(projectId)}, 
                        null, null, null, "1");
                
                if (videoClipCursor != null && videoClipCursor.moveToFirst()) {
                    int thumbnailPathIndex = videoClipCursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL_PATH);
                    if (thumbnailPathIndex != -1) {
                        String thumbnailPath = videoClipCursor.getString(thumbnailPathIndex);
                        project.setThumbnailPath(thumbnailPath);
                    }
                    videoClipCursor.close();
                }
            }
            
            return projects;
        } catch (Exception e) {
            Log.e(TAG, "Error getting all projects", e);
            return projects;
        } finally {
            close();
        }
    }

    // Helper methods to insert child elements
    
    private long insertVideoClip(VideoClip clip, long projectId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_ID, projectId);
        values.put(DatabaseHelper.COLUMN_PATH, clip.getPath());
        values.put(DatabaseHelper.COLUMN_START_TIME, clip.getStartTime());
        values.put(DatabaseHelper.COLUMN_END_TIME, clip.getEndTime());
        values.put(DatabaseHelper.COLUMN_DURATION, clip.getDuration());
        values.put(DatabaseHelper.COLUMN_TIMELINE_POSITION, clip.getTimelinePosition());
        values.put(DatabaseHelper.COLUMN_WIDTH, clip.getWidth());
        values.put(DatabaseHelper.COLUMN_HEIGHT, clip.getHeight());
        values.put(DatabaseHelper.COLUMN_VOLUME, clip.getVolume());
        values.put(DatabaseHelper.COLUMN_SPEED, clip.getSpeed());
        values.put(DatabaseHelper.COLUMN_THUMBNAIL_PATH, clip.getThumbnailPath());
        values.put(DatabaseHelper.COLUMN_IS_MUTED, clip.isMuted() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_IS_REVERSED, clip.isReversed() ? 1 : 0);
        
        long id = database.insert(DatabaseHelper.TABLE_VIDEO_CLIPS, null, values);
        clip.setId(id);
        return id;
    }
    
    private long insertAudioClip(AudioClip clip, long projectId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_ID, projectId);
        values.put(DatabaseHelper.COLUMN_PATH, clip.getPath());
        values.put(DatabaseHelper.COLUMN_START_TIME, clip.getStartTime());
        values.put(DatabaseHelper.COLUMN_END_TIME, clip.getEndTime());
        values.put(DatabaseHelper.COLUMN_DURATION, clip.getDuration());
        values.put(DatabaseHelper.COLUMN_TIMELINE_POSITION, clip.getTimelinePosition());
        values.put(DatabaseHelper.COLUMN_VOLUME, clip.getVolume());
        values.put(DatabaseHelper.COLUMN_IS_FADE_IN, clip.isFadeIn() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_IS_FADE_OUT, clip.isFadeOut() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_FADE_IN_DURATION, clip.getFadeInDuration());
        values.put(DatabaseHelper.COLUMN_FADE_OUT_DURATION, clip.getFadeOutDuration());
        values.put(DatabaseHelper.COLUMN_TYPE, clip.getType());
        
        long id = database.insert(DatabaseHelper.TABLE_AUDIO_CLIPS, null, values);
        clip.setId(id);
        return id;
    }
    
    private long insertTextOverlay(TextOverlay overlay, long projectId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_ID, projectId);
        values.put(DatabaseHelper.COLUMN_TEXT, overlay.getText());
        values.put(DatabaseHelper.COLUMN_FONT_NAME, overlay.getFontName());
        values.put(DatabaseHelper.COLUMN_FONT_SIZE, overlay.getFontSize());
        values.put(DatabaseHelper.COLUMN_COLOR, overlay.getColor());
        values.put(DatabaseHelper.COLUMN_BACKGROUND_COLOR, overlay.getBackgroundColor());
        values.put(DatabaseHelper.COLUMN_POSITION_X, overlay.getPositionX());
        values.put(DatabaseHelper.COLUMN_POSITION_Y, overlay.getPositionY());
        values.put(DatabaseHelper.COLUMN_START_TIME, overlay.getStartTime());
        values.put(DatabaseHelper.COLUMN_END_TIME, overlay.getEndTime());
        values.put(DatabaseHelper.COLUMN_ROTATION, overlay.getRotation());
        values.put(DatabaseHelper.COLUMN_ANIMATION, overlay.getAnimation());
        values.put(DatabaseHelper.COLUMN_IS_BOLD, overlay.isBold() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_IS_ITALIC, overlay.isItalic() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_IS_UNDERLINE, overlay.isUnderline() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_ALIGNMENT, overlay.getAlignment());
        
        long id = database.insert(DatabaseHelper.TABLE_TEXT_OVERLAYS, null, values);
        overlay.setId(id);
        return id;
    }
    
    private long insertStickerOverlay(StickerOverlay overlay, long projectId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_ID, projectId);
        values.put(DatabaseHelper.COLUMN_PATH, overlay.getPath());
        values.put(DatabaseHelper.COLUMN_POSITION_X, overlay.getPositionX());
        values.put(DatabaseHelper.COLUMN_POSITION_Y, overlay.getPositionY());
        values.put(DatabaseHelper.COLUMN_SCALE, overlay.getScale());
        values.put(DatabaseHelper.COLUMN_ROTATION, overlay.getRotation());
        values.put(DatabaseHelper.COLUMN_START_TIME, overlay.getStartTime());
        values.put(DatabaseHelper.COLUMN_END_TIME, overlay.getEndTime());
        values.put(DatabaseHelper.COLUMN_ANIMATION, overlay.getAnimation());
        
        long id = database.insert(DatabaseHelper.TABLE_STICKER_OVERLAYS, null, values);
        overlay.setId(id);
        return id;
    }
    
    private long insertFilter(Filter filter, long projectId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_ID, projectId);
        values.put(DatabaseHelper.COLUMN_NAME, filter.getName());
        values.put(DatabaseHelper.COLUMN_TYPE, filter.getType());
        values.put(DatabaseHelper.COLUMN_BRIGHTNESS, filter.getBrightness());
        values.put(DatabaseHelper.COLUMN_CONTRAST, filter.getContrast());
        values.put(DatabaseHelper.COLUMN_SATURATION, filter.getSaturation());
        values.put(DatabaseHelper.COLUMN_EXPOSURE, filter.getExposure());
        values.put(DatabaseHelper.COLUMN_TEMPERATURE, filter.getTemperature());
        values.put(DatabaseHelper.COLUMN_TINT, filter.getTint());
        values.put(DatabaseHelper.COLUMN_VIBRANCE, filter.getVibrance());
        values.put(DatabaseHelper.COLUMN_HIGHLIGHTS, filter.getHighlights());
        values.put(DatabaseHelper.COLUMN_SHADOWS, filter.getShadows());
        values.put(DatabaseHelper.COLUMN_THUMBNAIL_PATH, filter.getThumbnailPath());
        values.put(DatabaseHelper.COLUMN_LUT, filter.getLut());
        
        long id = database.insert(DatabaseHelper.TABLE_FILTERS, null, values);
        filter.setId(id);
        return id;
    }
    
    private long insertTransition(Transition transition, long projectId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PROJECT_ID, projectId);
        values.put(DatabaseHelper.COLUMN_NAME, transition.getName());
        values.put(DatabaseHelper.COLUMN_TYPE, transition.getType());
        values.put(DatabaseHelper.COLUMN_DURATION, transition.getDuration());
        values.put(DatabaseHelper.COLUMN_CLIP_START_ID, transition.getClipStartId());
        values.put(DatabaseHelper.COLUMN_CLIP_END_ID, transition.getClipEndId());
        values.put(DatabaseHelper.COLUMN_THUMBNAIL_PATH, transition.getThumbnailPath());
        values.put(DatabaseHelper.COLUMN_POSITION, transition.getPosition());
        
        long id = database.insert(DatabaseHelper.TABLE_TRANSITIONS, null, values);
        transition.setId(id);
        return id;
    }
    
    // Helper methods to convert cursor to objects
    
    private Project cursorToProject(Cursor cursor) {
        Project project = new Project();
        
        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
        int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
        int createdAtIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT);
        int lastModifiedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LAST_MODIFIED);
        int widthIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_WIDTH);
        int heightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_HEIGHT);
        int durationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION);
        int thumbnailPathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL_PATH);
        
        if (idIndex != -1) project.setId(cursor.getLong(idIndex));
        if (nameIndex != -1) project.setName(cursor.getString(nameIndex));
        if (createdAtIndex != -1) project.setCreatedAt(cursor.getLong(createdAtIndex));
        if (lastModifiedIndex != -1) project.setLastModified(cursor.getLong(lastModifiedIndex));
        if (widthIndex != -1) project.setWidth(cursor.getInt(widthIndex));
        if (heightIndex != -1) project.setHeight(cursor.getInt(heightIndex));
        if (durationIndex != -1) project.setDuration(cursor.getDouble(durationIndex));
        if (thumbnailPathIndex != -1) project.setThumbnailPath(cursor.getString(thumbnailPathIndex));
        
        return project;
    }
    
    private VideoClip cursorToVideoClip(Cursor cursor) {
        VideoClip clip = new VideoClip();
        
        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
        int pathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PATH);
        int startTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_START_TIME);
        int endTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_END_TIME);
        int durationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION);
        int timelinePositionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMELINE_POSITION);
        int widthIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_WIDTH);
        int heightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_HEIGHT);
        int volumeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VOLUME);
        int speedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SPEED);
        int thumbnailPathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL_PATH);
        int isMutedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_MUTED);
        int isReversedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_REVERSED);
        
        if (idIndex != -1) clip.setId(cursor.getLong(idIndex));
        if (pathIndex != -1) clip.setPath(cursor.getString(pathIndex));
        if (startTimeIndex != -1) clip.setStartTime(cursor.getDouble(startTimeIndex));
        if (endTimeIndex != -1) clip.setEndTime(cursor.getDouble(endTimeIndex));
        if (durationIndex != -1) clip.setDuration(cursor.getDouble(durationIndex));
        if (timelinePositionIndex != -1) clip.setTimelinePosition(cursor.getDouble(timelinePositionIndex));
        if (widthIndex != -1) clip.setWidth(cursor.getInt(widthIndex));
        if (heightIndex != -1) clip.setHeight(cursor.getInt(heightIndex));
        if (volumeIndex != -1) clip.setVolume(cursor.getFloat(volumeIndex));
        if (speedIndex != -1) clip.setSpeed(cursor.getFloat(speedIndex));
        if (thumbnailPathIndex != -1) clip.setThumbnailPath(cursor.getString(thumbnailPathIndex));
        if (isMutedIndex != -1) clip.setMuted(cursor.getInt(isMutedIndex) == 1);
        if (isReversedIndex != -1) clip.setReversed(cursor.getInt(isReversedIndex) == 1);
        
        return clip;
    }
    
    private AudioClip cursorToAudioClip(Cursor cursor) {
        AudioClip clip = new AudioClip();
        
        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
        int pathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PATH);
        int startTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_START_TIME);
        int endTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_END_TIME);
        int durationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION);
        int timelinePositionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMELINE_POSITION);
        int volumeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VOLUME);
        int isFadeInIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FADE_IN);
        int isFadeOutIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FADE_OUT);
        int fadeInDurationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FADE_IN_DURATION);
        int fadeOutDurationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FADE_OUT_DURATION);
        int typeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE);
        
        if (idIndex != -1) clip.setId(cursor.getLong(idIndex));
        if (pathIndex != -1) clip.setPath(cursor.getString(pathIndex));
        if (startTimeIndex != -1) clip.setStartTime(cursor.getDouble(startTimeIndex));
        if (endTimeIndex != -1) clip.setEndTime(cursor.getDouble(endTimeIndex));
        if (durationIndex != -1) clip.setDuration(cursor.getDouble(durationIndex));
        if (timelinePositionIndex != -1) clip.setTimelinePosition(cursor.getDouble(timelinePositionIndex));
        if (volumeIndex != -1) clip.setVolume(cursor.getFloat(volumeIndex));
        if (isFadeInIndex != -1) clip.setFadeIn(cursor.getInt(isFadeInIndex) == 1);
        if (isFadeOutIndex != -1) clip.setFadeOut(cursor.getInt(isFadeOutIndex) == 1);
        if (fadeInDurationIndex != -1) clip.setFadeInDuration(cursor.getFloat(fadeInDurationIndex));
        if (fadeOutDurationIndex != -1) clip.setFadeOutDuration(cursor.getFloat(fadeOutDurationIndex));
        if (typeIndex != -1) clip.setType(cursor.getInt(typeIndex));
        
        return clip;
    }
    
    private TextOverlay cursorToTextOverlay(Cursor cursor) {
        TextOverlay overlay = new TextOverlay();
        
        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
        int textIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TEXT);
        int fontNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FONT_NAME);
        int fontSizeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FONT_SIZE);
        int colorIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COLOR);
        int backgroundColorIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BACKGROUND_COLOR);
        int positionXIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION_X);
        int positionYIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION_Y);
        int startTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_START_TIME);
        int endTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_END_TIME);
        int rotationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROTATION);
        int animationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ANIMATION);
        int isBoldIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BOLD);
        int isItalicIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_ITALIC);
        int isUnderlineIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_UNDERLINE);
        int alignmentIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ALIGNMENT);
        
        if (idIndex != -1) overlay.setId(cursor.getLong(idIndex));
        if (textIndex != -1) overlay.setText(cursor.getString(textIndex));
        if (fontNameIndex != -1) overlay.setFontName(cursor.getString(fontNameIndex));
        if (fontSizeIndex != -1) overlay.setFontSize(cursor.getFloat(fontSizeIndex));
        if (colorIndex != -1) overlay.setColor(cursor.getInt(colorIndex));
        if (backgroundColorIndex != -1) overlay.setBackgroundColor(cursor.getInt(backgroundColorIndex));
        if (positionXIndex != -1) overlay.setPositionX(cursor.getFloat(positionXIndex));
        if (positionYIndex != -1) overlay.setPositionY(cursor.getFloat(positionYIndex));
        if (startTimeIndex != -1) overlay.setStartTime(cursor.getDouble(startTimeIndex));
        if (endTimeIndex != -1) overlay.setEndTime(cursor.getDouble(endTimeIndex));
        if (rotationIndex != -1) overlay.setRotation(cursor.getFloat(rotationIndex));
        if (animationIndex != -1) overlay.setAnimation(cursor.getString(animationIndex));
        if (isBoldIndex != -1) overlay.setBold(cursor.getInt(isBoldIndex) == 1);
        if (isItalicIndex != -1) overlay.setItalic(cursor.getInt(isItalicIndex) == 1);
        if (isUnderlineIndex != -1) overlay.setUnderline(cursor.getInt(isUnderlineIndex) == 1);
        if (alignmentIndex != -1) overlay.setAlignment(cursor.getInt(alignmentIndex));
        
        return overlay;
    }
    
    private StickerOverlay cursorToStickerOverlay(Cursor cursor) {
        StickerOverlay overlay = new StickerOverlay();
        
        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
        int pathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PATH);
        int positionXIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION_X);
        int positionYIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION_Y);
        int scaleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SCALE);
        int rotationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROTATION);
        int startTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_START_TIME);
        int endTimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_END_TIME);
        int animationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ANIMATION);
        
        if (idIndex != -1) overlay.setId(cursor.getLong(idIndex));
        if (pathIndex != -1) overlay.setPath(cursor.getString(pathIndex));
        if (positionXIndex != -1) overlay.setPositionX(cursor.getFloat(positionXIndex));
        if (positionYIndex != -1) overlay.setPositionY(cursor.getFloat(positionYIndex));
        if (scaleIndex != -1) overlay.setScale(cursor.getFloat(scaleIndex));
        if (rotationIndex != -1) overlay.setRotation(cursor.getFloat(rotationIndex));
        if (startTimeIndex != -1) overlay.setStartTime(cursor.getDouble(startTimeIndex));
        if (endTimeIndex != -1) overlay.setEndTime(cursor.getDouble(endTimeIndex));
        if (animationIndex != -1) overlay.setAnimation(cursor.getString(animationIndex));
        
        return overlay;
    }
    
    private Filter cursorToFilter(Cursor cursor) {
        Filter filter = new Filter();
        
        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
        int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
        int typeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE);
        int brightnessIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BRIGHTNESS);
        int contrastIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTRAST);
        int saturationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SATURATION);
        int exposureIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPOSURE);
        int temperatureIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TEMPERATURE);
        int tintIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TINT);
        int vibranceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VIBRANCE);
        int highlightsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_HIGHLIGHTS);
        int shadowsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SHADOWS);
        int thumbnailPathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL_PATH);
        int lutIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LUT);
        
        if (idIndex != -1) filter.setId(cursor.getLong(idIndex));
        if (nameIndex != -1) filter.setName(cursor.getString(nameIndex));
        if (typeIndex != -1) filter.setType(cursor.getString(typeIndex));
        if (brightnessIndex != -1) filter.setBrightness(cursor.getFloat(brightnessIndex));
        if (contrastIndex != -1) filter.setContrast(cursor.getFloat(contrastIndex));
        if (saturationIndex != -1) filter.setSaturation(cursor.getFloat(saturationIndex));
        if (exposureIndex != -1) filter.setExposure(cursor.getFloat(exposureIndex));
        if (temperatureIndex != -1) filter.setTemperature(cursor.getFloat(temperatureIndex));
        if (tintIndex != -1) filter.setTint(cursor.getFloat(tintIndex));
        if (vibranceIndex != -1) filter.setVibrance(cursor.getFloat(vibranceIndex));
        if (highlightsIndex != -1) filter.setHighlights(cursor.getFloat(highlightsIndex));
        if (shadowsIndex != -1) filter.setShadows(cursor.getFloat(shadowsIndex));
        if (thumbnailPathIndex != -1) filter.setThumbnailPath(cursor.getString(thumbnailPathIndex));
        if (lutIndex != -1) filter.setLut(cursor.getString(lutIndex));
        
        return filter;
    }
    
    private Transition cursorToTransition(Cursor cursor) {
        Transition transition = new Transition();
        
        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
        int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
        int typeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE);
        int durationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION);
        int clipStartIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLIP_START_ID);
        int clipEndIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLIP_END_ID);
        int thumbnailPathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL_PATH);
        int positionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION);
        
        if (idIndex != -1) transition.setId(cursor.getLong(idIndex));
        if (nameIndex != -1) transition.setName(cursor.getString(nameIndex));
        if (typeIndex != -1) transition.setType(cursor.getString(typeIndex));
        if (durationIndex != -1) transition.setDuration(cursor.getDouble(durationIndex));
        if (clipStartIdIndex != -1) transition.setClipStartId(cursor.getLong(clipStartIdIndex));
        if (clipEndIdIndex != -1) transition.setClipEndId(cursor.getLong(clipEndIdIndex));
        if (thumbnailPathIndex != -1) transition.setThumbnailPath(cursor.getString(thumbnailPathIndex));
        if (positionIndex != -1) transition.setPosition(cursor.getInt(positionIndex));
        
        return transition;
    }
    
    // Helper methods to get child elements for a project
    
    private List<VideoClip> getVideoClipsForProject(long projectId) {
        List<VideoClip> clips = new ArrayList<>();
        
        Cursor cursor = database.query(DatabaseHelper.TABLE_VIDEO_CLIPS, null, 
                DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)}, 
                null, null, DatabaseHelper.COLUMN_TIMELINE_POSITION + " ASC");
        
        if (cursor == null) {
            return clips;
        }
        
        if (!cursor.moveToFirst()) {
            cursor.close();
            return clips;
        }
        
        do {
            VideoClip clip = cursorToVideoClip(cursor);
            clips.add(clip);
        } while (cursor.moveToNext());
        
        cursor.close();
        return clips;
    }
    
    private List<AudioClip> getAudioClipsForProject(long projectId) {
        List<AudioClip> clips = new ArrayList<>();
        
        Cursor cursor = database.query(DatabaseHelper.TABLE_AUDIO_CLIPS, null, 
                DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)}, 
                null, null, DatabaseHelper.COLUMN_TIMELINE_POSITION + " ASC");
        
        if (cursor == null) {
            return clips;
        }
        
        if (!cursor.moveToFirst()) {
            cursor.close();
            return clips;
        }
        
        do {
            AudioClip clip = cursorToAudioClip(cursor);
            clips.add(clip);
        } while (cursor.moveToNext());
        
        cursor.close();
        return clips;
    }
    
    private List<TextOverlay> getTextOverlaysForProject(long projectId) {
        List<TextOverlay> overlays = new ArrayList<>();
        
        Cursor cursor = database.query(DatabaseHelper.TABLE_TEXT_OVERLAYS, null, 
                DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)}, 
                null, null, DatabaseHelper.COLUMN_START_TIME + " ASC");
        
        if (cursor == null) {
            return overlays;
        }
        
        if (!cursor.moveToFirst()) {
            cursor.close();
            return overlays;
        }
        
        do {
            TextOverlay overlay = cursorToTextOverlay(cursor);
            overlays.add(overlay);
        } while (cursor.moveToNext());
        
        cursor.close();
        return overlays;
    }
    
    private List<StickerOverlay> getStickerOverlaysForProject(long projectId) {
        List<StickerOverlay> overlays = new ArrayList<>();
        
        Cursor cursor = database.query(DatabaseHelper.TABLE_STICKER_OVERLAYS, null, 
                DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)}, 
                null, null, DatabaseHelper.COLUMN_START_TIME + " ASC");
        
        if (cursor == null) {
            return overlays;
        }
        
        if (!cursor.moveToFirst()) {
            cursor.close();
            return overlays;
        }
        
        do {
            StickerOverlay overlay = cursorToStickerOverlay(cursor);
            overlays.add(overlay);
        } while (cursor.moveToNext());
        
        cursor.close();
        return overlays;
    }
    
    private Filter getFilterForProject(long projectId) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_FILTERS, null, 
                DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)}, 
                null, null, null);
        
        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        
        Filter filter = cursorToFilter(cursor);
        cursor.close();
        return filter;
    }
    
    private List<Transition> getTransitionsForProject(long projectId) {
        List<Transition> transitions = new ArrayList<>();
        
        Cursor cursor = database.query(DatabaseHelper.TABLE_TRANSITIONS, null, 
                DatabaseHelper.COLUMN_PROJECT_ID + " = ?", new String[]{String.valueOf(projectId)}, 
                null, null, DatabaseHelper.COLUMN_POSITION + " ASC");
        
        if (cursor == null) {
            return transitions;
        }
        
        if (!cursor.moveToFirst()) {
            cursor.close();
            return transitions;
        }
        
        do {
            Transition transition = cursorToTransition(cursor);
            transitions.add(transition);
        } while (cursor.moveToNext());
        
        cursor.close();
        return transitions;
    }
}
