package com.example.snapeditprovs.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a video clip in the timeline
 */
public class VideoClip {
    
    private long id;
    private Uri uri;
    private int startTimeMs; // Start time in the original video
    private int endTimeMs;   // End time in the original video
    private int position;    // Position in the timeline (in ms)
    private int durationMs;  // Duration of the clip (after trimming)
    private String thumbnailPath;
    private Filter appliedFilter;
    private List<Effect> appliedEffects;
    private Transition inTransition;
    private Transition outTransition;
    private float volume;
    private float playbackSpeed;
    
    public VideoClip() {
        this.appliedEffects = new ArrayList<>();
        this.volume = 1.0f;
        this.playbackSpeed = 1.0f;
    }
    
    public VideoClip(Uri uri, int durationMs, String thumbnailPath) {
        this();
        this.uri = uri;
        this.startTimeMs = 0;
        this.endTimeMs = durationMs;
        this.durationMs = durationMs;
        this.thumbnailPath = thumbnailPath;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Uri getUri() {
        return uri;
    }
    
    public void setUri(Uri uri) {
        this.uri = uri;
    }
    
    public int getStartTimeMs() {
        return startTimeMs;
    }
    
    public void setStartTimeMs(int startTimeMs) {
        this.startTimeMs = startTimeMs;
        updateDuration();
    }
    
    public int getEndTimeMs() {
        return endTimeMs;
    }
    
    public void setEndTimeMs(int endTimeMs) {
        this.endTimeMs = endTimeMs;
        updateDuration();
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public int getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }
    
    public String getThumbnailPath() {
        return thumbnailPath;
    }
    
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
    
    public Filter getAppliedFilter() {
        return appliedFilter;
    }
    
    public void setAppliedFilter(Filter appliedFilter) {
        this.appliedFilter = appliedFilter;
    }
    
    public List<Effect> getAppliedEffects() {
        return appliedEffects;
    }
    
    public void setAppliedEffects(List<Effect> appliedEffects) {
        this.appliedEffects = appliedEffects;
    }
    
    public void addEffect(Effect effect) {
        this.appliedEffects.add(effect);
    }
    
    public void removeEffect(Effect effect) {
        this.appliedEffects.remove(effect);
    }
    
    public Transition getInTransition() {
        return inTransition;
    }
    
    public void setInTransition(Transition inTransition) {
        this.inTransition = inTransition;
    }
    
    public Transition getOutTransition() {
        return outTransition;
    }
    
    public void setOutTransition(Transition outTransition) {
        this.outTransition = outTransition;
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setVolume(float volume) {
        this.volume = volume;
    }
    
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }
    
    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
        updateDuration();
    }
    
    /**
     * Updates the duration based on the start and end time
     */
    private void updateDuration() {
        // Calculate base duration from trim points
        int baseDuration = endTimeMs - startTimeMs;
        
        // Adjust for playback speed
        durationMs = (int) (baseDuration / playbackSpeed);
    }
    
    /**
     * Create a duplicate of this clip (for split operations)
     */
    public VideoClip duplicate() {
        VideoClip newClip = new VideoClip();
        newClip.uri = this.uri;
        newClip.startTimeMs = this.startTimeMs;
        newClip.endTimeMs = this.endTimeMs;
        newClip.durationMs = this.durationMs;
        newClip.thumbnailPath = this.thumbnailPath;
        newClip.appliedFilter = this.appliedFilter;
        newClip.appliedEffects.addAll(this.appliedEffects);
        newClip.volume = this.volume;
        newClip.playbackSpeed = this.playbackSpeed;
        
        return newClip;
    }
    
    /**
     * Split this clip at the given time point
     * @param splitTimeMs Time in milliseconds where to split the clip
     * @return A new clip representing the second part after the split
     */
    public VideoClip splitAt(int splitTimeMs) {
        // Ensure split point is within clip boundaries
        if (splitTimeMs <= startTimeMs || splitTimeMs >= endTimeMs) {
            return null;
        }
        
        // Create a new clip for the second part
        VideoClip secondPart = this.duplicate();
        
        // Adjust original clip (first part)
        int originalEndTime = this.endTimeMs;
        this.endTimeMs = startTimeMs + splitTimeMs;
        this.updateDuration();
        
        // Adjust second part
        secondPart.startTimeMs = startTimeMs + splitTimeMs;
        secondPart.endTimeMs = originalEndTime;
        secondPart.position = this.position + this.durationMs;
        secondPart.updateDuration();
        
        return secondPart;
    }
}
