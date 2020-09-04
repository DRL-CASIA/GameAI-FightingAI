package cn.centipede.npz;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cn.centipede.numpy.NDArray;

/**
 * A ZIP file where each individual file is in NPY format.
 *
 * By convention each file has `.npy` extension, however, the API doesn't expose
 * it. So for instance the array named "X" will be accessibly via "X" and
 * **not** "X.npy".
 *
 * Example:
 *
 * @sample [org.jetbrains.bio.npy.npzExample]
 */
public class NpzFile {
    private List<NpzEntry> npzList = new ArrayList<>();;
    private HashMap<String, NDArray> npzMap = new HashMap<>();;

    public NpzFile(InputStream is) {
        try(ZipInputStream zis=new ZipInputStream(is, Charset.forName("US-ASCII"))) {
            parseNpz(zis);
        } catch (IOException e) {
           throw new RuntimeException(e.getMessage());
        }
    }

    private void parseNpz(ZipInputStream zis) throws IOException {
        ZipEntry entry = zis.getNextEntry();
        while(entry != null) {
            NDArray array = readEntry(zis, entry);
            npzMap.put(entry.getName(), array);
            entry = zis.getNextEntry();
        }
    }

    private NDArray readEntry(ZipInputStream zis, ZipEntry entry) {
        try {
            ByteBuffer chunk = getBuffers(zis, entry.getCompressedSize());
            Header header = parseHeader(chunk, entry);
            return NpyFile.read(header, chunk);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Read Npy failed!");
    }

    public NDArray get(String name) {
        return npzMap.get(name+".npy");
    }

    private ByteBuffer getBuffers(ZipInputStream zin, long step) throws IOException {
        ByteBuffer chunk = ByteBuffer.allocate(0);

        int remaining = zin.available();
        if (remaining > 0) {
            chunk = ByteBuffer.allocate((int)step);
        }

        Channels.newChannel(zin).read(chunk);
        chunk = chunk.asReadOnlyBuffer();
        chunk.flip();
        return chunk;
    }

    public List<NpzEntry> introspect() {
        npzList.stream().forEach(System.out::println);
        return npzList;
    }

    private Header parseHeader(ByteBuffer chunk, ZipEntry entry) {
        Header header = Header.parseHeader(chunk);
        String entryName = entry.getName();
        entryName = entryName.substring(0, entryName.lastIndexOf("."));
        NpzEntry npzEntry = new NpzEntry(entryName, header.getType(), header.shape);
        npzList.add(npzEntry);
        return header;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        InputStream stream = NpzFile.class.getResourceAsStream("/mnist.npz");
        NpzFile npz = new NpzFile(stream);
        npz.get("k1").dump();
        npz.get("b1").dump();
    }
}
