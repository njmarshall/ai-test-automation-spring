package com.njmarshall.aitest.shared.testgen;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads an OpenAPI 3 document with swagger-parser and flattens it. Supports "classpath:" URIs so
 * a spec can ship inside the jar, plus plain file paths and URLs.
 */
public class SwaggerOpenApiSpecLoader implements OpenApiSpecLoader {

    private static final String CLASSPATH_PREFIX = "classpath:";

    @Override
    public List<ApiOperation> load(String specLocation) {
        OpenAPI openAPI = parse(specLocation);
        if (openAPI == null || openAPI.getPaths() == null) {
            throw new IllegalArgumentException("No usable OpenAPI paths found at: " + specLocation);
        }

        List<ApiOperation> operations = new ArrayList<>();
        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            Map<PathItem.HttpMethod, Operation> ops = pathEntry.getValue().readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> opEntry : ops.entrySet()) {
                operations.add(toApiOperation(path, opEntry.getKey(), opEntry.getValue()));
            }
        }
        return operations;
    }

    private ApiOperation toApiOperation(String path, PathItem.HttpMethod method, Operation op) {
        List<String> required = new ArrayList<>();
        if (op.getParameters() != null) {
            for (Parameter p : op.getParameters()) {
                if (Boolean.TRUE.equals(p.getRequired())) {
                    required.add(p.getName());
                }
            }
        }
        return new ApiOperation(
                method.name(),
                path,
                op.getOperationId(),
                op.getSummary(),
                required,
                op.getRequestBody() != null);
    }

    private OpenAPI parse(String specLocation) {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        if (specLocation.startsWith(CLASSPATH_PREFIX)) {
            String resource = specLocation.substring(CLASSPATH_PREFIX.length());
            String content = readClasspath(resource);
            SwaggerParseResult result = new OpenAPIV3Parser().readContents(content, null, options);
            return result.getOpenAPI();
        }

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specLocation, null, options);
        return result.getOpenAPI();
    }

    private String readClasspath(String resource) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("Classpath spec not found: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read classpath spec: " + resource, e);
        }
    }
}
