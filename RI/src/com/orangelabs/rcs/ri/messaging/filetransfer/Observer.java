package com.orangelabs.rcs.ri.messaging.filetransfer;

import com.orangelabs.rcs.ri.messaging.filetransfer.multi.AudioMessageTransfer;

/**
 * Created by sandrine on 14/01/2016.
 */
public class Observer {


    AudioMessageTransfer sujetActivity;
    RCSMediaPlayer play;
    RCSRecorder recorder;

    public Observer(AudioMessageTransfer a, RCSMediaPlayer pl, RCSRecorder rec)
    {
        sujetActivity=a;
        recorder=rec;
        rec.setObserver(this);
        play=pl;
        pl.setObserver(this);

    }


    public void detach()
    {
        sujetActivity=null;
        recorder=null;
        play=null;
    }

    public void NotifyDuration(int min, int sec)
    {
        sujetActivity.setDuration(play.getMin(),play.getSec());
    }
    public void NotifyEndDuration()
    {
        sujetActivity.endRecordingDuration();
    }
}
