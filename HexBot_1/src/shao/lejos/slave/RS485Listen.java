package shao.lejos.slave;

import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.LCPResponder;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.RS485;
import lejos.nxt.comm.USB;
import lejos.util.TextMenu;

/**
 * Create an LCP responder to handle LCP requests. Allow the
 * User to choose between Bluetooth, USB and RS485 protocols.
 * 
 * @author Andy Shaw
 *
 */
public class RS485Listen
{
    /**
     * Our local Responder class so that we can over-ride the standard
     * behaviour. We modify the disconnect action so that the thread will
     * exit.
     */
    static class Responder extends LCPResponder
    {
        Responder(NXTCommConnector con)
        {
            super(con);
        }

        protected void disconnect()
        {
            super.disconnect();
            super.shutdown();
        }
    }

    public static void main(String[] args) throws Exception
    {
        LCD.clear();
        LCD.drawString("Running...", 0, 1);
        Responder resp = new Responder(RS485.getConnector());
        resp.start();
        resp.join();
        LCD.drawString("Closing...  ", 0, 1);
    }
}

