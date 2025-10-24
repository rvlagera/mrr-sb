package dev.themobileapps.mrrsb.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

/**
 * Tests for DatabaseNotificationListener configuration
 * Verifies that the listener is only created when using PostgreSQL
 */
@SpringBootTest
class DatabaseNotificationListenerTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `should not create DatabaseNotificationListener bean when using H2 database`() {
        // When using H2 (test profile), the listener should not be created
        // due to @ConditionalOnProperty annotation

        // Then
        val hasListener = applicationContext.containsBean("databaseNotificationListener")

        // In test environment with H2, the bean should NOT exist
        assertThat(hasListener).isFalse()
    }

    @Test
    fun `should have NotificationService bean available`() {
        // NotificationService should always be available
        val hasService = applicationContext.containsBean("notificationService")

        assertThat(hasService).isTrue()
    }
}
