package dev.themobileapps.mrrsb.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class OpenApiConfigTest {

    private val openApiConfig = OpenApiConfig()

    @Test
    fun `should configure OpenAPI metadata`() {
        val openAPI = openApiConfig.openAPI()

        assertEquals("MRR Alert API", openAPI.info.title)
        assertEquals("Backend API for MRR Alert notification system", openAPI.info.description)
        assertEquals("1.0.0", openAPI.info.version)
        assertEquals("MRR Support", openAPI.info.contact.name)
        assertEquals("support@mrralert.com", openAPI.info.contact.email)
    }

    @Test
    fun `should configure security scheme`() {
        val openAPI = openApiConfig.openAPI()
        val securitySchemes = openAPI.components.securitySchemes

        assertNotNull(securitySchemes)
        val bearerScheme = securitySchemes["Bearer Authentication"]
        assertNotNull(bearerScheme)
        assertEquals("bearer", bearerScheme?.scheme)
        assertEquals("JWT", bearerScheme?.bearerFormat)
    }

    @Test
    fun `should configure server list`() {
        val openAPI = openApiConfig.openAPI()

        assertEquals(2, openAPI.servers.size)
        assertEquals("http://localhost:8080", openAPI.servers[0].url)
        assertEquals("Local server", openAPI.servers[0].description)
        assertEquals("https://api.mrralert.com", openAPI.servers[1].url)
        assertEquals("Production server", openAPI.servers[1].description)
    }
}
