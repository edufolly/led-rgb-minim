package br.com.oslunaticos.player;

import br.com.oslunaticos.serial.LinkSerial;
import br.com.oslunaticos.serial.Serial;

/**
 *
 * @author Eduardo Folly
 */
public class SerialColor {

    private Serial serial;

    public SerialColor() {
        serial = LinkSerial.getInstance().getSerial();
    }

    public void sendColor(int r, int g, int b) {
        int t = (r * 65536) + (g * 256) + b;
        sendColor(t);
    }

    public void sendColor(int t) {
        serial.write(("^" + t + "$").getBytes());
    }
}
