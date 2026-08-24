package com.njmarshall.aitest.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * SINGLETON (Spring-native).
 *
 * <p>In the Java and Python versions the single source of configuration truth was a
 * hand-rolled Singleton (double-checked locking in Java, a module global in Python). Here the
 * container owns exactly one instance of this class for the whole JVM. We do not write locking
 * code, we do not expose a static getInstance(), and there is no mutable static state to leak
 * between tests. That is the honest Spring answer to "how do you do Singleton here": you declare
 * the bean and let the container guarantee single instantiation and thread-safe publication.
 *
 * <p>Bound from the "aitest" prefix in application.yml.
 */
@ConfigurationProperties(prefix = "aitest")
public class FrameworkProperties {

    /** LLM settings used by the AI TestGenerator. */
    private Llm llm = new Llm();

    /** One entry per API server the suite can target in a single run. Drives ApiClientFactory. */
    private List<Server> servers = new ArrayList<>();

    /** Global default polling behavior. Individual calls can override. */
    private Poll poll = new Poll();

    public Llm getLlm() {
        return llm;
    }

    public void setLlm(Llm llm) {
        this.llm = llm;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public static class Llm {
        private String baseUrl = "https://api.anthropic.com";
        private String model = "claude-sonnet-4-6";
        private int maxTokens = 4096;
        /** Read from env AITEST_LLM_API_KEY by default. Never hard-code a key. */
        private String apiKey;
        private String anthropicVersion = "2023-06-01";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getAnthropicVersion() {
            return anthropicVersion;
        }

        public void setAnthropicVersion(String anthropicVersion) {
            this.anthropicVersion = anthropicVersion;
        }
    }

    public static class Server {
        /** Logical key a test asks for, e.g. "healthcare" or "insurance". */
        private String name;
        private String baseUrl;
        /** Optional per-server bearer token, resolved from env in application.yml. */
        private String apiKey;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Poll {
        private int maxAttempts = 20;
        private long initialDelayMillis = 500;
        private long maxDelayMillis = 15_000;
        private double multiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialDelayMillis() {
            return initialDelayMillis;
        }

        public void setInitialDelayMillis(long initialDelayMillis) {
            this.initialDelayMillis = initialDelayMillis;
        }

        public long getMaxDelayMillis() {
            return maxDelayMillis;
        }

        public void setMaxDelayMillis(long maxDelayMillis) {
            this.maxDelayMillis = maxDelayMillis;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }
}
