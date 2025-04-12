package com.example.snapeditprovs.models;

/**
 * Represents an audio clip in the timeline
 */
public class AudioClip {
    private long id;
    private String path;
    private double startTime;
    private double endTime;
    private double duration;
    private double timelinePosition;
    private float volume;
    private boolean isFadeIn;
    private boolean isFadeOut;
    private float fadeInDuration;
    private float fadeOutDuration;
    private int type; // 0 = background music, 1 = voice recording, 2 = sound effect

    public AudioClip() {
        this.volume = 1.0f;
        this.isFadeIn = false;
        this.isFadeOut = false;
        this.fadeInDuration = 0.5f;
        this.fadeOutDuration = 0.5f;
    }

    public AudioClip(String path, double startTime, double endTime, double timelinePosition) {
        this();
        this.path = path;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
        this.timelinePosition = timelinePosition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
        this.duration = endTime - startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
        this.duration = endTime - startTime;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
        this.endTime = startTime + duration;
    }

    public double getTimelinePosition() {
        return timelinePosition;
    }

    public void setTimelinePosition(double timelinePosition) {
        this.timelinePosition = timelinePosition;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public boolean isFadeIn() {
        return isFadeIn;
    }

    public void setFadeIn(boolean fadeIn) {
        isFadeIn = fadeIn;
    }

    public boolean isFadeOut() {
        return isFadeOut;
    }

    public void setFadeOut(boolean fadeOut) {
        isFadeOut = fadeOut;
    }

    public float getFadeInDuration() {
        return fadeInDuration;
    }

    public void setFadeInDuration(float fadeInDuration) {
        this.fadeInDuration = fadeInDuration;
    }

    public float getFadeOutDuration() {
        return fadeOutDuration;
    }

    public void setFadeOutDuration(float fadeOutDuration) {
        this.fadeOutDuration = fadeOutDuration;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Split this audio clip at the specified position
     * @param splitPosition position in seconds to split the clip
     * @return the new clip created after the split position
     */
    public AudioClip splitAt(double splitPosition) {
        if (splitPosition <= startTime || splitPosition >= endTime) {
            return null;
        }
        
        // Create a new clip for the second part
        AudioClip newClip = new AudioClip();
        newClip.setPath(this.path);
        newClip.setStartTime(splitPosition);
        newClip.setEndTime(this.endTime);
        newClip.setTimelinePosition(this.timelinePosition + (splitPosition - this.startTime));
        newClip.setVolume(this.volume);
        newClip.setFadeIn(false); // Reset fade in for new clip
        newClip.setFadeOut(this.isFadeOut);
        newClip.setFadeOutDuration(this.fadeOutDuration);
        newClip.setType(this.type);
        
        // Adjust the current clip to end at the split point
        this.setEndTime(splitPosition);
        // Keep fade in but reset fade out for first part
        this.setFadeOut(false);
        
        return newClip;
    }
    
    /**
     * Creates a deep copy of this audio clip
     */
    public AudioClip duplicate() {
        AudioClip copy = new AudioClip();
        copy.setPath(this.path);
        copy.setStartTime(this.startTime);
        copy.setEndTime(this.endTime);
        copy.setDuration(this.duration);
        copy.setTimelinePosition(this.timelinePosition);
        copy.setVolume(this.volume);
        copy.setFadeIn(this.isFadeIn);
        copy.setFadeOut(this.isFadeOut);
        copy.setFadeInDuration(this.fadeInDuration);
        copy.setFadeOutDuration(this.fadeOutDuration);
        copy.setType(this.type);
        return copy;
    }
}
