package dev.themobileapps.mrrsb.exception

/**
 * Exception thrown when a requested resource is not found
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a user attempts to access a resource they don't own
 */
class UnauthorizedAccessException(message: String) : RuntimeException(message)
