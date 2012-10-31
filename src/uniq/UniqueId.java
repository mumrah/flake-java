package uniq;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.eaio.uuid.UUIDGen;

public class UniqueId {
  
    // Get the MAC address (i.e., the "node" from a UUID1)
    private final long clockSeqAndNode = UUIDGen.getClockSeqAndNode();
    private final byte[] node = new byte[]{
        (byte)((clockSeqAndNode >> 40) & 0xff),
        (byte)((clockSeqAndNode >> 32) & 0xff),
        (byte)((clockSeqAndNode >> 24) & 0xff),
        (byte)((clockSeqAndNode >> 16) & 0xff),
        (byte)((clockSeqAndNode >> 8) & 0xff),
        (byte)((clockSeqAndNode >> 0) & 0xff),
    };

    private volatile int seq;
    private volatile long lastTimestamp;

    private final ByteBuffer bb = ByteBuffer.allocate(16);
    private final int min;
    private final int max;

    public UniqueId(int min, int max) {
      this.min = min;
      this.max = max;
      System.err.println("Staring UniqueId[" + min + "," + max + "]");
    }
        
    public byte[] getId() throws InterruptedException {
        if(seq >= max) {
            System.err.println("Too fast");
            Thread.sleep(1);
        }
        
        long time = System.currentTimeMillis();
        if(time != lastTimestamp) {
            lastTimestamp = time;
            seq = min;
        }
        seq++;
        bb.rewind();
        bb.putLong(time);
        bb.put(node);
        bb.putShort((short)seq);
        return bb.array();    
    }

    /*
    public String getStringId() {
      byte[] ba = getId();
      ByteBuffer bb = ByteBuffer.wrap(ba);
      long ts = bb.getLong();
      int node_0 = bb.getInt();
      short node_1 = bb.getShort();
      short seq = bb.getShort();
      return String.format("%016d-%s%s-%04d", ts, Integer.toHexString(node_0), Integer.toHexString(node_1), seq);
    }*/

    public static Thread newThread(final int min, final int max, final int n) {
      return new Thread(new Runnable(){
        @Override
        public void run() {
          try {
            UniqueId uid = new UniqueId(min, max);
            OutputStream os = new FileOutputStream(Thread.currentThread().getId() + ".out");
            long t0 = System.currentTimeMillis();
            for(int i=0; i<n; i++) {
              os.write(uid.getId());
            }
            long t1 = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getId() + " " + n + " " + (t1-t0));
          } catch(IOException e) {

          } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
          }
        }
      });
    }

    public static void main(String[] args) throws IOException {
        int n = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        int max = 0xffff;
        int step = max / m;
        int x = 0;
        for(int i=0; i<m; i++) {
          newThread(x, x+step, n).start();
          x += step;
        }
    }
}
