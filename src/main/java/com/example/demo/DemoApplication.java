package com.example.demo;

import brave.Tracing;
import brave.context.slf4j.MDCCurrentTraceContext;
import brave.httpclient.TracingHttpClientBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@SpringBootApplication
@RestController
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(DemoApplication.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index() throws IOException {
		Tracing tracing = Tracing.newBuilder()
				.localServiceName("helloClient")
				.currentTraceContext(MDCCurrentTraceContext.create())
//                .reporter(Reporter.NOOP)
				.build();

		CloseableHttpClient httpClient = TracingHttpClientBuilder.create(tracing).build();

		HttpPost httpPost = new HttpPost("http://localhost:8080");

		logger.info("About to submit a request");
		httpClient.execute(httpPost);

		String traceId = MDC.get("traceId");

		return "traceId: " + traceId;
	}
}
