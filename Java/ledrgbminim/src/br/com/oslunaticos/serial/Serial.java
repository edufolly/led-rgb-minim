package br.com.oslunaticos.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Eduardo Folly
 *
 * Baseado na classe de comunicação serial do Processing.
 *
 * http://processing.org
 *
 */
public class Serial implements SerialPortEventListener, Runnable {

    private SerialCallback callback = null;
    //--
    private int useTimeOut;
    private int useStep = 100;
    private boolean inUse = false;
    //--
    private SerialPort port;
    private InputStream input;
    private OutputStream output;
    //--
    private final String name;
    private int rate;
    private int parity;
    private int databits;
    private int stopbits;
    //--
    private byte buffer[] = new byte[32768];
    private int bufferIndex;
    private int bufferLast;

//    /*
//     *
//     */
//    public static List<String> listPorts() {
//        List<String> portas = new ArrayList();
//        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
//        while (portList.hasMoreElements()) {
//            CommPortIdentifier portId =
//                    (CommPortIdentifier) portList.nextElement();
//
//            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
//                portas.add(portId.getName());
//            }
//        }
//
//        return portas;
//    }
    /**
     *
     * @param callback
     * @param name
     */
    public Serial(SerialCallback callback, String name) {
        this(callback, name, 9600);
    }

    /**
     *
     * @param callback
     * @param name
     * @param rate
     */
    public Serial(SerialCallback callback, String name, int rate) {
        this(callback, name, rate, SerialPort.PARITY_NONE);
    }

    /**
     *
     * @param callback
     * @param name
     * @param rate
     * @param parity
     */
    public Serial(SerialCallback callback, String name, int rate, int parity) {
        this(callback, name, rate, parity, SerialPort.DATABITS_8);
    }

    /**
     *
     * @param callback
     * @param name
     * @param rate
     * @param parity
     * @param databits
     */
    public Serial(SerialCallback callback, String name, int rate, int parity, int databits) {
        this(callback, name, rate, parity, databits, SerialPort.STOPBITS_1);
    }

    /**
     *
     * @param callback
     * @param name
     * @param rate
     * @param parity
     * @param databits
     * @param stopbits
     */
    public Serial(SerialCallback callback, String name, int rate, int parity, int databits, int stopbits) {
        this(callback, name, rate, parity, databits, stopbits, 2000);
    }

    /**
     *
     * @param callback
     * @param name
     * @param rate
     * @param parity
     * @param databits
     * @param stopbits
     * @param useTimeOut
     */
    public Serial(SerialCallback callback, String name, int rate, int parity, int databits, int stopbits, int useTimeOut) {
        this.callback = callback;
        this.name = name;
        this.rate = rate;
        this.parity = parity;
        this.databits = databits;
        this.stopbits = stopbits;
        this.useTimeOut = useTimeOut;
    }

