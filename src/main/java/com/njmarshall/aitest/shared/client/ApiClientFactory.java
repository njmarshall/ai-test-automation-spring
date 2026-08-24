package com.njmarshall.aitest.shared.client;

import com.njmarshall.aitest.shared.client.poll.Sleeper;
import com.njmarshall.aitest.shared.config.FrameworkProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FACTORY + INTERFACE SEGREGATION.
 *
 * <p>At construction it reads the list of configured servers and builds exactly one
 * {@link RestApiClient} per server, each pinned to its own base URL and optional bearer token. A
 * single test run can therefore target several back ends at once: {@code factory.async("healthcare")}
 * and {@code factory.sync("insurance")} return clients bound to different services.
 *
 * <p>The two accessors return narrow, segregated views of the same underlying object. Ask for a
 * {@link SyncApiClient} and you cannot see the poll methods; ask for an {@link AsyncApiClient} and
 * you get the polling surface. Callers depend only on the slice they use.
 *
 * <p>This factory is itself a singleton bean (see FrameworkConfig), but note it is a runtime
 * factory over configuration data, which is the thing Spring's own bean factory cannot give you:
 * the servers are rows in application.yml, not compile-time bean definitions.
 */
public class ApiClientFactory {

    private final Map<String, RestApiClient> clientsByServer = new HashMap<>();

    public ApiClientFactory(List<FrameworkProperties.Server> servers,
                            FrameworkProperties.Poll pollDefaults,
                            RestClient.Builder baseBuilder) {
        for (FrameworkProperties.Server server : servers) {
            RestClient.Builder builder = baseBuilder.clone().baseUrl(server.getBaseUrl());
            if (StringUtils.hasText(server.getApiKey())) {
                builder.defaultHeader("Authorization", "Bearer " + server.getApiKey());
            }
            RestApiClient client = new RestApiClient(
                    server.getName(), builder.build(), pollDefaults, Sleeper.REAL);
            clientsByServer.put(server.getName(), client);
        }
    }

    /** Narrow synchronous view of the client for the named server. */
    public SyncApiClient sync(String server) {
        return require(server);
    }

    /** Narrow asynchronous (polling) view of the client for the named server. */
    public AsyncApiClient async(String server) {
        return require(server);
    }

    public Set<String> serverNames() {
        return Set.copyOf(clientsByServer.keySet());
    }

    private RestApiClient require(String server) {
        RestApiClient client = clientsByServer.get(server);
        if (client == null) {
            throw new IllegalArgumentException(
                    "No server configured named '" + server + "'. Configured servers: "
                            + clientsByServer.keySet());
        }
        return client;
    }
}
