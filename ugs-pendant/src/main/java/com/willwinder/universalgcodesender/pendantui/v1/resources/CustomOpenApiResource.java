package com.willwinder.universalgcodesender.pendantui.v1.resources;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.Collections;

public class CustomOpenApiResource extends OpenApiResource {
    public CustomOpenApiResource() {
        OpenAPI oas = new OpenAPI().info(new Info()
                    .title("Universal Gcode Sender Pendant API")
                    .description("API for controlling Universal Gcode Sender"))
                .servers(Collections.singletonList(new Server().url("/api/v1")));

        super.setOpenApiConfiguration(new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true));
    }
}