package com.orangelabs.rcs.ri.messaging.filetransfer;

import com.orangelabs.rcs.ri.messaging.filetransfer.multi.AudioMessageTransfer;

/**
 * Created by sandrine on 14/01/2016.
 */
public class Observer {


    AudioMessageTransfer sujetActivity;
    RCSMediaPlayer m_play;
    RCSRecorder m_recorder;

    public Observer(AudioMessageTransfer a, RCSMediaPlayer pl, RCSRecorder rec)
    {
        sujetActivity=a;
        m_recorder=rec;
        rec.setObserver(this);
        m_play=pl;
        pl.setObserver(this);

    }


    public void detach()
    {
        sujetActivity=null;
        m_recorder=null;
        m_play=null;
    }

    public void NotifyDuration()
    {
        sujetActivity.setDuration(m_play.getMin(),m_play.getSec());
    }
    public void NotifyEndDuration()
    {
        sujetActivity.endRecordingDuration();
    }
}
