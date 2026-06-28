package com.tnf.cas.webserver;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class DevWatcher {

    public static void main(String[] argv) throws Exception {
        String fileName = argv[0];
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(fileName);
        path.register(watcher, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);

        for (;;) {
            WatchKey key = watcher.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println(event.kind());
                System.out.println(event.context());
            }
            boolean valid = key.reset();
            if (!valid) {
                System.out.println("exit");
                break;
            }
        }
    }

}
