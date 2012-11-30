package br.com.oslunaticos.player;

/**
 *
 * @author Eduardo Folly
 */
public interface BeatMsg {

    public void colorFade(float r, float g, float b);

    public void playing();

    public void stoped();

    public void paused();
}
