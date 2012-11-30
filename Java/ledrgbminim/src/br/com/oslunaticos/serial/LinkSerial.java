package br.com.oslunaticos.serial;

/**
 *
 * @author Eduardo Folly
 */
public class LinkSerial implements SerialCallback {

    private Serial serial;

    @Override
    public void sendError(String name, Exception ex) {
        ex.printStackTrace();
    }

    private static class LinkSerialHolder {

        public static final LinkSerial INSTANCE = new LinkSerial();
    }

    /**
     *
     * @return
     */
    public static LinkSerial getInstance() {
        return LinkSerialHolder.INSTANCE;
    }

    private LinkSerial() {
        String dev = "COM6";
        int rate = 115200;
        int parity = 0;
        int databits = 8;
        int stopbits = 1;
        int timeOut = 2000;

        serial = new Serial(this, dev, rate, parity, databits, stopbits, timeOut);
        Thread t = new Thread(serial);
        int id = t.hashCode();
        t.start();

    }

    public Serial getSerial() {
        return serial;
    }
}
