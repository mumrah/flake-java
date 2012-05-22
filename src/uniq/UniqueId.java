package uniq;

import java.io.IOException;
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
    private final ThreadLocal<ByteBuffer> tlbb = new ThreadLocal<ByteBuffer>() {
        @Override
        public ByteBuffer initialValue() {
            return ByteBuffer.allocate(16);
        }       
    };
    private volatile short seq;
    private volatile long lastTimestamp;
    private final Object lock = new Object();
        
    public byte[] getId() {
        if(seq == Short.MAX_VALUE) {
            throw new RuntimeException("Too fast");
        }
        
        long time = 0;
        synchronized(lock) {
            time = System.currentTimeMillis();
            if(time != lastTimestamp) {
                lastTimestamp = time;
                seq = 0;
                // We're cutting the range of seq in half by counting from zero
                // but it makes for nicer looking IDs. Also, do we really expect
                // to generate more than 32767 IDs on a single node in one 
                // millisecond? Probably not.
            }
            seq++;
        }
        ByteBuffer bb = tlbb.get();
        bb.rewind();
        bb.putLong(time);
        bb.put(node);
        bb.putShort(seq);
        return bb.array();    
    }

    public static void main(String[] args) throws IOException {
        UniqueId uid = new UniqueId();
        int n = Integer.parseInt(args[0]);
        for(int i=0; i<n; i++) {
            System.out.write(uid.getId());
        }
    }
}
