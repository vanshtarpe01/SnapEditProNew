package com.example.snapeditprovs.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a video filter
 */
public class Filter {
    
    private String id;
    private String name;
    private String thumbnailPath;
    private String ffmpegCommand;
    private Map<String, Float> parameters;
    
    public Filter() {
        this.parameters = new HashMap<>();
    }
    
    public Filter(String id, String name, String thumbnailPath, String ffmpegCommand) {
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
     * Create a clone of this filter
     * @return A new filter with the same properties
     */
    public Filter clone() {
        Filter clone = new Filter(id, name, thumbnailPath, ffmpegCommand);
        clone.parameters.putAll(this.parameters);
        return clone;
    }
    
    /**
     * Set default values for predefined filter types
     * @param filterType Type of filter to initialize
     */
    public void initDefaultValues(String filterType) {
        switch (filterType) {
            case "grayscale":
                this.id = "grayscale";
                this.name = "Black & White";
                this.ffmpegCommand = "colorchannelmixer=.3:.4:.3:0:.3:.4:.3:0:.3:.4:.3";
                break;
            case "sepia":
                this.id = "sepia";
                this.name = "Sepia";
                this.ffmpegCommand = "colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131";
                break;
            case "vintage":
                this.id = "vintage";
                this.name = "Vintage";
                this.ffmpegCommand = "curves=vintage";
                break;
            case "vignette":
                this.id = "vignette";
                this.name = "Vignette";
                this.ffmpegCommand = "vignette=PI/4";
                break;
            case "vibrant":
                this.id = "vibrant";
                this.name = "Vibrant";
                this.ffmpegCommand = "eq=saturation=1.5:contrast=1.2";
                break;
            case "cool":
                this.id = "cool";
                this.name = "Cool";
                this.ffmpegCommand = "colortemperature=temperature=7000";
                break;
            case "warm":
                this.id = "warm";
                this.name = "Warm";
                this.ffmpegCommand = "colortemperature=temperature=4000";
                break;
            default:
                this.id = "none";
                this.name = "None";
                this.ffmpegCommand = "";
                break;
        }
    }
}
