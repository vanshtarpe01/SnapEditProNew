package com.example.snapeditprovs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TensorFlowUtils {
    private static final String TAG = "TensorFlowUtils";
    
    // Model configuration for face detection
    private static final String FACE_DETECTION_MODEL = "face_detection.tflite";
    private static final int FACE_MODEL_INPUT_SIZE = 320;
    private static final float FACE_DETECTION_THRESHOLD = 0.6f;
    
    // Model configuration for segmentation
    private static final String SEGMENTATION_MODEL = "segmentation.tflite";
    private static final int SEGMENTATION_INPUT_SIZE = 257;
    
    /**
     * Result class for face detection
     */
    public static class Face {
        public RectF boundingBox;
        public float confidence;
        
        public Face(RectF boundingBox, float confidence) {
            this.boundingBox = boundingBox;
            this.confidence = confidence;
        }
    }
    
    /**
     * Helper to initialize TFLite interpreter
     * @param context Application context
     * @param modelFilename Model filename in assets
     * @return Interpreter instance or null if initialization failed
     */
    private static Interpreter initializeInterpreter(Context context, String modelFilename) {
        try {
            // Copy model from assets to cache for runtime access
            File modelFile = new File(context.getCacheDir(), modelFilename);
            if (!modelFile.exists()) {
                copyAssetToFile(context, modelFilename, modelFile);
            }
            
            // Load model
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, modelFile.getAbsolutePath());
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            return new Interpreter(modelBuffer, options);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing TFLite interpreter", e);
            return null;
        }
    }
    
    /**
     * Copy asset file to app's cache directory
     * @param context Application context
     * @param assetName Asset filename
     * @param outputFile Output file
     * @throws IOException If copying fails
     */
    private static void copyAssetToFile(Context context, String assetName, File outputFile) throws IOException {
        try (InputStream is = context.getAssets().open(assetName);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     * Detect faces in an image
     * @param context Application context
     * @param bitmap Input image
     * @return List of detected faces with bounding boxes
     */
    public static List<Face> detectFaces(Context context, Bitmap bitmap) {
        List<Face> results = new ArrayList<>();
        Interpreter interpreter = initializeInterpreter(context, FACE_DETECTION_MODEL);
        
        if (interpreter == null) {
            return results;
        }
        
        try {
            // Prepare input image
            int inputSize = FACE_MODEL_INPUT_SIZE;
            TensorImage inputImage = new TensorImage(DataType.FLOAT32);
            
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int cropSize = Math.min(width, height);
            
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                    .add(new ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(127.5f, 127.5f))
                    .build();
            
            inputImage.load(bitmap);
            inputImage = imageProcessor.process(inputImage);
            
            // Output arrays
            float[][][] outputBoxes = new float[1][10][4]; // 10 boxes max, 4 values per box
            float[][] outputScores = new float[1][10]; // Confidence scores
            float[][] outputClasses = new float[1][10]; // Class IDs
            float[] numDetections = new float[1]; // Number of detections
            
            Object[] outputMap = {outputBoxes, outputScores, outputClasses, numDetections};
            
            // Run inference
            interpreter.runForMultipleInputsOutputs(new Object[]{inputImage.getBuffer()}, outputMap);
            
            // Process results
            int numDetected = (int) numDetections[0];
            for (int i = 0; i < numDetected; i++) {
                float confidence = outputScores[0][i];
                if (confidence >= FACE_DETECTION_THRESHOLD) {
                    float ymin = outputBoxes[0][i][0] * height;
                    float xmin = outputBoxes[0][i][1] * width;
                    float ymax = outputBoxes[0][i][2] * height;
                    float xmax = outputBoxes[0][i][3] * width;
                    
                    RectF boundingBox = new RectF(xmin, ymin, xmax, ymax);
                    results.add(new Face(boundingBox, confidence));
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error running face detection model", e);
        } finally {
            interpreter.close();
        }
        
        return results;
    }
    
    /**
     * Perform image segmentation to separate foreground from background
     * @param context Application context
     * @param bitmap Input image
     * @return Bitmap with alpha channel set based on segmentation (null pixels are background)
     */
    public static Bitmap segmentForeground(Context context, Bitmap bitmap) {
        Interpreter interpreter = initializeInterpreter(context, SEGMENTATION_MODEL);
        
        if (interpreter == null) {
            return bitmap;
        }
        
        try {
            // Prepare input image
            int inputSize = SEGMENTATION_INPUT_SIZE;
            TensorImage inputImage = new TensorImage(DataType.FLOAT32);
            
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(127.5f, 127.5f))
                    .build();
            
            inputImage.load(bitmap);
            inputImage = imageProcessor.process(inputImage);
            
            // Allocate output buffer (1 is background, 0 is foreground)
            ByteBuffer segmentationMask = ByteBuffer.allocateDirect(inputSize * inputSize * 4)
                    .order(ByteOrder.nativeOrder());
            
            // Run inference
            interpreter.run(inputImage.getBuffer(), segmentationMask);
            
            // Process the mask to create a new bitmap with alpha channel
            segmentationMask.rewind();
            Bitmap outputBitmap = Bitmap.createBitmap(
                    bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            
            // Scale the segmentation mask to match the original image dimensions
            int origWidth = bitmap.getWidth();
            int origHeight = bitmap.getHeight();
            
            // Create array for full-sized mask
            float[][] fullMask = new float[origHeight][origWidth];
            
            // Read the mask values and scale
            for (int y = 0; y < inputSize; y++) {
                for (int x = 0; x < inputSize; x++) {
                    float maskValue = segmentationMask.getFloat();
                    
                    // Map from inputSize to original dimensions
                    int origX = (int) (x * ((float) origWidth / inputSize));
                    int origY = (int) (y * ((float) origHeight / inputSize));
                    
                    if (origX < origWidth && origY < origHeight) {
                        fullMask[origY][origX] = maskValue > 0.7f ? 0 : 255; // Threshold
                    }
                }
            }
            
            // Copy the original pixels but modify alpha based on mask
            int[] pixels = new int[origWidth * origHeight];
            bitmap.getPixels(pixels, 0, origWidth, 0, 0, origWidth, origHeight);
            
            for (int y = 0; y < origHeight; y++) {
                for (int x = 0; x < origWidth; x++) {
                    int index = y * origWidth + x;
                    int pixelColor = pixels[index];
                    
                    // Keep RGB but modify alpha based on mask
                    int alpha = (int) fullMask[y][x];
                    int red = (pixelColor >> 16) & 0xFF;
                    int green = (pixelColor >> 8) & 0xFF;
                    int blue = pixelColor & 0xFF;
                    
                    pixels[index] = (alpha << 24) | (red << 16) | (green << 8) | blue;
                }
            }
            
            outputBitmap.setPixels(pixels, 0, origWidth, 0, 0, origWidth, origHeight);
            return outputBitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Error running segmentation model", e);
            return bitmap;
        } finally {
            interpreter.close();
        }
    }
    
    /**
     * Extract timestamps of beats in an audio file for beat syncing
     * @param context Application context
     * @param audioPath Path to the audio file
     * @return Array of beat timestamps in seconds
     */
    public static float[] detectBeats(Context context, String audioPath) {
        // This is a complex operation that would typically use a specialized library
        // For a production app, you'd use something like Essentia or a native library
        // This is a simplified placeholder implementation
        
        try {
            // Get audio duration to fake some beats
            android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
            retriever.setDataSource(audioPath);
            String durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            double durationSecs = Integer.parseInt(durationStr) / 1000.0;
            retriever.release();
            
            // Assume beat detection at 120 BPM (2 beats per second)
            int beatsCount = (int) (durationSecs * 2);
            float[] beats = new float[beatsCount];
            
            for (int i = 0; i < beatsCount; i++) {
                beats[i] = i * 0.5f; // 0.5 seconds between beats (120 BPM)
            }
            
            return beats;
        } catch (Exception e) {
            Log.e(TAG, "Error detecting beats", e);
            return new float[0];
        }
    }
}
