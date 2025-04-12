package com.example.snapeditprovs.models;

/**
 * Represents a sticker overlay in the video
 */
public class StickerOverlay {
    private long id;
    private String path;
    private float positionX;
    private float positionY;
    private float scale;
    private float rotation;
    private double startTime;
    private double endTime;
    private String animation; // e.g., "fade", "bounce", "rotate"

    public StickerOverlay() {
        this.positionX = 0.5f;
        this.positionY = 0.5f;
        this.scale = 1.0f;
        this.rotation = 0.0f;
        this.animation = "none";
    }

    public StickerOverlay(String path, double startTime, double endTime) {
        this();
        this.path = path;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
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

    public String getAnimation() {
        return animation;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
    }
    
    /**
     * Creates a deep copy of this sticker overlay
     */
    public StickerOverlay duplicate() {
        StickerOverlay copy = new StickerOverlay();
        copy.setPath(this.path);
        copy.setPositionX(this.positionX);
        copy.setPositionY(this.positionY);
        copy.setScale(this.scale);
        copy.setRotation(this.rotation);
        copy.setStartTime(this.startTime);
        copy.setEndTime(this.endTime);
        copy.setAnimation(this.animation);
        return copy;
    }
}
