package info.hassan.jersey.statics.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import info.hassan.jersey.statics.api.ResourceResult;

/** A service to load static resources */
public interface ResourceService {

  /** A cache to put the resources for later use */
  ConcurrentHashMap<String, Map.Entry<String, byte[]>> resourceCache = new ConcurrentHashMap<>();

  /**
   * Finds a resource either from cache from from the file system
   *
   * @param resourceName the name of the resource to find, e.g. index.html, css/main.css
   * @return an {@link Optional} of {@link java.util.AbstractMap.SimpleEntry} of mime-type and data
   *     as byte array
   */
  ResourceResult getDataForResource(final String resourceName);

  Path getBaseDir();
  /**
   * A utility method for getting default page from resources.
   *
   * @param pageNumber e.g 404 or 500
   * @return data bytes or empty
   */
  default Optional<byte[]> getPageProvidedResources(final int pageNumber) {
    final InputStream stream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(pageNumber + ".html");
    if (stream == null) {
      return Optional.empty();
    }
    return new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))
        .lines()
        .map(String::getBytes)
        .reduce(
            (a, b) -> {
              final byte[] c = new byte[a.length + b.length];
              System.arraycopy(a, 0, c, 0, a.length);
              System.arraycopy(b, 0, c, a.length, b.length);
              return c;
            });
  }

  /**
   * Loads data bytes and figures out mime-type from a path.
   *
   * @param rPath resource path
   * @return an Optional of Key and Value where key is mime-type and value is data bytes
   */
  default Optional<AbstractMap.SimpleEntry<String, byte[]>> readDataFromPath(final Path rPath) {
    if (!Files.exists(rPath)) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          new AbstractMap.SimpleEntry<>(Files.probeContentType(rPath), Files.readAllBytes(rPath)));
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
