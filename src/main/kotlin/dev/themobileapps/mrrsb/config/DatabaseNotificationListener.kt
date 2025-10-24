package dev.themobileapps.mrrsb.config

import dev.themobileapps.mrrsb.service.NotificationService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.postgresql.PGConnection
import org.postgresql.PGNotification
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import javax.sql.DataSource

/**
 * Listens for PostgreSQL NOTIFY events for new outbound SMS messages
 * This component is only active when using PostgreSQL database (not H2)
 */
@Component
@ConditionalOnProperty(
    name = ["spring.datasource.driver-class-name"],
    havingValue = "org.postgresql.Driver"
)
class DatabaseNotificationListener(
    private val dataSource: DataSource,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(DatabaseNotificationListener::class.java)
    private var listenerThread: Thread? = null
    private var running = false
    private var pgConnection: PGConnection? = null

    companion object {
        private const val NOTIFICATION_CHANNEL = "new_outbound_sms"
        private const val POLL_INTERVAL_MS = 1000L
    }

    /**
     * Initialize the database notification listener
     * Sets up the LISTEN command and starts the polling thread
     */
    @PostConstruct
    fun init() {
        try {
            logger.info("Initializing PostgreSQL notification listener for channel: $NOTIFICATION_CHANNEL")

            // Get the underlying PostgreSQL connection
            val connection = dataSource.connection
            pgConnection = connection.unwrap(PGConnection::class.java)

            // Execute LISTEN command
            val statement = connection.createStatement()
            statement.execute("LISTEN $NOTIFICATION_CHANNEL")
            statement.close()

            logger.info("Successfully subscribed to PostgreSQL channel: $NOTIFICATION_CHANNEL")

            // Start the listener thread
            startListening()

        } catch (e: Exception) {
            logger.error("Failed to initialize database notification listener: ${e.message}", e)
            // Don't throw - allow application to start even if listener fails
        }
    }

    /**
     * Start the background thread that polls for notifications
     */
    private fun startListening() {
        running = true

        listenerThread = Thread({
            logger.info("Database notification listener thread started")

            while (running && !Thread.currentThread().isInterrupted) {
                try {
                    // Poll for notifications
                    val notifications = pgConnection?.getNotifications(POLL_INTERVAL_MS.toInt())

                    notifications?.forEach { notification ->
                        handleNotification(notification)
                    }

                } catch (e: InterruptedException) {
                    logger.info("Database notification listener thread interrupted")
                    Thread.currentThread().interrupt()
                    break
                } catch (e: Exception) {
                    logger.error("Error in database notification listener: ${e.message}", e)

                    // Try to reconnect if connection is lost
                    if (!isConnectionValid()) {
                        logger.warn("Database connection lost, attempting to reconnect...")
                        reconnect()
                    }
                }
            }

            logger.info("Database notification listener thread stopped")
        }, "db-notification-listener")

        listenerThread?.isDaemon = true
        listenerThread?.start()
    }

    /**
     * Handle a notification received from PostgreSQL
     */
    private fun handleNotification(notification: PGNotification) {
        try {
            logger.debug("Received notification from channel ${notification.name}: ${notification.parameter}")

            // Process the notification payload
            notificationService.processNewMessage(notification.parameter)

        } catch (e: Exception) {
            logger.error("Error handling notification: ${e.message}", e)
        }
    }

    /**
     * Check if the database connection is still valid
     */
    private fun isConnectionValid(): Boolean {
        return try {
            pgConnection?.let { conn ->
                // Use reflection to check if connection is closed
                val connection = dataSource.connection
                !connection.isClosed
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Attempt to reconnect to the database and re-subscribe to notifications
     */
    private fun reconnect() {
        try {
            logger.info("Reconnecting to database for notification listener...")

            val connection = dataSource.connection
            pgConnection = connection.unwrap(PGConnection::class.java)

            val statement = connection.createStatement()
            statement.execute("LISTEN $NOTIFICATION_CHANNEL")
            statement.close()

            logger.info("Successfully reconnected and re-subscribed to $NOTIFICATION_CHANNEL")

        } catch (e: Exception) {
            logger.error("Failed to reconnect: ${e.message}", e)
        }
    }

    /**
     * Cleanup when the application shuts down
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down database notification listener...")

        running = false

        // Interrupt the listener thread
        listenerThread?.interrupt()

        // Wait for thread to finish (with timeout)
        try {
            listenerThread?.join(5000)
        } catch (e: InterruptedException) {
            logger.warn("Timeout waiting for listener thread to finish")
            Thread.currentThread().interrupt()
        }

        // Clean up the database connection
        try {
            pgConnection?.let {
                val statement = dataSource.connection.createStatement()
                statement.execute("UNLISTEN $NOTIFICATION_CHANNEL")
                statement.close()
            }
        } catch (e: Exception) {
            logger.error("Error during cleanup: ${e.message}", e)
        }

        logger.info("Database notification listener shut down completed")
    }
}
