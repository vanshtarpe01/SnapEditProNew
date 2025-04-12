package com.example.snapeditprovs.models;

/**
 * Represents export settings for a video project
 */
public class ExportSettings {
    private int resolution; // 720, 1080, 2160 (4K)
    private int bitrate; // in kbps
    private int framerate; // fps
    private String outputPath;
    private String outputFilename;
    private String format; // "mp4", "mov", "gif"
    private boolean includeAudio;
    private boolean hardwareAcceleration;

    public ExportSettings() {
        this.resolution = 1080;
        this.bitrate = 5000;
        this.framerate = 30;
        this.format = "mp4";
        this.includeAudio = true;
        this.hardwareAcceleration = true;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isIncludeAudio() {
        return includeAudio;
    }

    public void setIncludeAudio(boolean includeAudio) {
        this.includeAudio = includeAudio;
    }

    public boolean isHardwareAcceleration() {
        return hardwareAcceleration;
    }

    public void setHardwareAcceleration(boolean hardwareAcceleration) {
        this.hardwareAcceleration = hardwareAcceleration;
    }

    /**
     * Get the full output file path
     */
    public String getFullOutputPath() {
        if (outputPath == null || outputFilename == null) {
            return null;
        }
        return outputPath + "/" + outputFilename;
    }

    /**
     * Get FFmpeg command arguments for export
     */
    public String[] getFFmpegArgs(String inputPath) {
        String resolutionStr;
        
        if (resolution == 720) {
            resolutionStr = "1280:720";
        } else if (resolution == 1080) {
            resolutionStr = "1920:1080";
        } else if (resolution == 2160) {
            resolutionStr = "3840:2160";
        } else {
            resolutionStr = "1920:1080"; // Default to 1080p
        }
        
        String[] baseArgs = {
            "-i", inputPath,
            "-s", resolutionStr,
            "-b:v", bitrate + "k",
            "-r", String.valueOf(framerate),
            "-c:v", "libx264",
            "-preset", "medium",
            "-profile:v", "high",
            "-pix_fmt", "yuv420p"
        };
        
        if (includeAudio) {
            String[] withAudio = new String[baseArgs.length + 4];
            System.arraycopy(baseArgs, 0, withAudio, 0, baseArgs.length);
            withAudio[baseArgs.length] = "-c:a";
            withAudio[baseArgs.length + 1] = "aac";
            withAudio[baseArgs.length + 2] = "-b:a";
            withAudio[baseArgs.length + 3] = "128k";
            return withAudio;
        }
        
        return baseArgs;
    }
}
