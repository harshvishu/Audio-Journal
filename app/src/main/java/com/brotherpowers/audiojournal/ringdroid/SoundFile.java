package com.brotherpowers.audiojournal.ringdroid;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Created by harsh_v on 11/6/16.
 */

public class SoundFile {
    private ProgressListener mProgressListener = null;
    private File mInputFile = null;

    // Member variables representing frame data
    private int mFileSize;
    private int mAvgBitRate; // Average bit rate in kbps.
    private int mSampleRate;
    private int mChannels;
    private int mNumSamples;  // total number of samples per channel in audio file
    private ByteBuffer mDecodedBytes;  // Raw audio data
    private ShortBuffer mDecodedSamples;  // shared buffer with mDecodedBytes.


    // Progress listener interface.
    public interface ProgressListener {
        /**
         * Will be called by the SoundFile class periodically
         * with values between 0.0 and 1.0.  Return true to continue
         * loading the file or recording the audio, and false to cancel or stop recording.
         */
        boolean reportProgress(double fractionComplete);
    }
}
