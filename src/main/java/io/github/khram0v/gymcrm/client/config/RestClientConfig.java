package io.github.khram0v.gymcrm.client.config;

import io.github.khram0v.gymcrm.client.security.ServiceTokenProvider;
import org.slf4j.MDC;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestFactory(requestFactory(5_000, 5_000));
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(ServiceTokenProvider serviceTokenProvider) {
        return RestClient.builder()
                .requestFactory(requestFactory(2_000, 3_000))
                .defaultHeaders(headers ->
                        headers.setContentType(MediaType.APPLICATION_JSON))
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(serviceTokenProvider.generateToken());

                    String transactionId = MDC.get(TRANSACTION_ID);
                    request.getHeaders().set(TRANSACTION_ID_HEADER, transactionId != null ? transactionId : "");

                    return execution.execute(request, body);
                });
    }

    private SimpleClientHttpRequestFactory requestFactory(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        return requestFactory;
    }
}
