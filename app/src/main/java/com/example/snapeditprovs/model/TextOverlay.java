package com.example.snapeditprovs.model;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Model class representing a text overlay in the timeline
 */
public class TextOverlay {
    
    private long id;
    private String text;
    private int startTimeMs; // When to show the text
    private int endTimeMs;   // When to hide the text
    private float xPosition; // Normalized (0.0 - 1.0)
    private float yPosition; // Normalized (0.0 - 1.0)
    private float rotation;
    private float scale;
    private int textColor;
    private int backgroundColor;
    private float backgroundOpacity;
    private String fontName;
    private int fontSize;
    private boolean isBold;
    private boolean isItalic;
    private boolean hasAnimation;
    private String animationType; // e.g., "fade", "slide", "zoom"
    
    public TextOverlay() {
        this.text = "";
        this.xPosition = 0.5f; // Center by default
        this.yPosition = 0.5f; // Center by default
        this.rotation = 0.0f;
        this.scale = 1.0f;
        this.textColor = Color.WHITE;
        this.backgroundColor = Color.TRANSPARENT;
        this.backgroundOpacity = 0.0f;
        this.fontName = "sans-serif";
        this.fontSize = 24;
        this.isBold = false;
        this.isItalic = false;
        this.hasAnimation = false;
        this.animationType = "none";
    }
    
    public TextOverlay(String text, int startTimeMs, int endTimeMs) {
        this();
        this.text = text;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
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
    
    public float getXPosition() {
        return xPosition;
    }
    
    public void setXPosition(float xPosition) {
        this.xPosition = xPosition;
    }
    
    public float getYPosition() {
        return yPosition;
    }
    
    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
    }
    
    public float getRotation() {
        return rotation;
    }
    
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
    }
    
    public int getTextColor() {
        return textColor;
    }
    
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
    
    public int getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public float getBackgroundOpacity() {
        return backgroundOpacity;
    }
    
    public void setBackgroundOpacity(float backgroundOpacity) {
        this.backgroundOpacity = backgroundOpacity;
    }
    
    public String getFontName() {
        return fontName;
    }
    
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }
    
    public int getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    
    public boolean isBold() {
        return isBold;
    }
    
    public void setBold(boolean bold) {
        isBold = bold;
    }
    
    public boolean isItalic() {
        return isItalic;
    }
    
    public void setItalic(boolean italic) {
        isItalic = italic;
    }
    
    public boolean isHasAnimation() {
        return hasAnimation;
    }
    
    public void setHasAnimation(boolean hasAnimation) {
        this.hasAnimation = hasAnimation;
    }
    
    public String getAnimationType() {
        return animationType;
    }
    
    public void setAnimationType(String animationType) {
        this.animationType = animationType;
        this.hasAnimation = !animationType.equals("none");
    }
    
    /**
     * Get duration of the text overlay
     * @return Duration in milliseconds
     */
    public int getDurationMs() {
        return endTimeMs - startTimeMs;
    }
    
    /**
     * Get the typeface based on bold and italic settings
     * @return The typeface style
     */
    public int getTypefaceStyle() {
        if (isBold && isItalic) {
            return Typeface.BOLD_ITALIC;
        } else if (isBold) {
            return Typeface.BOLD;
        } else if (isItalic) {
            return Typeface.ITALIC;
        } else {
            return Typeface.NORMAL;
        }
    }
}
