package com.example.snapeditprovs.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a transition between clips
 */
public class Transition {
    
    private String id;
    private String name;
    private String thumbnailPath;
    private String ffmpegCommand;
    private Map<String, Float> parameters;
    private int durationMs;
    
    public Transition() {
        this.parameters = new HashMap<>();
        this.durationMs = 1000; // Default 1 second
    }
    
    public Transition(String id, String name, String thumbnailPath, String ffmpegCommand) {
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
    
    public int getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
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
        
        // Replace duration placeholder with actual duration in seconds
        result = result.replace("{duration}", String.valueOf(durationMs / 1000.0f));
        
        return result;
    }
    
    /**
     * Create a clone of this transition
     * @return A new transition with the same properties
     */
    public Transition clone() {
        Transition clone = new Transition(id, name, thumbnailPath, ffmpegCommand);
        clone.parameters.putAll(this.parameters);
        clone.durationMs = this.durationMs;
        return clone;
    }
    
    /**
     * Set default values for predefined transition types
     * @param transitionType Type of transition to initialize
     */
    public void initDefaultValues(String transitionType) {
        switch (transitionType) {
            case "fade":
                this.id = "fade";
                this.name = "Fade";
                this.ffmpegCommand = "xfade=fade:duration={duration}";
                break;
            case "wipe_left":
                this.id = "wipe_left";
                this.name = "Wipe Left";
                this.ffmpegCommand = "xfade=wipeleft:duration={duration}";
                break;
            case "wipe_right":
                this.id = "wipe_right";
                this.name = "Wipe Right";
                this.ffmpegCommand = "xfade=wiperight:duration={duration}";
                break;
            case "wipe_up":
                this.id = "wipe_up";
                this.name = "Wipe Up";
                this.ffmpegCommand = "xfade=wipeup:duration={duration}";
                break;
            case "wipe_down":
                this.id = "wipe_down";
                this.name = "Wipe Down";
                this.ffmpegCommand = "xfade=wipedown:duration={duration}";
                break;
            case "slide_left":
                this.id = "slide_left";
                this.name = "Slide Left";
                this.ffmpegCommand = "xfade=slideleft:duration={duration}";
                break;
            case "slide_right":
                this.id = "slide_right";
                this.name = "Slide Right";
                this.ffmpegCommand = "xfade=slideright:duration={duration}";
                break;
            case "dissolve":
                this.id = "dissolve";
                this.name = "Dissolve";
                this.ffmpegCommand = "xfade=dissolve:duration={duration}";
                break;
            case "zoom_in":
                this.id = "zoom_in";
                this.name = "Zoom In";
                this.ffmpegCommand = "xfade=zoomin:duration={duration}";
                break;
            case "clock":
                this.id = "clock";
                this.name = "Clock";
                this.ffmpegCommand = "xfade=clock:duration={duration}";
                break;
            default:
                this.id = "none";
                this.name = "Cut";
                this.ffmpegCommand = "";
                this.durationMs = 0;
                break;
        }
    }
}
