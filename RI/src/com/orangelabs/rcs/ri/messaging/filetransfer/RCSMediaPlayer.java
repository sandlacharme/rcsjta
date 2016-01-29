package com.orangelabs.rcs.ri.messaging.filetransfer;

import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by sandrine on 12/01/2016.
 */
public class RCSMediaPlayer extends MediaPlayer {

    public Boolean getbReset() {
        return m_bReset;
    }

    public  Observer m_obs;


    Boolean m_bReset = false;


    public int getMin() {
        return m_min;
    }

    private int m_min;

    public int getSec() {
        return m_sec;
    }

    private int m_sec;


    public RCSMediaPlayer()
    {
        super();


    }

    public void setObserver(Observer o)
    {
        m_obs=o;
    }
    public void initPlayer(String outputfile)
    {
        try {
            //reset();
            m_bReset=false;
            setDataSource(outputfile);
            prepareAsync();


        }catch (Exception e){}
    }

    /**
     *
     * @param outputfile
     * @param m_bPlay : because this method is used even if there is no file palaying just fro getting duration for a record : bool true : play mode; false record mode
     */
    public void play (String outputfile, final boolean m_bPlay)
    {
        initPlayer(outputfile);
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                try {
                    if(m_bPlay) {
                        setVolume(1.0f, 1.0f);
                        mp.start();
                    }
                        int duration = getDuration();//ms
                        Log.v("audioDurationRecord", "onPrepared ");
                        if(duration>0) {
                            m_min = (int) ((duration / 1000) / 60) % 60;
                            m_sec = (int) ((duration /1000 ) % 60);
                            m_obs.NotifyDuration();
                        }
                    if(!m_bPlay) {
                        mp.reset();m_bReset=true;
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.v(this.toString(), "Illegal state exception thrown in start.");
                }
            }
        });

        setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.reset();
                m_bReset=true;
                Log.v("RCSMediaPlayer", "OnCompletionListener");
            }
        });


    }


    public void stopPlay() {
        try {
            if(!m_bReset) {
                stop();
                reset();
                Log.v("RCSMediaPlayer", "stopPlay");
            }
        } catch (RuntimeException e) {
            //mFile.delete();  //you must delete the outputfile when the recorder stop failed.
        } finally {


            //currentRecord.release();

        }


    }





}
