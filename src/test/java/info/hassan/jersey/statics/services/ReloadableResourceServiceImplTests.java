package info.hassan.jersey.statics.services;

import static info.hassan.jersey.statics.services.TestsHelper.TEXT_HTML;
import static info.hassan.jersey.statics.services.TestsHelper.assertEverythingInResource;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import info.hassan.jersey.statics.api.ResourceResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

@DisplayName("Testing the Reloadable ResourceService")
@DisabledOnOs({WINDOWS, MAC})
class ReloadableResourceServiceImplTests {

  private ResourceService service;

  @BeforeEach
  void setup() throws IOException {
    service =
        new ReloadableResourceServiceImpl(
            Paths.get("src", "test", "resources", "html"), SECONDS.toMillis(1));
  }

  @DisplayName("When a resource is updated at runtime, it gets reloaded after 1 second")
  @Test
  void reloadingIndexPage() throws IOException, InterruptedException {
    ResourceResult index = service.getDataForResource("index.html");
    assertEverythingInResource(index, 200, TEXT_HTML);
    final byte[] existingData = index.getData();
    final Document document = Jsoup.parse(new String(index.getData(), Charset.defaultCharset()));
    document.head().append("<script id='to-delete'>console.log('Adding test script');</script>");
    final String html = document.html();
    Files.write(
        service.getBaseDir().resolve("index.html"),
        html.getBytes(Charset.defaultCharset()),
        StandardOpenOption.TRUNCATE_EXISTING);
    System.err.println("Waiting for 1.5 seconds for cache to reload the changed resource");
    Thread.sleep(1500L);
    index = service.getDataForResource("index.html");
    assertNotEquals(existingData.length, index.getData().length);
    Files.write(
        service.getBaseDir().resolve("index.html"),
        existingData,
        StandardOpenOption.TRUNCATE_EXISTING);
  }
}
