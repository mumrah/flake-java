package uniq;

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
    
    private volatile short seq;
    private volatile long lastTimestamp;
    private final Object lock = new Object();
        
    public byte[] getId() {
        long time = 0;
        synchronized(lock) {
            time = System.currentTimeMillis();
            if(time != lastTimestamp) {
                lastTimestamp = time;
                System.err.println(Thread.currentThread().getId() + " " + seq);
                seq = 0;
                // We're cutting the range of seq in half by counting from zero
                // but it makes for nicer looking IDs. Also, do we really expect
                // to generate more than 32767 IDs on a single node in one 
                // millisecond? Probably not.
            }
            else {
                if(seq == Short.MAX_VALUE) {
                    throw new RuntimeException("Too fast");
                }
            }
            seq++;
        }
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(time);
        bb.put(node);
        bb.putShort(seq);
        return bb.array();    
    }

    public static void main(String[] args) {
        UniqueId uid = new UniqueId();
        while(true) {
            System.out.println(uid.getId());
        }
    }
}