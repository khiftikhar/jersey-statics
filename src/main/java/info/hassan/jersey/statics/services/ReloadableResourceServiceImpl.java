package info.hassan.jersey.statics.services;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadableResourceServiceImpl extends ResourceServiceImpl implements Closeable {

  public static final int MAX_DEPTH = 10;
  private static final Logger log = LoggerFactory.getLogger(ReloadableResourceServiceImpl.class);
  private static final long DEFAULT_POLL_FOR_CHANGE_IN_MILLIS = 5000L;
  private static final Kind<?>[] WATCH_EVENTS =
      new Kind<?>[] {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW};
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private WatchService watchService;

  /**
   * @param baseDir the base directory and all its subdirectories to depth = 10 to be watched
   * @param pollForChangeInMillis polling for changes in milliseconds, min is 1000L or one second
   * @throws IOException when baseDir couldn't read or some other I/O issues
   */
  public ReloadableResourceServiceImpl(Path baseDir, final long pollForChangeInMillis)
      throws IOException {
    super(baseDir);
    watchService = FileSystems.getDefault().newWatchService();
    baseDir.register(watchService, WATCH_EVENTS);
    addDirectoryToWatchList(baseDir);

    executor.scheduleAtFixedRate(
        this::pollForChanges,
        1000L,
        pollForChangeInMillis <= 1000L ? DEFAULT_POLL_FOR_CHANGE_IN_MILLIS : pollForChangeInMillis,
        TimeUnit.MILLISECONDS);
  }

  private void pollForChanges() {
    WatchKey key = watchService.poll();
    if (key != null) {
      for (WatchEvent<?> event : key.pollEvents()) {
        if (key.watchable() instanceof Path && event.context() instanceof Path) {
          final Path resourcePath =
              Paths.get(key.watchable().toString(), event.context().toString());
          if (resourcePath.toString().endsWith("~")) { // Still not saved
            continue;
          }
          log.info("This resource {}:  is {}", resourcePath.toString(), event.kind());
          if (event.kind().equals(ENTRY_MODIFY) && event.count() > 1) {
            if (resourceCache.containsKey(resourcePath.toString())) {
              log.debug("Updating cache with new data from resource: {}", resourcePath);
              readDataFromPath(resourcePath)
                  .ifPresent(entry -> resourceCache.replace(resourcePath.toString(), entry));
            }
          } else if (event.kind().equals(ENTRY_CREATE)) {
            if (Files.isDirectory(resourcePath)) {
              addDirectoryToWatchList(resourcePath);
            }
          } else if (event.kind().equals(ENTRY_DELETE)) {
            final Map.Entry<String, byte[]> entry = resourceCache.remove(resourcePath.toString());
            if (entry != null) {
              log.debug("Deleted resource: {} from the cache.", resourcePath);
            }
          } else if (event.kind().equals(OVERFLOW)) {
            log.debug("The event for resource : {} lost this time", resourcePath);
          }
        }
      }
      key.reset();
    }
  }

  private void addDirectoryToWatchList(final Path directory) {
    if (Files.isDirectory(directory)) {
      try {
        Files.walkFileTree(
            directory,
            Set.of(FOLLOW_LINKS),
            MAX_DEPTH,
            new SimpleFileVisitor<>() {
              @Override
              public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                  throws IOException {
                if (!dir.toString().endsWith("~")) {
                  dir.register(watchService, WATCH_EVENTS);
                  log.debug("Watching directory: {}", dir);
                }
                return FileVisitResult.CONTINUE;
              }
            });
      } catch (IOException e) {
        log.warn("Unable to start watching the new directory : {}", directory, e);
      }
    } else {
      log.warn("Only a directory can be added to watch list: {} is not a directory ", directory);
    }
  }

  @Override
  public void close() throws IOException {
    watchService.close();
    executor.shutdownNow();
  }
}
