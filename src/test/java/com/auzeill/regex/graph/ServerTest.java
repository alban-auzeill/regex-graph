package com.auzeill.regex.graph;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerTest {

  public static HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
  public static Server server;
  public static String server_url;

  @BeforeAll
  static void beforeAll() throws IOException {
    server = new Server(0);
    server_url = "http://localhost:" + server.getListeningPort();
  }

  @AfterAll
  static void afterAll() {
    server.stop();
  }

  @Test
  void index() throws IOException, InterruptedException {
    HttpResponse<String> response = httpGet(server_url + "/");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("<html lang=\"en\">").contains("</html>");
  }

  @Test
  void favicon_ico() throws IOException, InterruptedException {
    HttpResponse<String> response = httpGet(server_url + "/favicon.ico");
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void favicon_png() throws IOException, InterruptedException {
    HttpResponse<String> response = httpGet(server_url + "/favicon.png");
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void svg_pattern() throws IOException, InterruptedException {
    HttpResponse<String> response = httpGet(server_url + "/pattern?exp=abc");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("<svg").contains("</svg>");
  }

  @Test
  void svg_regex_tree() throws IOException, InterruptedException {
    HttpResponse<String> response = httpGet(server_url + "/regex-tree?exp=abc");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("<svg").contains("</svg>");
  }

  @Test
  void not_found() throws IOException, InterruptedException {
    HttpResponse<String> response = httpGet(server_url + "/unknown");
    assertThat(response.statusCode()).isEqualTo(404);
  }

  private HttpResponse<String> httpGet(String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .build();
    return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
  }

}
