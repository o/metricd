package org.polimat.metricd.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class IOUtils {

    public static boolean checkFile(final File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found");
        }

        if (!file.isFile()) {
            throw new IOException("Path not points to a file");
        }

        if (!file.canRead()) {
            throw new IOException("File is unreadable");
        }

        return true;
    }

}
