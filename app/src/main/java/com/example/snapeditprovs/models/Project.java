package com.example.snapeditprovs.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a video editing project with its clips, audio, effects, etc.
 */
public class Project {
    private long id;
    private String name;
    private long createdAt;
    private long lastModified;
    private int width;
    private int height;
    private double duration;
    private List<VideoClip> videoClips;
    private List<AudioClip> audioClips;
    private List<TextOverlay> textOverlays;
    private List<StickerOverlay> stickerOverlays;
    private Filter appliedFilter;
    private List<Transition> transitions;
    private String thumbnailPath;

    public Project() {
        videoClips = new ArrayList<>();
        audioClips = new ArrayList<>();
        textOverlays = new ArrayList<>();
        stickerOverlays = new ArrayList<>();
        transitions = new ArrayList<>();
    }

    public Project(long id, String name, long createdAt) {
        this();
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.lastModified = createdAt;
    }

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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public List<VideoClip> getVideoClips() {
        return videoClips;
    }

    public void setVideoClips(List<VideoClip> videoClips) {
        this.videoClips = videoClips;
    }

    public void addVideoClip(VideoClip clip) {
        this.videoClips.add(clip);
        updateDuration();
    }

    public List<AudioClip> getAudioClips() {
        return audioClips;
    }

    public void setAudioClips(List<AudioClip> audioClips) {
        this.audioClips = audioClips;
    }

    public void addAudioClip(AudioClip clip) {
        this.audioClips.add(clip);
    }

    public List<TextOverlay> getTextOverlays() {
        return textOverlays;
    }

    public void setTextOverlays(List<TextOverlay> textOverlays) {
        this.textOverlays = textOverlays;
    }

    public void addTextOverlay(TextOverlay overlay) {
        this.textOverlays.add(overlay);
    }

    public List<StickerOverlay> getStickerOverlays() {
        return stickerOverlays;
    }

    public void setStickerOverlays(List<StickerOverlay> stickerOverlays) {
        this.stickerOverlays = stickerOverlays;
    }

    public void addStickerOverlay(StickerOverlay overlay) {
        this.stickerOverlays.add(overlay);
    }

    public Filter getAppliedFilter() {
        return appliedFilter;
    }

    public void setAppliedFilter(Filter appliedFilter) {
        this.appliedFilter = appliedFilter;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }

    public void addTransition(Transition transition) {
        this.transitions.add(transition);
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    private void updateDuration() {
        double totalDuration = 0;
        for (VideoClip clip : videoClips) {
            totalDuration += clip.getDuration();
        }
        this.duration = totalDuration;
    }
    
    /**
     * Creates a deep copy of this project for duplication
     */
    public Project duplicate() {
        Project copy = new Project();
        copy.setName(this.name + " (Copy)");
        copy.setCreatedAt(System.currentTimeMillis());
        copy.setLastModified(System.currentTimeMillis());
        copy.setWidth(this.width);
        copy.setHeight(this.height);
        copy.setDuration(this.duration);
        
        // Deep copy all lists
        for (VideoClip clip : this.videoClips) {
            copy.addVideoClip(clip.duplicate());
        }
        
        for (AudioClip clip : this.audioClips) {
            copy.addAudioClip(clip.duplicate());
        }
        
        for (TextOverlay overlay : this.textOverlays) {
            copy.addTextOverlay(overlay.duplicate());
        }
        
        for (StickerOverlay overlay : this.stickerOverlays) {
            copy.addStickerOverlay(overlay.duplicate());
        }
        
        for (Transition transition : this.transitions) {
            copy.addTransition(transition.duplicate());
        }
        
        if (this.appliedFilter != null) {
            copy.setAppliedFilter(this.appliedFilter.duplicate());
        }
        
        return copy;
    }
}
