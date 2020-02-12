package info.hassan.jersey.statics.services;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Optional;

import info.hassan.jersey.statics.api.ResourceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation is intended to be used in production environment. Any resources request and
 * served is cached for later use. If a cached resource is updated at runtime, changes will not be
 * propagated further to clients.
 */
public class ResourceServiceImpl implements ResourceService {

  private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);
  protected final Path baseDir;

  /**
   * @param baseDir {@link Path} to the base directory e.g. /var/www/html
   * @throws FileNotFoundException if the path {@link Files#exists(Path, LinkOption...)} returns
   *     false
   */
  public ResourceServiceImpl(Path baseDir) throws FileNotFoundException {
    if (baseDir == null) {
      throw new IllegalArgumentException("Base directory path must not null");
    } else if (!Files.exists(baseDir)) {
      throw new FileNotFoundException("Base directory path must exist");
    }
    this.baseDir = baseDir;
  }

  @Override
  public ResourceResult getDataForResource(final String resourceName) {
    final Path resourcePath = Paths.get(baseDir.toString(), resourceName);
    final String resourceKey = resourcePath.toString();
    if (resourceCache.containsKey(resourceKey)) {
      return new ResourceResult(
          200, resourceCache.get(resourceKey).getKey(), resourceCache.get(resourceKey).getValue());
    }
    if (!Files.exists(resourcePath)) {
      final String msg = String.format("Path %s doesn't exists", resourcePath);
      log.warn(msg);
      if ("404.html".equals(resourceName)) {
        return new ResourceResult(
            404, TEXT_HTML, getPageProvidedResources(404).orElse(new byte[0]));
      } else if ("500.html".equals(resourceName)) {
        return new ResourceResult(
            500, TEXT_HTML, getPageProvidedResources(500).orElse(new byte[0]));
      } else {
        return getDataForResource("404.html");
      }
    } else {
      final Optional<AbstractMap.SimpleEntry<String, byte[]>> dataWithMimeType =
          readDataFromPath(resourcePath);
      dataWithMimeType.ifPresent(data -> resourceCache.put(resourceKey, data));
      return dataWithMimeType
          .map(entry -> new ResourceResult(200, entry.getKey(), entry.getValue()))
          .orElseGet(() -> new ResourceResult(404));
    }
  }

  @Override
  public Path getBaseDir() {
    return this.baseDir;
  }
}