    @Override
    public void run() {
        try {
            CommPortIdentifier cp = CommPortIdentifier.getPortIdentifier(name);

            port = (SerialPort) cp.open("Arduino", useTimeOut);
            input = port.getInputStream();
            output = port.getOutputStream();
            port.setSerialPortParams(rate, databits, stopbits, parity);
            port.addEventListener(this);
            port.notifyOnDataAvailable(true);
        } catch (Exception ex) {
            callback.sendError(name, ex);
            port = null;
            input = null;
            output = null;
        }
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     */
    public void stop() {
        dispose();
    }

    /**
     *
     */
    public void dispose() {
        try {
            if (input != null) {
                input.close();
            }

            if (output != null) {
                output.close();
            }
        } catch (Exception e) {
            callback.sendError(name, e);
        } finally {
            input = null;
            output = null;
        }

        try {
            if (port != null) {
                port.close();
            }
        } catch (Exception e) {
            callback.sendError(name, e);
        } finally {
            port = null;
        }
    }

    /**
     *
     * @param spe
     */
    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                while (input.available() > 0) {
                    if (bufferLast == buffer.length) {
                        byte temp[] = new byte[bufferLast << 1];
                        System.arraycopy(buffer, 0, temp, 0, bufferLast);
                        buffer = temp;
                    }
                    buffer[bufferLast++] = (byte) input.read();
                }
            } catch (IOException e) {
                callback.sendError(name, e);
            }
        }
    }

    /**
     *
     * @return
     */
    public int available() {
        return (bufferLast - bufferIndex);
    }

    /**
     *
     */
    public void clear() {
        bufferLast = 0;
        bufferIndex = 0;
    }

    /**
     *
     * @return
     */
    public int read() {
        if (bufferIndex == bufferLast) {
            return -1;
        }

        int outgoing = buffer[bufferIndex++] & 0xff;
        if (bufferIndex == bufferLast) {
            bufferIndex = 0;
            bufferLast = 0;
        }

        return outgoing;
    }

    // Mais rápido.
    /**
     *
     * @return
     */
    public int last() {
        if (bufferIndex == bufferLast) {
            return -1;
        }

        int outgoing = buffer[bufferLast - 1];
        bufferIndex = 0;
        bufferLast = 0;

        return outgoing;
    }

    /**
     *
     * @return
     */
    public char readChar() {
        if (bufferIndex == bufferLast) {
            return (char) (-1);
        }
        return (char) read();
    }

    /**
     *
     * @return
     */
    public char lastChar() {
        if (bufferIndex == bufferLast) {
            return (char) (-1);
        }
        return (char) last();
    }

    /**
     *
     * @return
     */
    public byte[] readBytes() {

        if (bufferIndex == bufferLast) {
            return null;
        }

        int length = bufferLast - bufferIndex;
        byte outgoing[] = new byte[length];
        System.arraycopy(buffer, bufferIndex, outgoing, 0, length);
        bufferIndex = 0;
        bufferLast = 0;

        return outgoing;
    }

    // Mais eficiente.
    /**
     *
     * @param outgoing
     * @return
     */
    public int readBytes(byte outgoing[]) {
        if (bufferIndex == bufferLast) {
            return 0;
        }

        int length = bufferLast - bufferIndex;
        if (length > outgoing.length) {
            length = outgoing.length;
        }
        System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

        bufferIndex += length;
        if (bufferIndex == bufferLast) {
            bufferIndex = 0;  // rewind
            bufferLast = 0;
        }

        return length;
    }

    /**
     *
     * @param interesting
     * @return
     */
    public byte[] readBytesUntil(int interesting) {
        if (bufferIndex == bufferLast) {
            return null;
        }

        byte what = (byte) interesting;

        int found = -1;
        for (int k = bufferIndex; k < bufferLast; k++) {
            if (buffer[k] == what) {
                found = k;
                break;
            }
        }

        if (found == -1) {
            return null;
        }

        int length = found - bufferIndex + 1;
        byte outgoing[] = new byte[length];
        System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

        bufferIndex += length;
        if (bufferIndex == bufferLast) {
            bufferIndex = 0;
            bufferLast = 0;
        }

        return outgoing;
    }

    // Mais eficiente.
    /**
     *
     * @param interesting
     * @param outgoing
     * @return
     */
    public int readBytesUntil(int interesting, byte outgoing[]) {
        if (bufferIndex == bufferLast) {
            return 0;
        }

        byte what = (byte) interesting;

        int found = -1;
        for (int k = bufferIndex; k < bufferLast; k++) {
            if (buffer[k] == what) {
                found = k;
                break;
            }
        }

        if (found == -1) {
            return 0;
        }

        int length = found - bufferIndex + 1;
        if (length > outgoing.length) {
//            String erro = "readBytesUntil() byte buffer is"
//                    + " too small for the " + length
//                    + " bytes up to and including char " + interesting;
//            serverlog.erro(0, erro);
            return -1;
        }

        System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

        bufferIndex += length;
        if (bufferIndex == bufferLast) {
            bufferIndex = 0;
            bufferLast = 0;
        }
        return length;
    }

    /**
     *
     * @return
     */
    public String readString() {
        if (bufferIndex == bufferLast) {
            return null;
        }

        return new String(readBytes());
    }

    // If you want to move Unicode data, you can first convert the
    // String to a byte stream in the representation of your choice
    // (i.e. UTF8 or two-byte Unicode data), and send it as a byte array.
    /**
     *
     * @param interesting
     * @return
     */
    public String readStringUntil(int interesting) {
        byte b[] = readBytesUntil(interesting);
        if (b == null) {
            return null;
        }

        return new String(b);
    }

    // Avançado.
    /**
     *
     * @param what
     */
    public void write(int what) {
        try {
            output.write(what & 0xff);
//            output.flush();
        } catch (Exception e) {
            callback.sendError(name, e);
        }
    }

    /**
     *
     * @param bytes
     */
    public void write(byte bytes[]) {
        try {
            output.write(bytes);
//            output.flush();
        } catch (Exception e) {
            callback.sendError(name, e);
        }
    }

    // Avançado.
    // Caracteres UTF-8, problemas com Unicode.
    /**
     *
     * @param what
     */
    public void write(String what) {
        int count = useTimeOut;
        while (inUse) {
            try {
                Thread.sleep(useStep);
            } catch (Exception ex) {
            }
            if (count < 0) {
                return;
            }
            count = count - useStep;
        }

        inUse = true;

        this.clear();
        this.write(what.getBytes());

        inUse = false;
    }

    @Override
    public String toString() {
        String s = "";
        s += "Name: " + port.getName() + "\n";
        s += "Rate: " + port.getBaudRate() + "\n";
        s += "Parity: " + port.getParity() + "\n";
        s += "Data bits: " + port.getDataBits() + "\n";
        s += "Stop bits: " + port.getStopBits();
        return s;
    }

    /**
     *
     * @param write
     * @param wait
     * @param until
     * @return
     */
    public String writeWaitReadUntil(String write, int wait, char until) {
        int count = useTimeOut;
        while (inUse) {
            try {
                Thread.sleep(useStep);
            } catch (Exception ex) {
            }
            if (count < 0) {
                return null;
            }
            count = count - useStep;
        }

        inUse = true;

        this.clear();
        this.write(write.getBytes());
        try {
            Thread.sleep(wait);
        } catch (Exception ex) {
        }
        String s = this.readStringUntil(until);

        inUse = false;

        return s;
    }
}
