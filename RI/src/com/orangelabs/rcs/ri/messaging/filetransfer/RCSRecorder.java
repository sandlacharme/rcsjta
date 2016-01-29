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
        return m_outputfile;
    }



    private String m_outputfile = null;
    MediaRecorder m_currentRecord = null;


    public RCSRecorder(long m_lmax_audio_duration)
    {
        MAX_AUDIO_DURATION=m_lmax_audio_duration;
        m_currentRecord=new MediaRecorder();
       // m_currentRecord.reset();

    }

    public void setObserver(Observer o)
    {
        m_obs=o;
    }

    public void init()
    {

        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //m_currentRecord.setAudioEncoder(MediaRecorder.getAudioSourceMax());
        try
        {
            m_currentRecord.setAudioSource(AudioSource.VOICE_COMMUNICATION);
            m_currentRecord.setOutputFormat(OutputFormat.DEFAULT);
            m_currentRecord.setAudioEncoder(AudioEncoder.DEFAULT);
        }
        catch(IllegalStateException e){
            e.printStackTrace();

    }




        randomFilename();
        m_currentRecord.setMaxDuration((int)MAX_AUDIO_DURATION); // 10 seconds
        m_currentRecord.setOnInfoListener(new OnInfoListener() {
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
            m_currentRecord.prepare();
            m_currentRecord.start();

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
            m_currentRecord.stop();
            m_currentRecord.reset();
        } catch (RuntimeException e) {

        }

    }


    /**
     * Generate a random Filename for audiofiles
     */
    private void randomFilename() {

        int lower = 1;
        int higher = 10000;
        int random = (int) (Math.random() * (higher - lower)) + lower;
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecords");
        if (!f.exists())
        {
            f.mkdir();

        }
        m_outputfile= "/sdcard/audiorecords/"+"recording"+random+".3gp";
        Log.w(" File path and name ", m_outputfile);
    }

    public final long getMaxAudioDuration()
    {
        return this.MAX_AUDIO_DURATION;


    }



}
