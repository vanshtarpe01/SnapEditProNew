package com.example.snapeditprovs.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing a video editing project
 */
public class VideoProject {
    
    private long id;
    private String name;
    private Date createdAt;
    private Date modifiedAt;
    private int durationMs;
    private String thumbnailPath;
    private List<VideoClip> videoClips;
    private List<AudioClip> audioClips;
    private List<TextOverlay> textOverlays;
    
    public VideoProject() {
        this.createdAt = new Date();
        this.modifiedAt = new Date();
        this.videoClips = new ArrayList<>();
        this.audioClips = new ArrayList<>();
        this.textOverlays = new ArrayList<>();
    }
    
    public VideoProject(long id, String name, int durationMs, String thumbnailPath) {
        this();
        this.id = id;
        this.name = name;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getModifiedAt() {
        return modifiedAt;
    }
    
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
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
    
    public List<VideoClip> getVideoClips() {
        return videoClips;
    }
    
    public void setVideoClips(List<VideoClip> videoClips) {
        this.videoClips = videoClips;
    }
    
    public void addVideoClip(VideoClip videoClip) {
        this.videoClips.add(videoClip);
        updateModifiedDate();
    }
    
    public List<AudioClip> getAudioClips() {
        return audioClips;
    }
    
    public void setAudioClips(List<AudioClip> audioClips) {
        this.audioClips = audioClips;
    }
    
    public void addAudioClip(AudioClip audioClip) {
        this.audioClips.add(audioClip);
        updateModifiedDate();
    }
    
    public List<TextOverlay> getTextOverlays() {
        return textOverlays;
    }
    
    public void setTextOverlays(List<TextOverlay> textOverlays) {
        this.textOverlays = textOverlays;
    }
    
    public void addTextOverlay(TextOverlay textOverlay) {
        this.textOverlays.add(textOverlay);
        updateModifiedDate();
    }
    
    public void updateModifiedDate() {
        this.modifiedAt = new Date();
    }
    
    /**
     * Calculate total duration of the project based on the clips
     * @return Duration in milliseconds
     */
    public int calculateTotalDuration() {
        int totalDuration = 0;
        for (VideoClip clip : videoClips) {
            totalDuration += clip.getDurationMs();
        }
        return totalDuration;
    }
    
    /**
     * Updates the duration of the project based on the video clips
     */
    public void updateDuration() {
        this.durationMs = calculateTotalDuration();
    }
}
