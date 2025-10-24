package dev.themobileapps.mrrsb.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

/**
 * Error response data class
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val details: List<String>? = null
)

/**
 * Global exception handler for REST controllers
 */
@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * Handle bad credentials exception (401)
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                error = "INVALID_CREDENTIALS",
                message = "Invalid username or password"
            ))
    }
    
    /**
     * Handle resource not found exception (404)
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                error = "RESOURCE_NOT_FOUND",
                message = ex.message ?: "Resource not found"
            ))
    }
    
    /**
     * Handle unauthorized access exception (403)
     */
    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleUnauthorizedAccess(ex: UnauthorizedAccessException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(
                error = "UNAUTHORIZED_ACCESS",
                message = ex.message ?: "You are not authorized to access this resource"
            ))
    }
    
    /**
     * Handle validation errors (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.map { error ->
            val fieldName = (error as? FieldError)?.field ?: "unknown"
            val errorMessage = error.defaultMessage ?: "validation error"
            "$fieldName: $errorMessage"
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                error = "VALIDATION_ERROR",
                message = "Validation failed",
                details = errors
            ))
    }
    
    /**
     * Handle unsupported operation exception (501)
     */
    @ExceptionHandler(UnsupportedOperationException::class)
    fun handleUnsupportedOperation(ex: UnsupportedOperationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(ErrorResponse(
                error = "NOT_IMPLEMENTED",
                message = ex.message ?: "This operation is not supported"
            ))
    }
    
    /**
     * Handle generic exceptions (500)
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                error = "INTERNAL_ERROR",
                message = "An unexpected error occurred"
            ))
    }
}
