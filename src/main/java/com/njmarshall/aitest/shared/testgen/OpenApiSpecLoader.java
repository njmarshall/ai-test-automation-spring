package com.njmarshall.aitest.shared.testgen;

import java.util.List;

/**
 * Reads an OpenAPI 3 document and flattens it to a list of {@link ApiOperation}. Kept behind an
 * interface so the parsing library is swappable and so TestGenerator tests can feed operations
 * directly without a real spec file.
 */
public interface OpenApiSpecLoader {

    /**
     * @param specLocation a classpath resource ("classpath:openapi/x.yaml"), a file path, or a URL
     * @return every operation declared in the spec
     */
    List<ApiOperation> load(String specLocation);
}
