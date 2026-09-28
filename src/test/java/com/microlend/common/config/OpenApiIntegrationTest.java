package com.microlend.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("MicroLend API"))
                .andExpect(jsonPath("$.components.securitySchemes['Bearer Authentication']").exists())
                
                // Verify that public endpoints are present
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/register']").exists())
                .andExpect(jsonPath("$.paths['/api/health']").exists())

                // Verify that protected endpoints are present and require security
                .andExpect(jsonPath("$.paths['/api/customers/me']").exists())
                .andExpect(jsonPath("$.paths['/api/customers/me'].get.security").isArray())
                
                // Verify sensitive info is excluded
                .andExpect(jsonPath("$.components.schemas.User.properties.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.User.properties.password").doesNotExist())
                .andExpect(jsonPath("$..[?(@ == 'passwordHash')]").doesNotExist());
    }
}
