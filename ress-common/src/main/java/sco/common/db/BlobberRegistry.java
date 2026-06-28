package sco.common.db;

import java.util.Iterator;
import java.util.ServiceLoader;

public class BlobberRegistry {
    private static BlobberRegistry service;
    public ServiceLoader<Blobber> loader;

    private BlobberRegistry() {
        loader = ServiceLoader.load(Blobber.class);
    }

    public static synchronized BlobberRegistry getBlobber() {
        if (service == null) {
            service = new BlobberRegistry();
        }
        return service;
    }

    public byte[] save(String s) {
        byte[] result = null;

        try {
            Iterator<Blobber> blobbers = loader.iterator();
            while (result == null && blobbers.hasNext()) {
                Blobber d = blobbers.next();
                result = d.save(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO dz log at least
        }
        return result;
    }

    public String restore(byte[] b) {
        String result = null;

        try {
            Iterator<Blobber> blobbers = loader.iterator();
            while (result == null && blobbers.hasNext()) {
                Blobber d = blobbers.next();
                result = d.restore(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO dz log at least
        }
        return result;
    }

    public String info() {
        String info = "";
        Iterator<Blobber> blobbers = BlobberRegistry.getBlobber().loader
                .iterator();
        while (blobbers.hasNext()) {
            Blobber d = blobbers.next();
            info += d.getClass().getSimpleName() + " ";
        }
        return info;
    }

    public static void main(String[] argv) {
        System.out.println(getBlobber().info());
    }
}
