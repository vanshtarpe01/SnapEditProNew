package com.example.snapeditprovs.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import com.example.snapeditprovs.models.AudioClip;
import com.example.snapeditprovs.models.ExportSettings;
import com.example.snapeditprovs.models.Project;
import com.example.snapeditprovs.models.TextOverlay;
import com.example.snapeditprovs.models.Transition;
import com.example.snapeditprovs.models.VideoClip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FFmpegUtils {
    private static final String TAG = "FFmpegUtils";

    /**
     * Generate FFmpeg command for exporting a project
     * @param project The project to export
     * @param settings Export settings
     * @return FFmpeg command string
     */
    public static String generateExportCommand(Project project, ExportSettings settings) {
        StringBuilder command = new StringBuilder();
        
        // Add video inputs
        List<String> inputFiles = new ArrayList<>();
        List<VideoClip> videoClips = project.getVideoClips();
        
        // First list all input files
        for (int i = 0; i < videoClips.size(); i++) {
            VideoClip clip = videoClips.get(i);
            inputFiles.add(clip.getPath());
            command.append("-i ").append(clip.getPath()).append(" ");
        }
        
        // Add audio inputs
        List<AudioClip> audioClips = project.getAudioClips();
        for (AudioClip audioClip : audioClips) {
            inputFiles.add(audioClip.getPath());
            command.append("-i ").append(audioClip.getPath()).append(" ");
        }
        
        // Start building the filter complex
        command.append("-filter_complex \"");
        
        // Process video clips
        List<String> videoStreams = new ArrayList<>();
        for (int i = 0; i < videoClips.size(); i++) {
            VideoClip clip = videoClips.get(i);
            
            // Trim the clip
            String trimFilter = String.format(Locale.US, 
                    "[%d:v]trim=%f:%f,setpts=PTS-STARTPTS[v%d];", 
                    i, clip.getStartTime(), clip.getEndTime(), i);
            command.append(trimFilter);
            
            // Apply speed effect
            if (clip.getSpeed() != 1.0f) {
                command.append(String.format(Locale.US, 
                        "[v%d]setpts=%f*PTS[v%d];", 
                        i, 1.0/clip.getSpeed(), i));
            }
            
            // Apply filter if needed
            if (project.getAppliedFilter() != null) {
                command.append(String.format(Locale.US, 
                        "[v%d]%s[v%d];", 
                        i, project.getAppliedFilter().toFFmpegFilterString(), i));
            }
            
            // If clip is reversed
            if (clip.isReversed()) {
                command.append(String.format(Locale.US, 
                        "[v%d]reverse[v%d];", 
                        i, i));
            }
            
            videoStreams.add("[v" + i + "]");
        }
        
        // Process audio clips
        List<String> audioStreams = new ArrayList<>();
        int audioInputIndex = videoClips.size();
        
        // First process audio from video clips
        for (int i = 0; i < videoClips.size(); i++) {
            VideoClip clip = videoClips.get(i);
            
            if (!clip.isMuted()) {
                // Trim audio
                String trimFilter = String.format(Locale.US, 
                        "[%d:a]atrim=%f:%f,asetpts=PTS-STARTPTS[a%d];", 
                        i, clip.getStartTime(), clip.getEndTime(), i);
                command.append(trimFilter);
                
                // Apply volume
                if (clip.getVolume() != 1.0f) {
                    command.append(String.format(Locale.US, 
                            "[a%d]volume=%f[a%d];", 
                            i, clip.getVolume(), i));
                }
                
                // If clip is reversed
                if (clip.isReversed()) {
                    command.append(String.format(Locale.US, 
                            "[a%d]areverse[a%d];", 
                            i, i));
                }
                
                audioStreams.add("[a" + i + "]");
            }
        }
        
        // Process additional audio clips
        for (int i = 0; i < audioClips.size(); i++) {
            AudioClip clip = audioClips.get(i);
            int audioIndex = audioInputIndex + i;
            
            // Trim audio
            String trimFilter = String.format(Locale.US, 
                    "[%d:a]atrim=%f:%f,asetpts=PTS-STARTPTS[aa%d];", 
                    audioIndex, clip.getStartTime(), clip.getEndTime(), i);
            command.append(trimFilter);
            
            // Apply fade in/out
            if (clip.isFadeIn() || clip.isFadeOut()) {
                StringBuilder fadeFilter = new StringBuilder("[aa" + i + "]afade=");
                
                if (clip.isFadeIn()) {
                    fadeFilter.append("t=in:st=0:d=").append(clip.getFadeInDuration());
                }
                
                if (clip.isFadeIn() && clip.isFadeOut()) {
                    fadeFilter.append(",afade=");
                }
                
                if (clip.isFadeOut()) {
                    double outStart = clip.getDuration() - clip.getFadeOutDuration();
                    fadeFilter.append("t=out:st=").append(outStart)
                            .append(":d=").append(clip.getFadeOutDuration());
                }
                
                fadeFilter.append("[aa").append(i).append("];");
                command.append(fadeFilter);
            }
            
            // Apply volume
            if (clip.getVolume() != 1.0f) {
                command.append(String.format(Locale.US, 
                        "[aa%d]volume=%f[aa%d];", 
                        i, clip.getVolume(), i));
            }
            
            audioStreams.add("[aa" + i + "]");
        }
        
        // Concatenate video clips with transitions
        if (videoClips.size() > 1 && project.getTransitions().size() > 0) {
            for (int i = 0; i < videoClips.size() - 1; i++) {
                Transition transition = null;
                // Find transition for this position
                for (Transition t : project.getTransitions()) {
                    if (t.getPosition() == i) {
                        transition = t;
                        break;
                    }
                }
                
                if (transition != null) {
                    // Apply transition
                    command.append(String.format(Locale.US, 
                            "%s%s%s", 
                            videoStreams.get(i), videoStreams.get(i + 1), transition.toFFmpegTransitionCommand()));
                    
                    // Update stream names for next operation
                    videoStreams.set(i, "[v_trans" + i + "]");
                    videoStreams.remove(i + 1);
                    i--; // Process this position again with the next clip
                } else {
                    // No transition, just concatenate
                    if (i == 0) {
                        command.append(videoStreams.get(0) + videoStreams.get(1) + "concat=n=2:v=1:a=0[v_concat0];");
                        videoStreams.set(0, "[v_concat0]");
                        videoStreams.remove(1);
                        i--; // Process this position again with the next clip
                    }
                }
            }
        } else if (videoClips.size() > 1) {
            // Simple concatenation without transitions
            StringBuilder concatFilter = new StringBuilder();
            for (String stream : videoStreams) {
                concatFilter.append(stream);
            }
            concatFilter.append("concat=n=").append(videoClips.size())
                        .append(":v=1:a=0[v_final];");
            command.append(concatFilter);
            videoStreams.clear();
            videoStreams.add("[v_final]");
        }
        
        // Add text overlays
        if (!project.getTextOverlays().isEmpty()) {
            String baseStream = videoStreams.get(0);
            videoStreams.remove(0);
            
            for (int i = 0; i < project.getTextOverlays().size(); i++) {
                TextOverlay overlay = project.getTextOverlays().get(i);
                
                // Generate drawtext filter
                StringBuilder textFilter = new StringBuilder(baseStream);
                textFilter.append("drawtext=text='").append(overlay.getText()).append("'")
                          .append(":fontfile=/system/fonts/").append(overlay.getFontName().contains(" ") ? 
                                  overlay.getFontName().replace(" ", "").toLowerCase() : overlay.getFontName().toLowerCase())
                          .append(".ttf")
                          .append(":fontsize=").append((int)overlay.getFontSize())
                          .append(":fontcolor=0x").append(Integer.toHexString(overlay.getColor()))
                          .append(":x=").append((int)(overlay.getPositionX() * settings.getResolution()))
                          .append(":y=").append((int)(overlay.getPositionY() * settings.getResolution()))
                          .append(":enable='between(t,").append(overlay.getStartTime()).append(",").append(overlay.getEndTime()).append(")'");
                
                // Add text styling
                if (overlay.isBold()) {
                    textFilter.append(":fontweight=bold");
                }
                if (overlay.isItalic()) {
                    textFilter.append(":italic=1");
                }
                
                // Add text animation if specified
                if (overlay.getAnimation() != null && !overlay.getAnimation().equals("none")) {
                    double animDuration = 0.5; // animation duration in seconds
                    double startTime = overlay.getStartTime();
                    double endTime = overlay.getEndTime();
                    
                    if (overlay.getAnimation().equals("fade")) {
                        textFilter.append(":alpha='if(lt(t,").append(startTime + animDuration).append("),");
                        textFilter.append("(t-").append(startTime).append(")/").append(animDuration).append(",");
                        textFilter.append("if(gt(t,").append(endTime - animDuration).append("),");
                        textFilter.append("(").append(endTime).append("-t)/").append(animDuration).append(",1))'");
                    } else if (overlay.getAnimation().equals("slide")) {
                        // Start offscreen and slide in
                        textFilter.append(":x='if(lt(t,").append(startTime + animDuration).append("),");
                        textFilter.append("(t-").append(startTime).append(")/").append(animDuration).append("*");
                        textFilter.append((int)(overlay.getPositionX() * settings.getResolution())).append(",");
                        textFilter.append((int)(overlay.getPositionX() * settings.getResolution())).append(")'");
                    }
                }
                
                textFilter.append("[v_text").append(i).append("];");
                command.append(textFilter);
                
                baseStream = "[v_text" + i + "]";
            }
            
            videoStreams.add(baseStream);
        }
        
        // Mix audio streams if there are multiple
        if (audioStreams.size() > 1) {
            StringBuilder mixFilter = new StringBuilder();
            for (String stream : audioStreams) {
                mixFilter.append(stream);
            }
            mixFilter.append("amix=inputs=").append(audioStreams.size())
                     .append(":duration=longest[a_final]");
            command.append(mixFilter);
            
            audioStreams.clear();
            audioStreams.add("[a_final]");
        } else if (audioStreams.size() == 1) {
            audioStreams.set(0, audioStreams.get(0).replace(";", ""));
        }
        
        // Close filter complex
        command.append("\" ");
        
        // Add map for final video and audio streams
        if (!videoStreams.isEmpty()) {
            command.append("-map ").append(videoStreams.get(0).replace("[", "").replace("]", "")).append(" ");
        }
        
        if (!audioStreams.isEmpty() && settings.isIncludeAudio()) {
            command.append("-map ").append(audioStreams.get(0).replace("[", "").replace("]", "")).append(" ");
        }
        
        // Add encoding settings
        command.append("-s ").append(getResolutionString(settings.getResolution())).append(" ");
        command.append("-b:v ").append(settings.getBitrate()).append("k ");
        command.append("-r ").append(settings.getFramerate()).append(" ");
        command.append("-c:v libx264 -preset medium -profile:v high ");
        
        if (settings.isIncludeAudio()) {
            command.append("-c:a aac -b:a 128k ");
        } else {
            command.append("-an ");
        }
        
        // Add output file
        command.append("-y ").append(settings.getFullOutputPath());
        
        return command.toString();
    }
    
    /**
     * Extract a single frame from a video file at the specified position
     * @param context Application context
     * @param videoUri URI of the video file
     * @param position Position in seconds
     * @param outputPath Output path for the extracted frame
     * @return True if successful, false otherwise
     */
    public static boolean extractFrame(Context context, Uri videoUri, double position, String outputPath) {
        String videoPath = UriUtils.getPathFromUri(context, videoUri);
        if (videoPath == null) {
            return false;
        }
        
        String command = String.format(Locale.US,
                "-ss %f -i %s -vframes 1 -q:v 2 -y %s",
                position, videoPath, outputPath);
        
        int rc = FFmpeg.execute(command);
        return rc == Config.RETURN_CODE_SUCCESS;
    }
    
    /**
     * Generate a video thumbnail
     * @param videoPath Path to the video file
     * @param outputPath Output path for the thumbnail
     * @return True if successful, false otherwise
     */
    public static boolean generateThumbnail(String videoPath, String outputPath) {
        String command = String.format(Locale.US,
                "-i %s -ss 0.5 -vframes 1 -s 320x240 -y %s",
                videoPath, outputPath);
        
        int rc = FFmpeg.execute(command);
        return rc == Config.RETURN_CODE_SUCCESS;
    }
    
    /**
     * Cut a video file
     * @param inputPath Input video path
     * @param outputPath Output video path
     * @param startTime Start time in seconds
     * @param endTime End time in seconds
     * @return True if successful, false otherwise
     */
    public static boolean cutVideo(String inputPath, String outputPath, double startTime, double endTime) {
        String command = String.format(Locale.US,
                "-i %s -ss %f -to %f -c copy -y %s",
                inputPath, startTime, endTime, outputPath);
        
        int rc = FFmpeg.execute(command);
        return rc == Config.RETURN_CODE_SUCCESS;
    }
    
    /**
     * Apply a filter to a video file
     * @param inputPath Input video path
     * @param outputPath Output video path
     * @param filter Filter to apply
     * @return True if successful, false otherwise
     */
    public static boolean applyFilter(String inputPath, String outputPath, Filter filter) {
        String filterString = filter.toFFmpegFilterString();
        
        String command = String.format(Locale.US,
                "-i %s -vf \"%s\" -c:a copy -y %s",
                inputPath, filterString, outputPath);
        
        int rc = FFmpeg.execute(command);
        return rc == Config.RETURN_CODE_SUCCESS;
    }
    
    /**
     * Add audio to a video file
     * @param videoPath Video file path
     * @param audioPath Audio file path
     * @param outputPath Output video path
     * @param volume Audio volume (0.0 - 1.0)
     * @return True if successful, false otherwise
     */
    public static boolean addAudioToVideo(String videoPath, String audioPath, String outputPath, float volume) {
        String command = String.format(Locale.US,
                "-i %s -i %s -filter_complex \"[1:a]volume=%f[a1];[0:a][a1]amix=inputs=2:duration=shortest\" -c:v copy -y %s",
                videoPath, audioPath, volume, outputPath);
        
        int rc = FFmpeg.execute(command);
        return rc == Config.RETURN_CODE_SUCCESS;
    }
    
    /**
     * Add text overlay to a video
     * @param inputPath Input video path
     * @param outputPath Output video path
     * @param text Text to overlay
     * @param fontName Font name
     * @param fontSize Font size
     * @param color Text color (ARGB format)
     * @param posX Position X (0.0 - 1.0)
     * @param posY Position Y (0.0 - 1.0)
     * @param startTime Start time in seconds
     * @param endTime End time in seconds
     * @return True if successful, false otherwise
     */
    public static boolean addTextOverlay(String inputPath, String outputPath, String text,
                                         String fontName, float fontSize, int color,
                                         float posX, float posY, double startTime, double endTime) {
        // Get video dimensions
        int[] dimensions = getVideoDimensions(inputPath);
        if (dimensions == null) {
            return false;
        }
        
        int width = dimensions[0];
        int height = dimensions[1];
        
        String hexColor = String.format("%08X", color).substring(2); // Remove alpha
        
        String command = String.format(Locale.US,
                "-i %s -vf \"drawtext=text='%s':fontfile=/system/fonts/%s.ttf:fontsize=%d:" +
                "fontcolor=0x%s:x=%d:y=%d:enable='between(t,%f,%f)'\" " +
                "-c:a copy -y %s",
                inputPath, text, fontName.toLowerCase(), (int)fontSize,
                hexColor, (int)(posX * width), (int)(posY * height),
                startTime, endTime, outputPath);
        
        int rc = FFmpeg.execute(command);
        return rc == Config.RETURN_CODE_SUCCESS;
    }
    
    /**
     * Get video dimensions (width, height)
     * @param videoPath Path to the video file
     * @return int array with [width, height] or null if failed
     */
    public static int[] getVideoDimensions(String videoPath) {
        try {
            String command = String.format(Locale.US,
                    "-i %s -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0",
                    videoPath);
            
            Config.enableLogCallback(message -> {
                if (message.getText().contains("x")) {
                    String dimensions = message.getText().trim();
                    String[] parts = dimensions.split("x");
                    if (parts.length == 2) {
                        try {
                            int width = Integer.parseInt(parts[0]);
                            int height = Integer.parseInt(parts[1]);
                            return new int[]{width, height};
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing dimensions", e);
                        }
                    }
                }
                return null;
            });
            
            FFmpeg.execute(command);
            
            // Disable log callback
            Config.enableLogCallback(null);
            
            // If we didn't get dimensions from the callback, try probe directly
            File file = new File(videoPath);
            if (file.exists()) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoPath);
                String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                retriever.release();
                
                if (width != null && height != null) {
                    return new int[]{Integer.parseInt(width), Integer.parseInt(height)};
                }
            }
            
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting video dimensions", e);
            return null;
        }
    }
    
    /**
     * Get a resolution string (e.g., "1920x1080") from a resolution value
     * @param resolution Resolution value (720, 1080, or 2160)
     * @return Resolution string
     */
    private static String getResolutionString(int resolution) {
        switch (resolution) {
            case 720:
                return "1280x720";
            case 1080:
                return "1920x1080";
            case 2160:
                return "3840x2160";
            default:
                return "1920x1080";
        }
    }

    // Helper inner class for URI to path conversion
    private static class UriUtils {
        public static String getPathFromUri(Context context, Uri uri) {
            try {
                // Handle file URI
                if ("file".equalsIgnoreCase(uri.getScheme())) {
                    return uri.getPath();
                }
                
                // Handle content URI
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(column_index);
                        cursor.close();
                        return path;
                    }
                    cursor.close();
                }
                
                // Fallback to direct string conversion
                return uri.toString();
            } catch (Exception e) {
                Log.e(TAG, "Error getting path from URI", e);
                return uri.toString();
            }
        }
    }
    
    // MediaMetadataRetriever helper class for FFmpegUtils
    private static class MediaMetadataRetriever {
        public static final int METADATA_KEY_VIDEO_WIDTH = 18;
        public static final int METADATA_KEY_VIDEO_HEIGHT = 19;
        
        private android.media.MediaMetadataRetriever retriever;
        
        public MediaMetadataRetriever() {
            retriever = new android.media.MediaMetadataRetriever();
        }
        
        public void setDataSource(String path) {
            retriever.setDataSource(path);
        }
        
        public String extractMetadata(int keyCode) {
            return retriever.extractMetadata(keyCode);
        }
        
        public void release() {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
