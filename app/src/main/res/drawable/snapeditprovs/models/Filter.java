package com.example.snapeditprovs.models;

/**
 * Represents a video filter with various adjustments
 */
public class Filter {
    private long id;
    private String name;
    private String type; // "preset" or "custom"
    private float brightness;
    private float contrast;
    private float saturation;
    private float exposure;
    private float temperature;
    private float tint;
    private float vibrance;
    private float highlights;
    private float shadows;
    private String thumbnailPath;
    private String lut; // Look-up table path for preset filters

    public Filter() {
        this.brightness = 0.0f;
        this.contrast = 1.0f;
        this.saturation = 1.0f;
        this.exposure = 0.0f;
        this.temperature = 0.0f;
        this.tint = 0.0f;
        this.vibrance = 0.0f;
        this.highlights = 0.0f;
        this.shadows = 0.0f;
    }

    public Filter(String name, String type) {
        this();
        this.name = name;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getContrast() {
        return contrast;
    }

    public void setContrast(float contrast) {
        this.contrast = contrast;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public float getExposure() {
        return exposure;
    }

    public void setExposure(float exposure) {
        this.exposure = exposure;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getTint() {
        return tint;
    }

    public void setTint(float tint) {
        this.tint = tint;
    }

    public float getVibrance() {
        return vibrance;
    }

    public void setVibrance(float vibrance) {
        this.vibrance = vibrance;
    }

    public float getHighlights() {
        return highlights;
    }

    public void setHighlights(float highlights) {
        this.highlights = highlights;
    }

    public float getShadows() {
        return shadows;
    }

    public void setShadows(float shadows) {
        this.shadows = shadows;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getLut() {
        return lut;
    }

    public void setLut(String lut) {
        this.lut = lut;
    }

    /**
     * Generates FFmpeg filter string for this filter
     */
    public String toFFmpegFilterString() {
        StringBuilder filterBuilder = new StringBuilder();
        
        if (type.equals("preset") && lut != null && !lut.isEmpty()) {
            // Use LUT (Look-Up Table) for preset filters
            filterBuilder.append("lut3d=").append(lut);
        } else {
            // Apply individual adjustments
            if (brightness != 0.0f) {
                filterBuilder.append("eq=brightness=").append(brightness + 1.0f).append(",");
            }
            
            if (contrast != 1.0f) {
                filterBuilder.append("eq=contrast=").append(contrast).append(",");
            }
            
            if (saturation != 1.0f) {
                filterBuilder.append("eq=saturation=").append(saturation).append(",");
            }
            
            if (exposure != 0.0f) {
                filterBuilder.append("eq=gamma=").append(1.0f / (1.0f + exposure)).append(",");
            }
            
            // Temperature and tint require more complex filtering
            if (temperature != 0.0f || tint != 0.0f) {
                filterBuilder.append("colortemperature=").append(3500 + (temperature * 3000)).append(",");
            }
            
            // Remove trailing comma if exists
            if (filterBuilder.length() > 0 && filterBuilder.charAt(filterBuilder.length() - 1) == ',') {
                filterBuilder.setLength(filterBuilder.length() - 1);
            }
        }
        
        return filterBuilder.toString();
    }
    
    /**
     * Creates a deep copy of this filter
     */
    public Filter duplicate() {
        Filter copy = new Filter();
        copy.setName(this.name);
        copy.setType(this.type);
        copy.setBrightness(this.brightness);
        copy.setContrast(this.contrast);
        copy.setSaturation(this.saturation);
        copy.setExposure(this.exposure);
        copy.setTemperature(this.temperature);
        copy.setTint(this.tint);
        copy.setVibrance(this.vibrance);
        copy.setHighlights(this.highlights);
        copy.setShadows(this.shadows);
        copy.setThumbnailPath(this.thumbnailPath);
        copy.setLut(this.lut);
        return copy;
    }

    /**
     * Create a collection of preset filters
     */
    public static Filter[] createPresetFilters() {
        Filter[] presets = new Filter[10];
        
        // Normal (no filter)
        presets[0] = new Filter("Normal", "preset");
        
        // Black & White
        presets[1] = new Filter("Black & White", "preset");
        presets[1].setSaturation(0.0f);
        
        // Vintage
        presets[2] = new Filter("Vintage", "preset");
        presets[2].setSaturation(0.7f);
        presets[2].setTemperature(0.2f);
        presets[2].setContrast(1.2f);
        
        // Warm
        presets[3] = new Filter("Warm", "preset");
        presets[3].setTemperature(0.3f);
        
        // Cool
        presets[4] = new Filter("Cool", "preset");
        presets[4].setTemperature(-0.3f);
        
        // Vibrant
        presets[5] = new Filter("Vibrant", "preset");
        presets[5].setSaturation(1.5f);
        presets[5].setContrast(1.2f);
        
        // Dramatic
        presets[6] = new Filter("Dramatic", "preset");
        presets[6].setContrast(1.5f);
        presets[6].setSaturation(1.2f);
        presets[6].setShadows(0.3f);
        
        // Faded
        presets[7] = new Filter("Faded", "preset");
        presets[7].setContrast(0.8f);
        presets[7].setSaturation(0.8f);
        presets[7].setBrightness(0.1f);
        
        // High Contrast
        presets[8] = new Filter("High Contrast", "preset");
        presets[8].setContrast(1.8f);
        
        // Noir
        presets[9] = new Filter("Noir", "preset");
        presets[9].setSaturation(0.0f);
        presets[9].setContrast(1.4f);
        presets[9].setShadows(0.4f);
        
        return presets;
    }
}
