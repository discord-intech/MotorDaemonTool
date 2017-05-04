package sample;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Threads de contrôle du véhicule ; singleton
 */
public class Communication
{
    private Socket socket = null;

    private DataOutputStream oos = null;

    private BufferedReader iss = null;

    private static Communication instance = null;

    private double x = 0;

    private double y = 0;

    private double o = 0;

    private Communication() {}

    public static Communication getInstance()
    {
        if(instance == null) instance = new Communication();

        return instance;
    }

    public synchronized boolean connect(String ip)
    {
        try
        {
            if(oos != null) oos.close();

            if(socket != null) socket.close();
        } catch (IOException e) {e.printStackTrace();}

        try {
            socket = new Socket(ip, 56987);
            oos = new DataOutputStream(socket.getOutputStream());
            iss = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            oos.write("motordaemon".getBytes());
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if(socket.isConnected()) return true;
        else
        {
            System.out.println("Socket rage-quit.");
            return false;
        }

    }

    private void stop()
    {
        send("stop");
    }

    synchronized void send(String s)
    {
        if(oos == null || socket == null || !socket.isConnected()) return;

        //System.out.println(s);

        try {
            byte[] r = Arrays.copyOfRange(s.getBytes(), 0, 65536);

            oos.write(r);
            oos.flush();

            Thread.sleep(20);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized String[] sendAndReceive(String toSend, int numberOfLines)
    {
        if(oos == null || iss == null || socket == null || !socket.isConnected()) return null;

        send(toSend);

        String[] out = new String[numberOfLines];

        try {

            for(int i=0 ; i<numberOfLines ; i++)
            {
                out[i] = iss.readLine().replace("\0","");
                Thread.sleep(20);
            }

        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }

        return out;
    }

    public Double[] getPosition()
    {
        String[] sl = sendAndReceive("p", 1);

        if(sl == null) return null;

        String[] vals = sl[0].split(";");

        x = Double.parseDouble(vals[0]);
        y = Double.parseDouble(vals[1]);
        o = Double.parseDouble(vals[2].replace("\r", "").replace("\n", ""));

        return new Double[]{x,y,o};
    }

    public Long[] getSpeedValues()
    {
        String[] strs = sendAndReceive("sv", 1);

        if(strs == null) return null;

        String[] vals = strs[0].split(";");

        long t = Long.parseLong(vals[0]);
        long slc = Long.parseLong(vals[1]);
        long sl = Long.parseLong(vals[2]);
        long src = Long.parseLong(vals[3]);
        long sr = Long.parseLong(vals[4].replace("\r", "").replace("\n", ""));

        return new Long[]{t,slc,sl,src,sr};
    }

    public Double[] getPositionFast()
    {
        return new Double[]{x,y,o};
    }

    public ArrayList<Float> getConstants()
    {
        String[] lines = sendAndReceive("k", 4);

        if(lines == null) return null;

        ArrayList<Float> res = new ArrayList<>();

        for(String s : lines)
        {
            String[] vals = s.split(" ");
            res.add(Float.parseFloat(vals[3]));
            res.add(Float.parseFloat(vals[4]));
            res.add(Float.parseFloat(vals[5]));
        }
        return res;
    }

    public boolean isMoving() {
        String[] lines = sendAndReceive("m", 1);

        return lines != null && lines[0].contains("1");

    }

    public void setPosition(double x, double y)
    {
        send("setpos "+Integer.toString((int)x)+" "+Integer.toString((int)y));
    }

    public void setAngle(double o)
    {
        send("setang "+Float.toString((float)o));
    }
}
