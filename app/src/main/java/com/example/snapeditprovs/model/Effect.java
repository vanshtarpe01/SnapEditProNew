package com.example.snapeditprovs.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a video effect
 */
public class Effect {
    
    private String id;
    private String name;
    private String thumbnailPath;
    private String ffmpegCommand;
    private Map<String, Float> parameters;
    private int startTimeMs;  // When to start the effect
    private int endTimeMs;    // When to end the effect
    private boolean isFullDuration; // Whether effect applies to entire clip
    
    public Effect() {
        this.parameters = new HashMap<>();
        this.isFullDuration = true;
    }
    
    public Effect(String id, String name, String thumbnailPath, String ffmpegCommand) {
        this();
        this.id = id;
        this.name = name;
        this.thumbnailPath = thumbnailPath;
        this.ffmpegCommand = ffmpegCommand;
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getThumbnailPath() {
        return thumbnailPath;
    }
    
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
    
    public String getFfmpegCommand() {
        return ffmpegCommand;
    }
    
    public void setFfmpegCommand(String ffmpegCommand) {
        this.ffmpegCommand = ffmpegCommand;
    }
    
    public Map<String, Float> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Float> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(String key, float value) {
        this.parameters.put(key, value);
    }
    
    public Float getParameter(String key) {
        return this.parameters.get(key);
    }
    
    public int getStartTimeMs() {
        return startTimeMs;
    }
    
    public void setStartTimeMs(int startTimeMs) {
        this.startTimeMs = startTimeMs;
    }
    
    public int getEndTimeMs() {
        return endTimeMs;
    }
    
    public void setEndTimeMs(int endTimeMs) {
        this.endTimeMs = endTimeMs;
    }
    
    public boolean isFullDuration() {
        return isFullDuration;
    }
    
    public void setFullDuration(boolean fullDuration) {
        isFullDuration = fullDuration;
    }
    
    /**
     * Get the duration of this effect
     * @return Duration in milliseconds
     */
    public int getDurationMs() {
        return endTimeMs - startTimeMs;
    }
    
    /**
     * Get the FFmpeg command with parameter values applied
     * @return Ready-to-use FFmpeg filter command
     */
    public String getProcessedFfmpegCommand() {
        String result = ffmpegCommand;
        for (Map.Entry<String, Float> entry : parameters.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }
    
    /**
     * Create a clone of this effect
     * @return A new effect with the same properties
     */
    public Effect clone() {
        Effect clone = new Effect(id, name, thumbnailPath, ffmpegCommand);
        clone.parameters.putAll(this.parameters);
        clone.startTimeMs = this.startTimeMs;
        clone.endTimeMs = this.endTimeMs;
        clone.isFullDuration = this.isFullDuration;
        return clone;
    }
    
    /**
     * Set default values for predefined effect types
     * @param effectType Type of effect to initialize
     */
    public void initDefaultValues(String effectType) {
        switch (effectType) {
            case "blur":
                this.id = "blur";
                this.name = "Blur";
                this.ffmpegCommand = "boxblur=10:1";
                break;
            case "sharpen":
                this.id = "sharpen";
                this.name = "Sharpen";
                this.ffmpegCommand = "unsharp=5:5:1.5:5:5:0";
                break;
            case "glitch":
                this.id = "glitch";
                this.name = "Glitch";
                this.ffmpegCommand = "rgbashift=rh=5:bv=5:gh=-5";
                break;
            case "vhs":
                this.id = "vhs";
                this.name = "VHS";
                this.ffmpegCommand = "noise=c0s=8:allf=t+rgbashift=rh=1:bv=1";
                break;
            case "zoom":
                this.id = "zoom";
                this.name = "Zoom";
                this.ffmpegCommand = "zoompan=z='min(zoom+0.001,1.5)':d=25";
                break;
            case "mirror":
                this.id = "mirror";
                this.name = "Mirror";
                this.ffmpegCommand = "hflip";
                break;
            case "rotate":
                this.id = "rotate";
                this.name = "Rotate";
                this.ffmpegCommand = "rotate=45*PI/180";
                break;
            case "slow_motion":
                this.id = "slow_motion";
                this.name = "Slow Motion";
                this.ffmpegCommand = "setpts=2.0*PTS";
                break;
            case "fast_motion":
                this.id = "fast_motion";
                this.name = "Fast Motion";
                this.ffmpegCommand = "setpts=0.5*PTS";
                break;
            default:
                this.id = "none";
                this.name = "None";
                this.ffmpegCommand = "";
                break;
        }
    }
}
