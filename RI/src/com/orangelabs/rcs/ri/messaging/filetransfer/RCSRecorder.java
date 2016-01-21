package com.orangelabs.rcs.ri.messaging.filetransfer;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class RCSRecorder extends MediaRecorder {

    //
    private final long MAX_AUDIO_DURATION;//6sec
    public  Observer m_obs;
    public String getOutputFile() {
        return outputFile;
    }

    @Override
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    private String outputFile = null;
     MediaRecorder currentRecord = null;


    public RCSRecorder(long m_lmax_audio_duration)
    {
        MAX_AUDIO_DURATION=m_lmax_audio_duration;
        currentRecord=new MediaRecorder();
       // currentRecord.reset();

    }

    public void setObserver(Observer o)
    {
        m_obs=o;
    }

    public void init()
    {

        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //currentRecord.setAudioEncoder(MediaRecorder.getAudioSourceMax());
        currentRecord.setAudioSource(AudioSource.VOICE_COMMUNICATION);
        currentRecord.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        currentRecord.setAudioEncoder(AudioEncoder.DEFAULT);
        randomFilename();
        currentRecord.setOutputFile(outputFile);
        currentRecord.setMaxDuration((int)MAX_AUDIO_DURATION); // 10 seconds
        currentRecord.setOnInfoListener(new OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.v("VIDEOCAPTURE", "Maximum Duration Reached");
                    m_obs.NotifyEndDuration();
                    mr.stop();
                    mr.reset();

                }
            }
        });
    }

    public void launchRecord ()
    {
        try {

            init();
            currentRecord.prepare();
            currentRecord.start();

        }

        catch (IllegalStateException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        }

        catch (IOException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void stopRecord() {
        try {
            currentRecord.stop();
            currentRecord.reset();
        } catch (RuntimeException e) {

        }

    }


    /**
     * Generate a random Filename for audiofiles
     */
    public void randomFilename() {

        int lower = 1;
        int higher = 10000;
        int random = (int) (Math.random() * (higher - lower)) + lower;
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecords");
        if (!f.exists())
        {
            f.mkdir();

        }
        outputFile= Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecords/recording"+random+".3gp";
        Log.w(" File path and name ", outputFile);
    }

    public final long getMaxAudioDuration()
    {
        return this.MAX_AUDIO_DURATION;


    }



}
