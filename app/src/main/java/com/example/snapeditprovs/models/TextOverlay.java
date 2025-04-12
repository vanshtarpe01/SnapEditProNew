package com.example.snapeditprovs.models;

/**
 * Represents a text overlay in the video
 */
public class TextOverlay {
    private long id;
    private String text;
    private String fontName;
    private float fontSize;
    private int color;
    private int backgroundColor;
    private float positionX;
    private float positionY;
    private double startTime;
    private double endTime;
    private float rotation;
    private String animation; // e.g., "fade", "slide", "zoom"
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private int alignment; // 0=left, 1=center, 2=right

    public TextOverlay() {
        this.fontSize = 24.0f;
        this.color = 0xFFFFFFFF; // White
        this.backgroundColor = 0x00000000; // Transparent
        this.positionX = 0.5f; // Center
        this.positionY = 0.5f; // Center
        this.rotation = 0.0f;
        this.animation = "none";
        this.isBold = false;
        this.isItalic = false;
        this.isUnderline = false;
        this.alignment = 1; // Center
    }

    public TextOverlay(String text, String fontName, int color, double startTime, double endTime) {
        this();
        this.text = text;
        this.fontName = fontName;
        this.color = color;
        this.startTime = startTime;
        this.endTime = endTime;
    }

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

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public String getAnimation() {
        return animation;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
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

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setUnderline(boolean underline) {
        isUnderline = underline;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
    
    /**
     * Creates a deep copy of this text overlay
     */
    public TextOverlay duplicate() {
        TextOverlay copy = new TextOverlay();
        copy.setText(this.text);
        copy.setFontName(this.fontName);
        copy.setFontSize(this.fontSize);
        copy.setColor(this.color);
        copy.setBackgroundColor(this.backgroundColor);
        copy.setPositionX(this.positionX);
        copy.setPositionY(this.positionY);
        copy.setStartTime(this.startTime);
        copy.setEndTime(this.endTime);
        copy.setRotation(this.rotation);
        copy.setAnimation(this.animation);
        copy.setBold(this.isBold);
        copy.setItalic(this.isItalic);
        copy.setUnderline(this.isUnderline);
        copy.setAlignment(this.alignment);
        return copy;
    }
}
