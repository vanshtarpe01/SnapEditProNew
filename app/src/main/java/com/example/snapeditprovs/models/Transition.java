package com.example.snapeditprovs.models;

/**
 * Represents a transition between video clips
 */
public class Transition {
    private long id;
    private String name;
    private String type;
    private double duration;
    private long clipStartId;
    private long clipEndId;
    private String thumbnailPath;
    private int position; // Index in the transitions list

    public Transition() {
        this.duration = 1.0; // Default 1 second
    }

    public Transition(String name, String type, double duration) {
        this();
        this.name = name;
        this.type = type;
        this.duration = duration;
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

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public long getClipStartId() {
        return clipStartId;
    }

    public void setClipStartId(long clipStartId) {
        this.clipStartId = clipStartId;
    }

    public long getClipEndId() {
        return clipEndId;
    }

    public void setClipEndId(long clipEndId) {
        this.clipEndId = clipEndId;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Get FFmpeg command for this transition
     */
    public String toFFmpegTransitionCommand() {
        switch (type) {
            case "fade":
                return "xfade=fade:duration=" + duration;
            case "dissolve":
                return "xfade=dissolve:duration=" + duration;
            case "wipe":
                return "xfade=wipeleft:duration=" + duration;
            case "slide":
                return "xfade=slideleft:duration=" + duration;
            case "push":
                return "xfade=radial:duration=" + duration;
            case "zoom":
                return "xfade=fadeblack:duration=" + duration;
            case "whip":
                return "xfade=wipeup:duration=" + duration;
            case "circle":
                return "xfade=circleopen:duration=" + duration;
            case "clock":
                return "xfade=clock:duration=" + duration;
            default:
                return "xfade=fade:duration=" + duration;
        }
    }
    
    /**
     * Creates a deep copy of this transition
     */
    public Transition duplicate() {
        Transition copy = new Transition();
        copy.setName(this.name);
        copy.setType(this.type);
        copy.setDuration(this.duration);
        copy.setClipStartId(this.clipStartId);
        copy.setClipEndId(this.clipEndId);
        copy.setThumbnailPath(this.thumbnailPath);
        copy.setPosition(this.position);
        return copy;
    }

    /**
     * Create a collection of preset transitions
     */
    public static Transition[] createPresetTransitions() {
        Transition[] presets = new Transition[9];
        
        presets[0] = new Transition("Fade", "fade", 1.0);
        presets[1] = new Transition("Dissolve", "dissolve", 1.0);
        presets[2] = new Transition("Wipe", "wipe", 1.0);
        presets[3] = new Transition("Slide", "slide", 1.0);
        presets[4] = new Transition("Push", "push", 1.0);
        presets[5] = new Transition("Zoom", "zoom", 1.0);
        presets[6] = new Transition("Whip", "whip", 1.0);
        presets[7] = new Transition("Circle", "circle", 1.0);
        presets[8] = new Transition("Clock", "clock", 1.0);
        
        return presets;
    }
}
