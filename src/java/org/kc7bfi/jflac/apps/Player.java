package org.kc7bfi.jflac.apps;

/* libFLAC - Free Lossless Audio Codec library
 * Copyright (C) 2000,2001,2002,2003  Josh Coalson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.StreamDecoder;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteSpace;


/**
 * Play a FLAC file application.
 * @author kc7bfi
 */
public class Player implements PCMProcessor {
    private AudioFormat fmt;
    private DataLine.Info info;
    private SourceDataLine line;
    private boolean prefill = true;
    
    /**
     * Decode and play an input FLAC file.
     * @param inFileName    The input FLAC file name
     * @throws IOException  Thrown if error reading file
     * @throws LineUnavailableException Thrown if error playing file
     */
    public void decode(String inFileName) throws IOException, LineUnavailableException {
        System.out.println("Decode [" + inFileName + "]");
        FileInputStream is = new FileInputStream(inFileName);
        
        StreamDecoder decoder = new StreamDecoder(is);
        decoder.addPCMProcessor(this);
        decoder.decode();
        
        line.close();
    }
    
    /**
     * Process the StreamInfo block.
     * @param info the StreamInfo block
     * @see org.kc7bfi.jflac.PCMProcessor#processStreamInfo(org.kc7bfi.jflac.metadata.StreamInfo)
     */
    public void processStreamInfo(StreamInfo streamInfo) {
        try {
            fmt = new AudioFormat(streamInfo.sampleRate,
                    streamInfo.bitsPerSample,
                    streamInfo.channels,
                    true,
                    false);
            info = new DataLine.Info(SourceDataLine.class, fmt, AudioSystem.NOT_SPECIFIED);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt, AudioSystem.NOT_SPECIFIED);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Process the decoded PCM bytes.
     * @param pcm The decoded PCM data
     * @see org.kc7bfi.jflac.PCMProcessor#processPCM(org.kc7bfi.jflac.util.ByteSpace)
     */
    public void processPCM(ByteSpace pcm) {
        line.write(pcm.space, 0, pcm.pos);
        
        if (prefill) {
            line.start();	
            prefill = false;
        }
    }
    
    /**
     * The main routine.
     * <p>args[0] is the input file name
     * @param args  Command line arguments
     */
    public static void main(String[] args) {
        try {
            Player decoder = new Player();
            decoder.decode(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}