package com.example.snapeditprovs.model;

import android.net.Uri;

/**
 * Model class representing an audio clip in the timeline
 */
public class AudioClip {
    
    private long id;
    private Uri uri;
    private int startTimeMs; // Start time in original audio
    private int endTimeMs;   // End time in original audio
    private int position;    // Position in timeline (ms)
    private int durationMs;  // Duration after trimming
    private float volume;
    private boolean isMuted;
    private boolean isVoiceRecording;
    private String name;
    
    public AudioClip() {
        this.volume = 1.0f;
        this.isMuted = false;
    }
    
    public AudioClip(Uri uri, int durationMs, String name) {
        this();
        this.uri = uri;
        this.startTimeMs = 0;
        this.endTimeMs = durationMs;
        this.durationMs = durationMs;
        this.name = name;
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
    
    public float getVolume() {
        return volume;
    }
    
    public void setVolume(float volume) {
        this.volume = volume;
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public void setMuted(boolean muted) {
        isMuted = muted;
    }
    
    public boolean isVoiceRecording() {
        return isVoiceRecording;
    }
    
    public void setVoiceRecording(boolean voiceRecording) {
        isVoiceRecording = voiceRecording;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Toggle mute status
     */
    public void toggleMute() {
        this.isMuted = !this.isMuted;
    }
    
    /**
     * Updates the duration based on the start and end time
     */
    private void updateDuration() {
        this.durationMs = endTimeMs - startTimeMs;
    }
    
    /**
     * Create fade-in effect over specified duration
     * @param durationMs Duration of fade in milliseconds
     */
    public void createFadeIn(int durationMs) {
        // In a real app, this would store fade parameters for rendering
    }
    
    /**
     * Create fade-out effect over specified duration
     * @param durationMs Duration of fade in milliseconds
     */
    public void createFadeOut(int durationMs) {
        // In a real app, this would store fade parameters for rendering
    }
}
