package io.hops.cli.action;

import com.google.common.base.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class JobStartAction extends HopsworksAction {
  private static final Logger logger = LoggerFactory.getLogger(JobStartAction.class);
  
  private int jobId;
  private int projectId;
  
  public JobStartAction(String apiEndpoint, int port, boolean authentication, String path, int projectId) {
    super(apiEndpoint, port, authentication, path);
    this.projectId = projectId;
  }
  
  public JobStartAction(String apiEndpoint, int port, boolean authentication, String path, int projectId, int jobId) {
    super(apiEndpoint, port, authentication, path);
    this.projectId = projectId;
    this.jobId = jobId;
  }
  
  
  @Override
  public int execute() throws IOException {
    HttpContext localContext = null;
  
    if (getHttpServer().isAuthentication()) {
      List<Cookie> cookies;
      cookies = auth();
      localContext = generateContextWithCookies(cookies);
    }
  
//    HttpClient getClient = HttpClientBuilder.create().build();
    HttpClient getClient = getClient();
    String uri = getHttpServer().getAPIUrl() + "/project/" + projectId + "/jobs/" + jobId + "/executions";
    HttpPost request = new HttpPost(uri);
    request.addHeader("User-Agent", USER_AGENT);
    request.addHeader("ApiKey", getAuthData().getApiKey());

    //Set "Authorization" only for JWT. Not needed in Hopsworks 0.6
//    if (!Strings.isNullOrEmpty(getAuthData().getJwt())){
//      request.addHeader("Authorization", "Bearer " + getAuthData().getJwt());
//    }
    HttpResponse response = getClient.execute(request, localContext);
    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuilder result = new StringBuilder();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    JsonReader jsonReader = Json.createReader(new StringReader(result.toString()));
    JsonObject body = jsonReader.readObject();
    jsonReader.close();
    System.out.println("started job: " + jobId);
    return response.getStatusLine().getStatusCode();
  }
}
