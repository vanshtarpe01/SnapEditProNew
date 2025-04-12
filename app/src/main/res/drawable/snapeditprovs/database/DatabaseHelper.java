package com.example.snapeditprovs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    // Database information
    private static final String DATABASE_NAME = "snapedit.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_VIDEO_CLIPS = "video_clips";
    public static final String TABLE_AUDIO_CLIPS = "audio_clips";
    public static final String TABLE_TEXT_OVERLAYS = "text_overlays";
    public static final String TABLE_STICKER_OVERLAYS = "sticker_overlays";
    public static final String TABLE_TRANSITIONS = "transitions";
    public static final String TABLE_FILTERS = "filters";
    
    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PROJECT_ID = "project_id";
    
    // Project table columns
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_LAST_MODIFIED = "last_modified";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_THUMBNAIL_PATH = "thumbnail_path";
    
    // Video clip table columns
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_TIMELINE_POSITION = "timeline_position";
    public static final String COLUMN_VOLUME = "volume";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_IS_MUTED = "is_muted";
    public static final String COLUMN_IS_REVERSED = "is_reversed";
    
    // Audio clip table columns (some shared with video clips)
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_IS_FADE_IN = "is_fade_in";
    public static final String COLUMN_IS_FADE_OUT = "is_fade_out";
    public static final String COLUMN_FADE_IN_DURATION = "fade_in_duration";
    public static final String COLUMN_FADE_OUT_DURATION = "fade_out_duration";
    
    // Text overlay table columns
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_FONT_NAME = "font_name";
    public static final String COLUMN_FONT_SIZE = "font_size";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_BACKGROUND_COLOR = "background_color";
    public static final String COLUMN_POSITION_X = "position_x";
    public static final String COLUMN_POSITION_Y = "position_y";
    public static final String COLUMN_ROTATION = "rotation";
    public static final String COLUMN_ANIMATION = "animation";
    public static final String COLUMN_IS_BOLD = "is_bold";
    public static final String COLUMN_IS_ITALIC = "is_italic";
    public static final String COLUMN_IS_UNDERLINE = "is_underline";
    public static final String COLUMN_ALIGNMENT = "alignment";
    
    // Sticker overlay table columns (some shared with text overlays)
    public static final String COLUMN_SCALE = "scale";
    
    // Filter table columns
    public static final String COLUMN_BRIGHTNESS = "brightness";
    public static final String COLUMN_CONTRAST = "contrast";
    public static final String COLUMN_SATURATION = "saturation";
    public static final String COLUMN_EXPOSURE = "exposure";
    public static final String COLUMN_TEMPERATURE = "temperature";
    public static final String COLUMN_TINT = "tint";
    public static final String COLUMN_VIBRANCE = "vibrance";
    public static final String COLUMN_HIGHLIGHTS = "highlights";
    public static final String COLUMN_SHADOWS = "shadows";
    public static final String COLUMN_LUT = "lut";
    
    // Transition table columns
    public static final String COLUMN_CLIP_START_ID = "clip_start_id";
    public static final String COLUMN_CLIP_END_ID = "clip_end_id";
    public static final String COLUMN_POSITION = "position";

    // Create table statements
    private static final String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
            COLUMN_LAST_MODIFIED + " INTEGER NOT NULL, " +
            COLUMN_WIDTH + " INTEGER, " +
            COLUMN_HEIGHT + " INTEGER, " +
            COLUMN_DURATION + " REAL, " +
            COLUMN_THUMBNAIL_PATH + " TEXT" +
            ");";
    
    private static final String CREATE_VIDEO_CLIPS_TABLE = "CREATE TABLE " + TABLE_VIDEO_CLIPS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
            COLUMN_PATH + " TEXT NOT NULL, " +
            COLUMN_START_TIME + " REAL, " +
            COLUMN_END_TIME + " REAL, " +
            COLUMN_DURATION + " REAL, " +
            COLUMN_TIMELINE_POSITION + " REAL, " +
            COLUMN_WIDTH + " INTEGER, " +
            COLUMN_HEIGHT + " INTEGER, " +
            COLUMN_VOLUME + " REAL, " +
            COLUMN_SPEED + " REAL, " +
            COLUMN_THUMBNAIL_PATH + " TEXT, " +
            COLUMN_IS_MUTED + " INTEGER, " +
            COLUMN_IS_REVERSED + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
            ");";
    
    private static final String CREATE_AUDIO_CLIPS_TABLE = "CREATE TABLE " + TABLE_AUDIO_CLIPS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
            COLUMN_PATH + " TEXT NOT NULL, " +
            COLUMN_START_TIME + " REAL, " +
            COLUMN_END_TIME + " REAL, " +
            COLUMN_DURATION + " REAL, " +
            COLUMN_TIMELINE_POSITION + " REAL, " +
            COLUMN_VOLUME + " REAL, " +
            COLUMN_IS_FADE_IN + " INTEGER, " +
            COLUMN_IS_FADE_OUT + " INTEGER, " +
            COLUMN_FADE_IN_DURATION + " REAL, " +
            COLUMN_FADE_OUT_DURATION + " REAL, " +
            COLUMN_TYPE + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
            ");";
    
    private static final String CREATE_TEXT_OVERLAYS_TABLE = "CREATE TABLE " + TABLE_TEXT_OVERLAYS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
            COLUMN_TEXT + " TEXT NOT NULL, " +
            COLUMN_FONT_NAME + " TEXT, " +
            COLUMN_FONT_SIZE + " REAL, " +
            COLUMN_COLOR + " INTEGER, " +
            COLUMN_BACKGROUND_COLOR + " INTEGER, " +
            COLUMN_POSITION_X + " REAL, " +
            COLUMN_POSITION_Y + " REAL, " +
            COLUMN_START_TIME + " REAL, " +
            COLUMN_END_TIME + " REAL, " +
            COLUMN_ROTATION + " REAL, " +
            COLUMN_ANIMATION + " TEXT, " +
            COLUMN_IS_BOLD + " INTEGER, " +
            COLUMN_IS_ITALIC + " INTEGER, " +
            COLUMN_IS_UNDERLINE + " INTEGER, " +
            COLUMN_ALIGNMENT + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
            ");";
    
    private static final String CREATE_STICKER_OVERLAYS_TABLE = "CREATE TABLE " + TABLE_STICKER_OVERLAYS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
            COLUMN_PATH + " TEXT NOT NULL, " +
            COLUMN_POSITION_X + " REAL, " +
            COLUMN_POSITION_Y + " REAL, " +
            COLUMN_SCALE + " REAL, " +
            COLUMN_ROTATION + " REAL, " +
            COLUMN_START_TIME + " REAL, " +
            COLUMN_END_TIME + " REAL, " +
            COLUMN_ANIMATION + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
            ");";
    
    private static final String CREATE_FILTERS_TABLE = "CREATE TABLE " + TABLE_FILTERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_TYPE + " TEXT, " +
            COLUMN_BRIGHTNESS + " REAL, " +
            COLUMN_CONTRAST + " REAL, " +
            COLUMN_SATURATION + " REAL, " +
            COLUMN_EXPOSURE + " REAL, " +
            COLUMN_TEMPERATURE + " REAL, " +
            COLUMN_TINT + " REAL, " +
            COLUMN_VIBRANCE + " REAL, " +
            COLUMN_HIGHLIGHTS + " REAL, " +
            COLUMN_SHADOWS + " REAL, " +
            COLUMN_THUMBNAIL_PATH + " TEXT, " +
            COLUMN_LUT + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
            ");";
    
    private static final String CREATE_TRANSITIONS_TABLE = "CREATE TABLE " + TABLE_TRANSITIONS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_TYPE + " TEXT, " +
            COLUMN_DURATION + " REAL, " +
            COLUMN_CLIP_START_ID + " INTEGER, " +
            COLUMN_CLIP_END_ID + " INTEGER, " +
            COLUMN_THUMBNAIL_PATH + " TEXT, " +
            COLUMN_POSITION + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROJECTS_TABLE);
        db.execSQL(CREATE_VIDEO_CLIPS_TABLE);
        db.execSQL(CREATE_AUDIO_CLIPS_TABLE);
        db.execSQL(CREATE_TEXT_OVERLAYS_TABLE);
        db.execSQL(CREATE_STICKER_OVERLAYS_TABLE);
        db.execSQL(CREATE_FILTERS_TABLE);
        db.execSQL(CREATE_TRANSITIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For future upgrades, implement appropriate schema changes
        if (oldVersion < 2) {
            // Example: Add a new column to projects table
            // db.execSQL("ALTER TABLE " + TABLE_PROJECTS + " ADD COLUMN new_column TEXT;");
        }
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
