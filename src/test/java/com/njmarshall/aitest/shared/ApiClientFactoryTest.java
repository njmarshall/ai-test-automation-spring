package com.njmarshall.aitest.shared;

import com.njmarshall.aitest.shared.client.ApiClientFactory;
import com.njmarshall.aitest.shared.config.FrameworkProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the factory builds one client per configured server and hands back the narrow
 * interface views. No HTTP is performed, only registry behavior is checked.
 */
class ApiClientFactoryTest {

    private ApiClientFactory factory() {
        FrameworkProperties.Server healthcare = new FrameworkProperties.Server();
        healthcare.setName("healthcare");
        healthcare.setBaseUrl("http://localhost:8081");

        FrameworkProperties.Server insurance = new FrameworkProperties.Server();
        insurance.setName("insurance");
        insurance.setBaseUrl("http://localhost:8082");

        return new ApiClientFactory(
                List.of(healthcare, insurance),
                new FrameworkProperties.Poll(),
                RestClient.builder());
    }

    @Test
    void registersOneClientPerServer() {
        assertThat(factory().serverNames()).containsExactlyInAnyOrder("healthcare", "insurance");
    }

    @Test
    void syncAndAsyncViewsResolveToTheCorrectServer() {
        ApiClientFactory factory = factory();

        assertThat(factory.sync("healthcare").serverName()).isEqualTo("healthcare");
        assertThat(factory.async("insurance").serverName()).isEqualTo("insurance");
    }

    @Test
    void unknownServerFailsFastWithAHelpfulMessage() {
        assertThatThrownBy(() -> factory().async("dental"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dental")
                .hasMessageContaining("healthcare");
    }
}
