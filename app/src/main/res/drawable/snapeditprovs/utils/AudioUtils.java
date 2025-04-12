package com.example.snapeditprovs.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioUtils {
    private static final String TAG = "AudioUtils";
    
    /**
     * Audio metadata class to hold duration, bitrate, etc.
     */
    public static class AudioMetadata {
        public double duration;
        public int bitrate;
        public String mimeType;
        
        public AudioMetadata(double duration, int bitrate, String mimeType) {
            this.duration = duration;
            this.bitrate = bitrate;
            this.mimeType = mimeType;
        }
    }
    
    /**
     * Get metadata of an audio file
     * @param context Application context
     * @param audioUri URI of the audio file
     * @return AudioMetadata object containing duration, bitrate, and MIME type
     */
    public static AudioMetadata getAudioMetadata(Context context, Uri audioUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, audioUri);
            
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            
            double duration = Integer.parseInt(durationStr) / 1000.0; // Convert ms to seconds
            int bitrate = bitrateStr != null ? Integer.parseInt(bitrateStr) : 0;
            
            return new AudioMetadata(duration, bitrate, mimeType);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting audio metadata", e);
            // Return default values if there's an error
            return new AudioMetadata(0, 0, "audio/mp3");
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Copy audio file from Uri to app's internal storage
     * @param context Application context
     * @param audioUri URI of the audio file
     * @param fileName Destination file name
     * @return Path to the copied file, or null if copy failed
     */
    public static String copyAudioFile(Context context, Uri audioUri, String fileName) {
        File outputDir = new File(context.getFilesDir(), "audio");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        File outputFile = new File(outputDir, fileName);
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(audioUri);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            
            if (inputStream == null) {
                return null;
            }
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error copying audio file", e);
            return null;
        }
    }
    
    /**
     * Convert waveform data to a drawable representation
     * @param waveform Array of waveform amplitudes (normalized to 0-1)
     * @param width Width of the output waveform
     * @param height Height of the output waveform
     * @return int array representing pixel data for the waveform
     */
    public static int[] createWaveformImage(float[] waveform, int width, int height) {
        int[] pixels = new int[width * height];
        
        // Fill with transparent background
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0x00000000;
        }
        
        if (waveform == null || waveform.length == 0) {
            return pixels;
        }
        
        // Draw waveform
        int centerY = height / 2;
        int waveformLength = Math.min(width, waveform.length);
        
        for (int x = 0; x < waveformLength; x++) {
            float amplitude = waveform[x];
            int waveformHeight = (int) (amplitude * height / 2);
            
            // Draw vertical line for this sample
            for (int y = centerY - waveformHeight; y <= centerY + waveformHeight; y++) {
                if (y >= 0 && y < height) {
                    pixels[y * width + x] = 0xFF2196F3; // Material blue color
                }
            }
        }
        
        return pixels;
    }
    
    /**
     * Extract waveform data from an audio file
     * @param context Application context
     * @param audioPath Path to the audio file
     * @param sampleCount Number of samples to extract
     * @return Array of normalized amplitude values (0-1) or null if extraction failed
     */
    public static float[] extractWaveform(Context context, String audioPath, int sampleCount) {
        // This is a complex operation that would typically use native code or a specialized library
        // For a production app, you'd use something like SoundFile from Ringdroid or a native library
        // This is a simplified placeholder implementation
        
        try {
            // Create dummy waveform data for the UI
            float[] waveform = new float[sampleCount];
            
            // Get audio duration to scale the waveform properly
            AudioMetadata metadata = getAudioMetadata(context, Uri.fromFile(new File(audioPath)));
            
            for (int i = 0; i < sampleCount; i++) {
                // Generate semi-random waveform for visualization
                // In a real implementation, you'd extract actual amplitude data
                double position = i / (double) sampleCount;
                waveform[i] = (float) (0.2f + 0.8f * Math.sin(position * 20) * Math.sin(position * 10));
            }
            
            return waveform;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting waveform", e);
            return null;
        }
    }
    
    /**
     * Apply audio effects like fade in/out using FFmpeg
     * @param inputPath Input audio path
     * @param outputPath Output audio path
     * @param fadeInDuration Fade in duration in seconds (0 for no fade in)
     * @param fadeOutDuration Fade out duration in seconds (0 for no fade out)
     * @param volume Volume adjustment (1.0 = normal)
     * @return True if the operation was successful
     */
    public static boolean applyAudioEffects(String inputPath, String outputPath, 
                                            float fadeInDuration, float fadeOutDuration, float volume) {
        StringBuilder command = new StringBuilder();
        command.append("-i ").append(inputPath).append(" ");
        
        // Build filter string
        StringBuilder filterBuilder = new StringBuilder();
        boolean needsFilter = false;
        
        if (fadeInDuration > 0 || fadeOutDuration > 0) {
            needsFilter = true;
            filterBuilder.append("afade=");
            
            if (fadeInDuration > 0) {
                filterBuilder.append("t=in:st=0:d=").append(fadeInDuration);
            }
            
            if (fadeInDuration > 0 && fadeOutDuration > 0) {
                filterBuilder.append(",afade=");
            }
            
            if (fadeOutDuration > 0) {
                // Get duration to calculate fade out start time
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(inputPath);
                    String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    double durationSecs = Integer.parseInt(durationStr) / 1000.0;
                    double fadeOutStart = durationSecs - fadeOutDuration;
                    
                    filterBuilder.append("t=out:st=").append(fadeOutStart).append(":d=").append(fadeOutDuration);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting audio duration", e);
                } finally {
                    try {
                        retriever.release();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        
        if (volume != 1.0f) {
            if (needsFilter) {
                filterBuilder.append(",");
            }
            needsFilter = true;
            filterBuilder.append("volume=").append(volume);
        }
        
        if (needsFilter) {
            command.append("-filter:a \"").append(filterBuilder).append("\" ");
        }
        
        command.append("-y ").append(outputPath);
        
        // Execute the command
        return FFmpegUtils.executeCommand(command.toString());
    }
    
    /**
     * Extract audio from a video file
     * @param videoPath Path to the video file
     * @param outputPath Output audio file path
     * @return True if extraction was successful
     */
    public static boolean extractAudioFromVideo(String videoPath, String outputPath) {
        String command = String.format("-i %s -vn -acodec copy -y %s", videoPath, outputPath);
        return FFmpegUtils.executeCommand(command);
    }
    
    /**
     * Static helper class to execute FFmpeg commands
     */
    private static class FFmpegUtils {
        public static boolean executeCommand(String command) {
            try {
                int rc = com.arthenica.mobileffmpeg.FFmpeg.execute(command);
                return rc == com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
            } catch (Exception e) {
                Log.e(TAG, "Error executing FFmpeg command", e);
                return false;
            }
        }
    }
}
