package br.com.oslunaticos.serial;

/**
 *
 * @author Eduardo Folly
 */
public interface SerialCallback {
 
    // public void sendMsg(String msg);
    
    public void sendError(String name, Exception ex);
}
