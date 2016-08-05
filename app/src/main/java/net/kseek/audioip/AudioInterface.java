package net.kseek.audioip;

import java.nio.ByteBuffer;

/**
 * Created by teocci on 8/4/16.
 */
public interface AudioInterface
{
    public void onAudioRecord(ByteBuffer buffer, int bufsize);
}
