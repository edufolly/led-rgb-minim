/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.oslunaticos.player;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import processing.core.PApplet;

/**
 *
 * @author Android
 */
public class Tocador {

    private PApplet p = new PApplet();
    private Minim minim = new Minim(p);
    private AudioPlayer song = null;
    private BeatMsg bm;
    private FFT fft;
    private boolean paused = true;
    private int resolution = 250;

    public Tocador(BeatMsg beatMsg) {
        this.bm = beatMsg;
    }

    public void abrir(String arquivo) {
        if (song != null) {
            stop();
        }
        song = minim.loadFile(arquivo);
        fft = new FFT(song.bufferSize(), song.sampleRate());
        play();
    }

    public void play() {
        if (song == null) {
            return;
        }
        paused = !paused;

        if (paused) {
            song.pause();
            bm.paused();
        } else {
            new Thread(new Player()).start();
        }
    }

    public void stop() {
        if (song != null) {
            song.close();
            bm.stoped();
            song = null;
            paused = true;
            try {
                Thread.sleep(100);
                bm.colorFade(0, 0, 0);
            } catch (Exception ex) {
            }
        }
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    private class Player implements Runnable {

        @Override
        public void run() {
            song.play();
            bm.playing();

            while (song != null && (!paused) && song.isPlaying()) {
                fft.forward(song.mix);

                float grave = fft.calcAvg(0, 200);
                float medio = fft.calcAvg(201, 3000);
                float agudo = fft.calcAvg(3001, 20000);

                bm.colorFade(grave, medio, agudo);
                try {
                    Thread.sleep(resolution);
                } catch (Exception ex) {
                }
            }

            if (!paused) {
                stop();
            }
        }
    }
}
