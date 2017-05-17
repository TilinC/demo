package com.example.demo;

import brave.Tracing;
import brave.context.slf4j.MDCCurrentTraceContext;
import brave.http.HttpTracing;
import brave.httpclient.TracingHttpClientBuilder;
import brave.spring.webmvc.TracingHandlerInterceptor;
import java.io.IOException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@RestController
public class DemoApplication {

  /** Controls aspects of tracing such as the name that shows up in the UI */
  @Bean Tracing tracing() {
    return Tracing.newBuilder()
        .localServiceName("hello")
        .currentTraceContext(MDCCurrentTraceContext.create())
        .build();
  }

  /** Customize span names and tag policy here */
  @Bean HttpTracing httpTracing(Tracing tracing) {
    return HttpTracing.create(tracing);
  }

  /** Makes sure a trace is started for requests to the rest controller */
  @Bean WebMvcConfigurerAdapter traceIncomingRequests(HttpTracing httpTracing) {
    return new WebMvcConfigurerAdapter() {
      @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(TracingHandlerInterceptor.create(httpTracing));
      }
    };
  }

  /** Makes sure a trace is continued on outgoing requests */
  @Bean CloseableHttpClient traceOutgoingRequests(HttpTracing httpTracing) {
    return TracingHttpClientBuilder.create(httpTracing).build();
  }

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  private static Logger logger = org.slf4j.LoggerFactory.getLogger(DemoApplication.class);

  @Autowired CloseableHttpClient httpClient;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String index() throws IOException {
    HttpPost httpPost = new HttpPost("http://localhost:8080");

    logger.info("About to submit a request");
    httpClient.execute(httpPost);

    String traceId = MDC.get("traceId");

    return "traceId: " + traceId;
  }
}
