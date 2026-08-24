package com.njmarshall.aitest.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.njmarshall.aitest.shared.client.ApiClientFactory;
import com.njmarshall.aitest.shared.llm.AnthropicLlmClient;
import com.njmarshall.aitest.shared.llm.LlmClient;
import com.njmarshall.aitest.shared.testgen.OpenApiSpecLoader;
import com.njmarshall.aitest.shared.testgen.SwaggerOpenApiSpecLoader;
import com.njmarshall.aitest.shared.testgen.TestGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wiring. Every bean declared here is singleton-scoped by default, so these are the shared,
 * stateless collaborators the whole suite reuses. Contrast with the per-test data objects
 * (validators, entity factories) which are NOT beans, because they carry request-scoped state.
 * Keeping that line clear is the senior nuance: infrastructure is singleton, test data is not.
 */
@Configuration
public class FrameworkConfig {

    /**
     * ApiClientFactory is the runtime FACTORY that vends an API client per named server. The
     * container cannot do this for us, because the set of servers is data from application.yml,
     * not a compile-time set of beans. That is exactly why a hand-rolled factory still earns its
     * place on top of Spring's own bean factory.
     */
    @Bean
    public ApiClientFactory apiClientFactory(FrameworkProperties properties,
                                             RestClient.Builder restClientBuilder) {
        return new ApiClientFactory(properties.getServers(), properties.getPoll(), restClientBuilder);
    }

    @Bean
    public LlmClient llmClient(FrameworkProperties properties, RestClient.Builder restClientBuilder) {
        return new AnthropicLlmClient(properties.getLlm(), restClientBuilder, new ObjectMapper());
    }

    @Bean
    public OpenApiSpecLoader openApiSpecLoader() {
        return new SwaggerOpenApiSpecLoader();
    }

    @Bean
    public TestGenerator testGenerator(LlmClient llmClient,
                                       OpenApiSpecLoader specLoader,
                                       FrameworkProperties properties) {
        return new TestGenerator(llmClient, specLoader, new ObjectMapper(), properties.getLlm());
    }
}
