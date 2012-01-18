package uniq;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;

import com.eaio.uuid.UUIDGen;

public class UniqueId {
    public static class IdStruct {
        public final long time;
        public final byte[] node;
        public final short seq;
        private final byte[] bytes;
        
        public IdStruct(long time, byte[] node, short seq) {
            this.time = time;
            this.node = node;
            this.seq = seq;
            
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.putLong(time);
            bb.put(node);
            bb.putShort(seq);
            this.bytes = bb.array();
        }
        
        public String toString() {
            return String.format("%011x-%s-%04x", time, Hex.encodeHexString(node), seq);
        }
        
        public byte[] toBytes() {
            return bytes;
        }
    }
    
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

    public IdStruct getId() {
        long time;
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
            else {
                if(seq == Short.MAX_VALUE) {
                    throw new RuntimeException("Too fast");
                }
            }
            seq++;
        }
        return new IdStruct(time, node, seq);
    }

    public static void main(String[] args) {
        UniqueId uid = new UniqueId();
        while(true) {
            System.out.println(uid.getId());
        }
    }
}