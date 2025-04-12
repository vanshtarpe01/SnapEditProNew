package com.example.snapeditprovs.models;

/**
 * Represents a single video clip in the timeline
 */
public class VideoClip {
    private long id;
    private String path;
    private double startTime;
    private double endTime;
    private double duration;
    private double timelinePosition;
    private int width;
    private int height;
    private float volume;
    private float speed;
    private String thumbnailPath;
    private boolean isMuted;
    private boolean isReversed;

    public VideoClip() {
        this.volume = 1.0f;
        this.speed = 1.0f;
        this.isMuted = false;
        this.isReversed = false;
    }

    public VideoClip(String path, double startTime, double endTime, double timelinePosition) {
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

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        // Adjust duration based on speed change
        this.duration = (endTime - startTime) / speed;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public boolean isReversed() {
        return isReversed;
    }

    public void setReversed(boolean reversed) {
        isReversed = reversed;
    }
    
    /**
     * Split this clip at the specified position
     * @param splitPosition position in seconds to split the clip
     * @return the new clip created after the split position
     */
    public VideoClip splitAt(double splitPosition) {
        if (splitPosition <= startTime || splitPosition >= endTime) {
            return null;
        }
        
        // Create a new clip for the second part
        VideoClip newClip = new VideoClip();
        newClip.setPath(this.path);
        newClip.setStartTime(splitPosition);
        newClip.setEndTime(this.endTime);
        newClip.setTimelinePosition(this.timelinePosition + (splitPosition - this.startTime));
        newClip.setWidth(this.width);
        newClip.setHeight(this.height);
        newClip.setVolume(this.volume);
        newClip.setSpeed(this.speed);
        newClip.setMuted(this.isMuted);
        newClip.setReversed(this.isReversed);
        
        // Adjust the current clip to end at the split point
        this.setEndTime(splitPosition);
        
        return newClip;
    }
    
    /**
     * Creates a deep copy of this clip
     */
    public VideoClip duplicate() {
        VideoClip copy = new VideoClip();
        copy.setPath(this.path);
        copy.setStartTime(this.startTime);
        copy.setEndTime(this.endTime);
        copy.setDuration(this.duration);
        copy.setTimelinePosition(this.timelinePosition);
        copy.setWidth(this.width);
        copy.setHeight(this.height);
        copy.setVolume(this.volume);
        copy.setSpeed(this.speed);
        copy.setThumbnailPath(this.thumbnailPath);
        copy.setMuted(this.isMuted);
        copy.setReversed(this.isReversed);
        return copy;
    }
}
