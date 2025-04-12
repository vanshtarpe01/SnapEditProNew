package com.example.snapeditprovs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class VideoUtils {
    private static final String TAG = "VideoUtils";
    
    /**
     * Video metadata class to hold width, height, and duration
     */
    public static class VideoMetadata {
        public int width;
        public int height;
        public double duration;
        
        public VideoMetadata(int width, int height, double duration) {
            this.width = width;
            this.height = height;
            this.duration = duration;
        }
    }
    
    /**
     * Get metadata of a video file
     * @param context Application context
     * @param videoUri URI of the video file
     * @return VideoMetadata object containing width, height, and duration
     */
    public static VideoMetadata getVideoMetadata(Context context, Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            
            String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            
            int width = Integer.parseInt(widthStr);
            int height = Integer.parseInt(heightStr);
            double duration = Integer.parseInt(durationStr) / 1000.0; // Convert ms to seconds
            
            return new VideoMetadata(width, height, duration);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting video metadata", e);
            // Return default values if there's an error
            return new VideoMetadata(1920, 1080, 0);
        } finally {
            retriever.release();
        }
    }
    
    /**
     * Extract a thumbnail from a video at a specific position
     * @param context Application context
     * @param videoUri URI of the video file
     * @param timeMs Position in milliseconds
     * @return Bitmap thumbnail or null if extraction failed
     */
    public static Bitmap extractThumbnailAt(Context context, Uri videoUri, long timeMs) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            return retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting video thumbnail", e);
            return null;
        } finally {
            retriever.release();
        }
    }
    
    /**
     * Extract a thumbnail from a video (default frame)
     * @param context Application context
     * @param videoUri URI of the video file
     * @return Bitmap thumbnail or null if extraction failed
     */
    public static Bitmap extractVideoThumbnail(Context context, Uri videoUri) {
        return extractThumbnailAt(context, videoUri, 0);
    }
    
    /**
     * Save a bitmap to file
     * @param bitmap Bitmap to save
     * @param outputPath Output file path
     * @return True if successful, false otherwise
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, String outputPath) {
        File outputFile = new File(outputPath);
        try {
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            
            FileOutputStream out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file", e);
            return false;
        }
    }
    
    /**
     * Generate thumbnails at regular intervals for a video
     * @param context Application context
     * @param videoUri URI of the video file
     * @param count Number of thumbnails to generate
     * @param outputDir Output directory for thumbnails
     * @param baseName Base name for thumbnail files
     * @return Array of thumbnail file paths, or null if generation failed
     */
    public static String[] generateThumbnailGrid(Context context, Uri videoUri, int count, 
                                                File outputDir, String baseName) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        String[] thumbnailPaths = new String[count];
        VideoMetadata metadata = getVideoMetadata(context, videoUri);
        
        // Calculate interval between thumbnails
        double interval = metadata.duration / count;
        
        for (int i = 0; i < count; i++) {
            // Get thumbnail at position
            double position = i * interval;
            Bitmap thumbnail = extractThumbnailAt(context, videoUri, (long)(position * 1000));
            
            if (thumbnail != null) {
                String outputPath = new File(outputDir, baseName + "_" + i + ".jpg").getAbsolutePath();
                if (saveBitmapToFile(thumbnail, outputPath)) {
                    thumbnailPaths[i] = outputPath;
                }
                thumbnail.recycle();
            }
        }
        
        return thumbnailPaths;
    }
    
    /**
     * Calculate optimal output size for video export
     * @param originalWidth Original video width
     * @param originalHeight Original video height
     * @param targetResolution Target resolution (e.g., 1080)
     * @return Size object with width and height
     */
    public static Size calculateOutputSize(int originalWidth, int originalHeight, int targetResolution) {
        if (originalWidth >= originalHeight) {
            // Landscape or square
            int calculatedHeight;
            if (targetResolution == 720) {
                calculatedHeight = 720;
            } else if (targetResolution == 1080) {
                calculatedHeight = 1080;
            } else if (targetResolution == 2160) {
                calculatedHeight = 2160;
            } else {
                calculatedHeight = 1080; // Default
            }
            
            // Calculate width while maintaining aspect ratio
            int calculatedWidth = calculatedHeight * originalWidth / originalHeight;
            // Ensure width is even (required for some encoders)
            calculatedWidth = calculatedWidth + (calculatedWidth % 2);
            
            return new Size(calculatedWidth, calculatedHeight);
        } else {
            // Portrait
            int calculatedWidth;
            if (targetResolution == 720) {
                calculatedWidth = 720;
            } else if (targetResolution == 1080) {
                calculatedWidth = 1080;
            } else if (targetResolution == 2160) {
                calculatedWidth = 2160;
            } else {
                calculatedWidth = 1080; // Default
            }
            
            // Calculate height while maintaining aspect ratio
            int calculatedHeight = calculatedWidth * originalHeight / originalWidth;
            // Ensure height is even (required for some encoders)
            calculatedHeight = calculatedHeight + (calculatedHeight % 2);
            
            return new Size(calculatedWidth, calculatedHeight);
        }
    }
    
    /**
     * Check if device supports hardware acceleration for video encoding
     * @return True if hardware acceleration is supported
     */
    public static boolean isHardwareAccelerationSupported() {
        try {
            // Check for specific hardware acceleration support
            android.media.MediaCodecList codecList = new android.media.MediaCodecList(android.media.MediaCodecList.REGULAR_CODECS);
            for (android.media.MediaCodecInfo codecInfo : codecList.getCodecInfos()) {
                if (codecInfo.isEncoder() && codecInfo.getName().startsWith("OMX.qcom.")
                        || codecInfo.getName().startsWith("OMX.Exynos.")
                        || codecInfo.getName().startsWith("OMX.Intel.")) {
                    String[] types = codecInfo.getSupportedTypes();
                    for (String type : types) {
                        if (type.equalsIgnoreCase("video/avc") || type.equalsIgnoreCase("video/hevc")) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking hardware acceleration support", e);
        }
        
        return false;
    }
}
