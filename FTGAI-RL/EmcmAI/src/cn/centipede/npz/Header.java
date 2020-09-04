package cn.centipede.npz;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * NPY file header.
 *
 * Presently NumPy implements two version of the NPY format: 1.0 and 2.0.
 * The difference between the two is the maximum size of the NPY header.
 * Version 1.0 requires it to be <=2**16 while version 2.0 allows <=2**32.
 *
 * The appropriate NPY format is chosen automatically based on the
 * header size.
 */
public class Header {
     /** Each NPY file *must* start with this byte sequence. */
     public static final byte[] MAGIC = {-109, 78, 85, 77, 80, 89};

     /** Maximum byte size of the header to be written as NPY1.0. */
     public static final int NPY_10_20_SIZE_BOUNDARY = 65535;

     /** Verion */
     int major;
     int minor;
     /** Meta-data formatted as a Python dict and 16-byte-padded. */
     String meta;

    /** Header size in bytes. */
    int[] shape;
    int size;
    char type;
    int bytes;
    ByteOrder order;


    public Header(ByteOrder order, char type, int bytes, int[] shape) {
        this.bytes = bytes;
        this.type = type;
        this.order = order;
        this.shape = shape;

        String metaUnpadded = new StringJoiner(", ", "{", "}")
                .add("descr: " + toChar(order) + type+bytes)
                .add("fortran_order: False")
                .add("shape: (" + Arrays.toString(shape) + ", )")
                .toString();

        int totalUnpadded = MAGIC.length + 2 + Short.BYTES + metaUnpadded.length() + 1;
        int padding = 16 - totalUnpadded % 16;
        int total = totalUnpadded + padding;

        if (total <= NPY_10_20_SIZE_BOUNDARY) {
            major = 1;
        } else {
            total += 2;  // fix for the XXX above.
            major = 2;
        }

        String repeated = new String(new char[padding]).replace("\0", " ");
        meta = metaUnpadded + repeated + '\n';
        size = total;
    }

    public static Header parseHeader(ByteBuffer input) {
        byte[] buf = new byte[6];
        input.get(buf);

        if (!Arrays.equals(MAGIC, buf)) {
            throw new RuntimeException("Not a legal npy!");
        }

        input.order(ByteOrder.LITTLE_ENDIAN);

        int major = input.get();
        int minor = input.get();

        if (major > 2 ) {
            String err = String.format("npy version: %d.%d\n", major, minor);
            throw new RuntimeException(err);
        }

        int size = (major == 1) ? input.getShort() : input.getInt();
        byte[] header = new byte[size];

        input.get(header);
        String s = new String(header, Charset.forName("UTF-8"));

        HashMap<String, Object> meta = parseDict(s);
        String dtype = meta.get("descr").toString();
        ByteOrder order = dtype.charAt(0)=='>'?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN;
        int[] shape = (int[])meta.get("shape");
        int bytes = Integer.parseInt(dtype.substring(2));

        return new Header(order, dtype.charAt(1), bytes, shape);
    }

    public Class<?> getType() {
        switch(this.type) {
            case 'b': return boolean.class;
            case 'i': case 'u': 
                switch(this.bytes) {
                case 1: return byte.class;
                case 2: return short.class;
                case 4: return int.class;
                case 8: return long.class;
                default: break;}
            case 'f': 
                switch(this.bytes) {
                case 4: return float.class;
                case 8: return double.class;
                default: break;}
            case 'S': return String.class;
            default: break;
        }
        throw new RuntimeException("Not support type!"); 
    }

    /**
     * "{'descr': '<f8', 'fortran_order': False, 'shape': (5, 5, 1, 6), }                                                    \n"
     * @param s
     * @return dict
     */
    private static HashMap<String, Object> parseDict(String s) {
        if (s.startsWith("{") && s.endsWith("}")) {
            throw new RuntimeException("Illegal header!");
        }

        HashMap<String, Object> dict = new HashMap<>();

        for (int i = 1; i < s.length()-1; i++) {
            StringBuilder sb = new StringBuilder();
            char c = s.charAt(++i);
            while (c != '\'') {
                sb.append(c);
                c = s.charAt(++i);
            }

            String name = sb.toString();
            sb = new StringBuilder();

            i += 3; // jump over [',_] or [', ]
            c = s.charAt(i);

            if (c != '(') {
                if (c == '\'') {
                    c = s.charAt(++i);
                }
                while (c != '\'' && c != ',') {
                    sb.append(c);
                    c = s.charAt(++i);
                }
                i++; // jump [_] or ['_]
                if (c != ',') i++; // jump [_] or ['_]
                dict.put(name, sb.toString());
                continue;
            }

            c = s.charAt(++i);
            while (c != ')') {
                sb.append(c);
                c = s.charAt(++i);
            }

            if (c == ')') {
                int[] shape = Stream.of(sb.toString().split(", *")).mapToInt(Integer::parseInt).toArray();
                dict.put(name, shape);
                break;
            }
        }
        return dict;
    }

    private char toChar(ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN){
            return '<';
        } else if (order == ByteOrder.BIG_ENDIAN){
            return '>';
        } else if (order == null) {
            return '|';
        } else {
            throw new RuntimeException("unkown order!");
        }
    }
}