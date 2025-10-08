package com.ev.evstationoperatorapp;

import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * This is a custom capture activity that is used to lock the screen in portrait mode.
 * We register this activity in the AndroidManifest.xml and set its screenOrientation.
 */
public class CaptureActivityPortrait extends CaptureActivity {
    // This class remains empty. Its sole purpose is to be a target
    // for the orientation lock in the AndroidManifest.xml file.
}
