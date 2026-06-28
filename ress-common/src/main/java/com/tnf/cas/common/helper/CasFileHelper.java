package com.tnf.cas.common.helper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;

public class CasFileHelper {

    private CasFileHelper() {
    }

    public static Path getTempDir() throws IOException {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"),
                System.getProperty("user.name"));
        Files.createDirectories(tmpDir);
        return tmpDir;
    }

    public static void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            Files.deleteIfExists(dir);
        }
    }

    public static void removeUnnecessaryFiles(Path dir, int maxFiles, Logger logger) {
        if (Files.exists(dir)) {
            try {
                List<FileRemoverInfo> list = new ArrayList<FileRemoverInfo>();
                try (DirectoryStream<Path> rootStream = Files.newDirectoryStream(dir)) {
                    for (Path tablePath : rootStream) {
                        if (Files.isDirectory(tablePath)) {
                            try (DirectoryStream<Path> subStream = Files
                                    .newDirectoryStream(tablePath)) {
                                for (Path path : subStream) {
                                    try {
                                        long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
                                        list.add(new FileRemoverInfo(path, lastModifiedTime));
                                    } catch (NoSuchFileException ex) {
                                    }
                                }
                            }
                        }
                    }
                }

                sortByLastModifiedTimeDescending(list);
                int deleted = 0;
                int i = 0;
                for (FileRemoverInfo fileInfo : list) {
                    if (++i > maxFiles) {
                        Path path = fileInfo.getPath();
                        if (Files.isDirectory(path)) {
                            logger.info("delete old directory: {} lastModified: {}",
                                    path.toString(),
                                    new Date(fileInfo.getLastModifiedTime()));
                            CasFileHelper.deleteDirectory(path);
                        } else {
                            logger.info("delete old file: {} lastModified: {}",
                                    path.toString(),
                                    new Date(fileInfo.getLastModifiedTime()));
                            Files.deleteIfExists(path);
                        }
                        deleted++;
                    }
                }
                logger.info(//
                        "Check {}: was {} entries, {} deleted.", //
                        new Object[] { dir, list.size(), deleted }//
                );

            } catch (IOException ex) {
                logger.error("", ex);
            }
        }
    }

    static void sortByLastModifiedTimeDescending(List<FileRemoverInfo> list) {
        Collections.sort(list, new Comparator<FileRemoverInfo>() {
            @Override
            public int compare(FileRemoverInfo p1, FileRemoverInfo p2) {
                long lastModifiedTime1 = p1.getLastModifiedTime();
                long lastModifiedTime2 = p2.getLastModifiedTime();
                if (lastModifiedTime2 > lastModifiedTime1) {
                    return 1;
                } else if (lastModifiedTime2 < lastModifiedTime1) {
                    return -1;
                }
                return 0;
            }
        });
    }

    static class FileRemoverInfo {
        private Path path;
        private Long lastModifiedTime;

        public FileRemoverInfo(Path path, Long lastModifiedTime) {
            this.path = path;
            this.lastModifiedTime = lastModifiedTime;
        }

        public Path getPath() {
            return path;
        }

        public Long getLastModifiedTime() {
            return lastModifiedTime;
        }
    }
}
