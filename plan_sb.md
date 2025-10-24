# Spring Boot Backend - Implementation Plan

## Subtask Implementation Workflow

### Before Starting a Subtask

- **Run tests** to verify all are passing
- **If any test is failing:**
  - Create a `./stop-tasks.txt` file
  - Write the reason for stopping
  - Stop without implementing the subtask
- **Reference** `./tasks-summary.md` for anything that could help with current subtask implementation

### During Subtask Execution

- **Each subtask must have appropriate test(s)**
- **If for whatever reason the task cannot be completed:**
  - Create a `./stop-tasks.txt` file
  - Write the reason for stopping
  - Stop immediately

### After Subtask Completion

1. **Update** the `./plan_sb.md` file (mark subtask as completed with ✅)
2. **Create or update** `./tasks-summary.md` with:
   - Subtask name
   - Timestamp when subtask was completed
   - Short description of things done
   - Information that will help future subtask execution
3. **Create a summary file** in `./tasks-summary/` folder:
   - Named after the completed subtask (e.g., `task-1.1.1-summary.md`)
   - Description of things done
   - List of files created or updated
4. **Git commit and push:**
   - Add all new files in the project
   - Commit with meaningful description
   - Push using `git push` command
5. **Ask to continue:**
   - If explicitly asked to complete only one subtask, stop after completion
   - Otherwise, always ask to continue to the next subtask

---

## Quick Start Guide

### Current Environment Requirements

✅ **Prerequisites to Verify:**

- JDK 17 (Corretto 17)
- Kotlin 1.9.25
- Maven 3.8+
- PostgreSQL 17.6 (running on port 5432, database: 'mrr', user: 'mrr_user')
- Git
- IntelliJ IDEA (recommended) or VS Code with Kotlin plugin

### Immediate Next Steps

1. **Navigate to Project:**
   ```bash
   cd /Users/rommel/mrralert/mrr-sb
   ```

2. **Setup Database Connection:**
   ```bash
   # Create application-local.properties with PostgreSQL connection
   cat > src/main/resources/application-local.properties << 'EOL'
   spring.datasource.url=jdbc:postgresql://localhost:5432/mrr
   spring.datasource.username=mrr_user
   spring.datasource.password=respond!qu1cklY
   spring.profiles.active=local
   EOL
   ```

3. **Verify Project Setup:**
   ```bash
   ./mvnw clean compile
   ./mvnw test
   ```

## Project Overview

A Spring Boot backend service providing REST APIs and WebSocket connections for the MRR Alert notification system. This backend will handle authentication, message retrieval, real-time updates via WebSocket, and integration with the existing PostgreSQL database (read-only operations).

## Technology Stack

- **Framework**: Spring Boot 3.2.x (LTS)
- **Language**: Kotlin 1.9.25
- **Runtime**: JDK 17 (Corretto)
- **Build Tool**: Maven 3.8+
- **Database**: PostgreSQL 17.6 (existing, read-only)
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **WebSocket**: Spring WebSocket with STOMP
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, MockK, TestContainers
- **Monitoring**: Spring Actuator
- **Logging**: SLF4J with Logback
- **JSON**: Jackson
- **Validation**: Jakarta Bean Validation
- **Development Tools**: Spring Boot DevTools

## Phase 1: Project Setup and Configuration

### Task 1.1: Project Structure Setup

- [✅] **Sub-task 1.1.1**: Configure Maven dependencies
  ```xml
  <!-- pom.xml additions -->
  <properties>
    <kotlin.version>1.9.25</kotlin.version>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
  </properties>

  <dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Kotlin -->
    <dependency>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-stdlib-jdk8</artifactId>
    </dependency>
    <dependency>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-reflect</artifactId>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.module</groupId>
      <artifactId>jackson-module-kotlin</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    
    <!-- JWT -->
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.11.5</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>
    
    <!-- Documentation -->
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.2.0</version>
    </dependency>
    
    <!-- Development -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-devtools</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>io.mockk</groupId>
      <artifactId>mockk</artifactId>
      <version>1.13.8</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>postgresql</artifactId>
      <version>1.19.0</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>1.19.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  ```

- [✅] **Sub-task 1.1.2**: Create package structure
  ```
  src/main/kotlin/dev/themobileapps/mrrsb/
  ├── config/              # Configuration classes
  │   ├── DatabaseConfig.kt
  │   ├── SecurityConfig.kt
  │   ├── WebSocketConfig.kt
  │   └── JwtConfig.kt
  ├── controller/          # REST controllers
  │   ├── AuthController.kt
  │   ├── MessageController.kt
  │   └── UserController.kt
  ├── dto/                 # Data Transfer Objects
  │   ├── request/
  │   └── response/
  ├── entity/              # JPA entities
  │   ├── Person.kt
  │   └── OutboundSms.kt
  ├── repository/          # JPA repositories
  │   ├── PersonRepository.kt
  │   └── OutboundSmsRepository.kt
  ├── service/             # Business logic
  │   ├── AuthService.kt
  │   ├── MessageService.kt
  │   └── NotificationService.kt
  ├── security/            # Security components
  │   ├── JwtAuthenticationFilter.kt
  │   ├── JwtTokenProvider.kt
  │   └── CustomUserDetailsService.kt
  ├── websocket/           # WebSocket handlers
  │   ├── MessageWebSocketHandler.kt
  │   └── NotificationWebSocketService.kt
  ├── exception/           # Exception handling
  │   ├── GlobalExceptionHandler.kt
  │   └── CustomExceptions.kt
  ├── util/                # Utility classes
  │   └── DateTimeUtils.kt
  └── MrrSbApplication.kt  # Main application class
  ```

- [✅] **Sub-task 1.1.3**: Configure application properties
  ```properties
  # application.properties
  server.port=8080
  server.servlet.context-path=/api
  
  # Database Configuration (Read-Only)
  spring.datasource.url=jdbc:postgresql://localhost:5432/mrr
  spring.datasource.username=mrr_user
  spring.datasource.password=${DB_PASSWORD:password}
  spring.datasource.driver-class-name=org.postgresql.Driver
  
  # JPA Configuration
  spring.jpa.database=POSTGRESQL
  spring.jpa.show-sql=false
  spring.jpa.hibernate.ddl-auto=none
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
  spring.jpa.properties.hibernate.default_schema=public
  spring.jpa.open-in-view=false
  
  # JWT Configuration
  jwt.secret=${JWT_SECRET:your-secret-key-change-in-production}
  jwt.expiration=86400000
  
  # Logging
  logging.level.dev.themobileapps.mrrsb=DEBUG
  logging.level.org.springframework.web=INFO
  logging.level.org.hibernate.SQL=DEBUG
  
  # CORS Configuration
  cors.allowed-origins=http://localhost:3000,http://localhost:8080
  cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
  cors.allowed-headers=*
  cors.allow-credentials=true
  
  # WebSocket Configuration
  websocket.allowed-origins=*
  websocket.message-size-limit=65536
  websocket.send-buffer-size=524288
  websocket.send-time-limit=20000
  
  # Actuator
  management.endpoints.web.exposure.include=health,info,metrics
  management.endpoint.health.show-details=when-authorized
  
  # Jackson Configuration
  spring.jackson.default-property-inclusion=non_null
  spring.jackson.serialization.write-dates-as-timestamps=false
  
  # API Documentation
  springdoc.api-docs.path=/api-docs
  springdoc.swagger-ui.path=/swagger-ui.html
  ```

### Task 1.2: Database Integration

- [✅] **Sub-task 1.2.1**: Create JPA entities
  ```kotlin
  // Person.kt
  @Entity
  @Table(name = "person")
  data class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val personId: Int = 0,
    
    @Column(name = "username", unique = true, nullable = false)
    val username: String,
    
    @Column(name = "password")
    val password: String,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "mobile_no")
    val mobileNo: String? = null,
    
    @Column(name = "active")
    val active: Boolean = true,
    
    @Column(name = "date_created")
    val dateCreated: LocalDateTime? = null,
    
    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    val outboundMessages: List<OutboundSms> = emptyList()
  )
  
  // OutboundSms.kt
  @Entity
  @Table(name = "outbound_sms")
  data class OutboundSms(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val smsId: Int = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    val person: Person? = null,
    
    @Column(name = "message", columnDefinition = "TEXT")
    val message: String,
    
    @Column(name = "alert_level")
    val alertLevel: Int = 0,
    
    @Column(name = "date_requested")
    val dateRequested: LocalDateTime,
    
    @Column(name = "date_sent")
    val dateSent: LocalDateTime? = null,
    
    @Column(name = "mobile_no")
    val mobileNo: String,
    
    @Column(name = "status")
    val status: String,
    
    @Column(name = "requested_by")
    val requestedBy: String
  )
  ```

- [✅] **Sub-task 1.2.2**: Create repositories
  ```kotlin
  // PersonRepository.kt
  @Repository
  interface PersonRepository : JpaRepository<Person, Int> {
    fun findByUsername(username: String): Person?
    fun findByUsernameAndActive(username: String, active: Boolean): Person?
  }
  
  // OutboundSmsRepository.kt
  @Repository
  interface OutboundSmsRepository : JpaRepository<OutboundSms, Int> {
    fun findByPersonPersonIdOrderByDateRequestedDesc(
      personId: Int,
      pageable: Pageable
    ): Page<OutboundSms>
    
    fun findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(
      personId: Int,
      after: LocalDateTime
    ): List<OutboundSms>
    
    fun countByPersonPersonIdAndDateSentIsNull(personId: Int): Long
  }
  ```

- [✅] **Sub-task 1.2.3**: Configure database listener for real-time updates
  ```kotlin
  // DatabaseNotificationListener.kt
  @Component
  class DatabaseNotificationListener(
    private val dataSource: DataSource,
    private val notificationService: NotificationService
  ) {
    private lateinit var pgConnection: PGConnection
    
    @PostConstruct
    fun init() {
      val connection = dataSource.connection
      pgConnection = connection.unwrap(PGConnection::class.java)
      
      val statement = connection.createStatement()
      statement.execute("LISTEN new_outbound_sms")
      statement.close()
      
      startListening()
    }
    
    private fun startListening() {
      Thread {
        while (!Thread.currentThread().isInterrupted) {
          try {
            val notifications = pgConnection.getNotifications(1000)
            notifications?.forEach { notification ->
              handleNotification(notification)
            }
          } catch (e: Exception) {
            // Log error and reconnect if needed
          }
        }
      }.start()
    }
    
    private fun handleNotification(notification: PGNotification) {
      val payload = notification.parameter
      notificationService.processNewMessage(payload)
    }
  }
  ```

## Phase 2: Authentication System

### Task 2.1: JWT Authentication Implementation

- [✅] **Sub-task 2.1.1**: Create JWT token provider
  ```kotlin
  // JwtTokenProvider.kt
  @Component
  class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
  ) {
    fun generateToken(username: String, userId: Int): String {
      val now = Date()
      val expiryDate = Date(now.time + expiration)
      
      return Jwts.builder()
        .setSubject(username)
        .claim("userId", userId)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, secret)
        .compact()
    }
    
    fun validateToken(token: String): Boolean {
      return try {
        Jwts.parser().setSigningKey(secret).parseClaimsJws(token)
        true
      } catch (e: Exception) {
        false
      }
    }
    
    fun getUsernameFromToken(token: String): String {
      val claims = Jwts.parser()
        .setSigningKey(secret)
        .parseClaimsJws(token)
        .body
      return claims.subject
    }
  }
  ```

- [✅] **Sub-task 2.1.2**: Create authentication filter
  ```kotlin
  // JwtAuthenticationFilter.kt
  @Component
  class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService
  ) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
      request: HttpServletRequest,
      response: HttpServletResponse,
      filterChain: FilterChain
    ) {
      val token = extractToken(request)
      
      if (token != null && jwtTokenProvider.validateToken(token)) {
        val username = jwtTokenProvider.getUsernameFromToken(token)
        val userDetails = userDetailsService.loadUserByUsername(username)
        
        val authentication = UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
      }
      
      filterChain.doFilter(request, response)
    }
    
    private fun extractToken(request: HttpServletRequest): String? {
      val bearerToken = request.getHeader("Authorization")
      return if (bearerToken?.startsWith("Bearer ") == true) {
        bearerToken.substring(7)
      } else null
    }
  }
  ```

- [✅] **Sub-task 2.1.3**: Create authentication service
  ```kotlin
  // AuthService.kt
  @Service
  class AuthService(
    private val personRepository: PersonRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
  ) {
    fun authenticate(username: String, password: String): AuthResponse {
      val person = personRepository.findByUsernameAndActive(username, true)
        ?: throw BadCredentialsException("Invalid credentials")
      
      // Check password (support both plain text and bcrypt for migration)
      val passwordMatch = if (person.password.startsWith("$2")) {
        passwordEncoder.matches(password, person.password)
      } else {
        password == person.password
      }
      
      if (!passwordMatch) {
        throw BadCredentialsException("Invalid credentials")
      }
      
      val token = jwtTokenProvider.generateToken(username, person.personId)
      
      return AuthResponse(
        token = token,
        username = username,
        name = person.name,
        userId = person.personId
      )
    }
  }
  ```

### Task 2.2: Security Configuration

- [ ] **Sub-task 2.2.1**: Configure Spring Security
  ```kotlin
  // SecurityConfig.kt
  @Configuration
  @EnableWebSecurity
  @EnableMethodSecurity(prePostEnabled = true)
  class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val corsProperties: CorsProperties
  ) {
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
      return http
        .csrf { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .sessionManagement { 
          it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
        .authorizeHttpRequests { auth ->
          auth.requestMatchers(
            "/api/auth/login",
            "/api/health",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/ws/**"
          ).permitAll()
          auth.anyRequest().authenticated()
        }
        .addFilterBefore(
          jwtAuthenticationFilter,
          UsernamePasswordAuthenticationFilter::class.java
        )
        .exceptionHandling { 
          it.authenticationEntryPoint(JwtAuthenticationEntryPoint())
        }
        .build()
    }
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
      val configuration = CorsConfiguration().apply {
        allowedOrigins = corsProperties.allowedOrigins
        allowedMethods = corsProperties.allowedMethods
        allowedHeaders = corsProperties.allowedHeaders
        allowCredentials = corsProperties.allowCredentials
      }
      
      val source = UrlBasedCorsConfigurationSource()
      source.registerCorsConfiguration("/**", configuration)
      return source
    }
    
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
  }
  ```

## Phase 3: REST API Implementation

### Task 3.1: Authentication Controller

- [ ] **Sub-task 3.1.1**: Create auth controller
  ```kotlin
  // AuthController.kt
  @RestController
  @RequestMapping("/auth")
  @Tag(name = "Authentication", description = "Authentication endpoints")
  class AuthController(
    private val authService: AuthService
  ) {
    
    @PostMapping("/login")
    @Operation(summary = "User login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
      val response = authService.authenticate(request.username, request.password)
      return ResponseEntity.ok(response)
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token")
    fun refresh(
      @RequestHeader("Authorization") token: String
    ): ResponseEntity<AuthResponse> {
      // Implementation for token refresh
      return ResponseEntity.ok(authService.refreshToken(token))
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout")
    fun logout(): ResponseEntity<Void> {
      // Clear any server-side session if needed
      return ResponseEntity.ok().build()
    }
  }
  ```

### Task 3.2: Message Controller

- [ ] **Sub-task 3.2.1**: Create message controller
  ```kotlin
  // MessageController.kt
  @RestController
  @RequestMapping("/messages")
  @Tag(name = "Messages", description = "Message management endpoints")
  class MessageController(
    private val messageService: MessageService
  ) {
    
    @GetMapping
    @Operation(summary = "Get user messages")
    fun getMessages(
      @AuthenticationPrincipal user: CustomUserDetails,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "20") size: Int,
      @RequestParam(required = false) after: LocalDateTime?
    ): ResponseEntity<Page<MessageDto>> {
      val messages = messageService.getUserMessages(
        user.userId,
        PageRequest.of(page, size, Sort.by("dateRequested").descending()),
        after
      )
      return ResponseEntity.ok(messages)
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID")
    fun getMessage(
      @PathVariable id: Int,
      @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<MessageDto> {
      val message = messageService.getMessage(id, user.userId)
      return ResponseEntity.ok(message)
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count")
    fun getUnreadCount(
      @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<UnreadCountDto> {
      val count = messageService.getUnreadCount(user.userId)
      return ResponseEntity.ok(UnreadCountDto(count))
    }
    
    @PutMapping("/{id}/mark-read")
    @Operation(summary = "Mark message as read")
    fun markAsRead(
      @PathVariable id: Int,
      @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<Void> {
      messageService.markAsRead(id, user.userId)
      return ResponseEntity.ok().build()
    }
  }
  ```

### Task 3.3: User Controller

- [ ] **Sub-task 3.3.1**: Create user controller
  ```kotlin
  // UserController.kt
  @RestController
  @RequestMapping("/users")
  @Tag(name = "Users", description = "User management endpoints")
  class UserController(
    private val userService: UserService
  ) {
    
    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    fun getProfile(
      @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<UserProfileDto> {
      val profile = userService.getUserProfile(user.userId)
      return ResponseEntity.ok(profile)
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    fun updateProfile(
      @AuthenticationPrincipal user: CustomUserDetails,
      @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserProfileDto> {
      val profile = userService.updateProfile(user.userId, request)
      return ResponseEntity.ok(profile)
    }
  }
  ```

## Phase 4: WebSocket Implementation

### Task 4.1: WebSocket Configuration

- [ ] **Sub-task 4.1.1**: Configure WebSocket with STOMP
  ```kotlin
  // WebSocketConfig.kt
  @Configuration
  @EnableWebSocketMessageBroker
  class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
      config.enableSimpleBroker("/topic", "/queue")
      config.setApplicationDestinationPrefixes("/app")
    }
    
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
      registry.addEndpoint("/ws")
        .setAllowedOrigins("*")
        .withSockJS()
    }
    
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
      registration.interceptors(AuthChannelInterceptor())
    }
  }
  ```

- [ ] **Sub-task 4.1.2**: Create WebSocket authentication interceptor
  ```kotlin
  // AuthChannelInterceptor.kt
  class AuthChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider
  ) : ChannelInterceptor {
    
    override fun preSend(
      message: Message<*>,
      channel: MessageChannel
    ): Message<*>? {
      val accessor = MessageHeaderAccessor.getAccessor(
        message,
        StompHeaderAccessor::class.java
      )
      
      if (StompCommand.CONNECT == accessor?.command) {
        val token = accessor.getFirstNativeHeader("Authorization")
        if (token != null && jwtTokenProvider.validateToken(token)) {
          val username = jwtTokenProvider.getUsernameFromToken(token)
          val principal = UsernamePasswordAuthenticationToken(
            username, null, emptyList()
          )
          accessor.user = principal
        }
      }
      
      return message
    }
  }
  ```

### Task 4.2: WebSocket Message Handler

- [ ] **Sub-task 4.2.1**: Create message notification service
  ```kotlin
  // NotificationWebSocketService.kt
  @Service
  class NotificationWebSocketService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val messageService: MessageService
  ) {
    
    fun notifyNewMessage(message: OutboundSms) {
      val dto = messageService.convertToDto(message)
      
      // Send to specific user
      messagingTemplate.convertAndSendToUser(
        message.person?.username ?: "",
        "/queue/messages",
        dto
      )
      
      // Broadcast high priority alerts to topic
      if (message.alertLevel >= 2) {
        messagingTemplate.convertAndSend(
          "/topic/alerts",
          dto
        )
      }
    }
    
    fun sendHeartbeat() {
      messagingTemplate.convertAndSend(
        "/topic/heartbeat",
        mapOf("timestamp" to System.currentTimeMillis())
      )
    }
  }
  ```

- [ ] **Sub-task 4.2.2**: Create WebSocket controller
  ```kotlin
  // WebSocketController.kt
  @Controller
  class WebSocketController(
    private val notificationService: NotificationWebSocketService
  ) {
    
    @MessageMapping("/subscribe")
    @SendToUser("/queue/subscribed")
    fun subscribe(principal: Principal): Map<String, Any> {
      return mapOf(
        "status" to "subscribed",
        "user" to principal.name,
        "timestamp" to System.currentTimeMillis()
      )
    }
    
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    fun ping(): Map<String, Any> {
      return mapOf(
        "status" to "pong",
        "timestamp" to System.currentTimeMillis()
      )
    }
    
    @Scheduled(fixedDelay = 30000)
    fun sendHeartbeat() {
      notificationService.sendHeartbeat()
    }
  }
  ```

## Phase 5: Service Layer Implementation

### Task 5.1: Core Services

- [ ] **Sub-task 5.1.1**: Implement message service
  ```kotlin
  // MessageService.kt
  @Service
  @Transactional(readOnly = true)
  class MessageService(
    private val outboundSmsRepository: OutboundSmsRepository,
    private val dateTimeUtils: DateTimeUtils
  ) {
    
    fun getUserMessages(
      userId: Int,
      pageable: Pageable,
      after: LocalDateTime?
    ): Page<MessageDto> {
      val messages = if (after != null) {
        outboundSmsRepository
          .findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(
            userId,
            after
          )
          .let { Page.empty<OutboundSms>() } // Convert to Page
      } else {
        outboundSmsRepository
          .findByPersonPersonIdOrderByDateRequestedDesc(userId, pageable)
      }
      
      return messages.map { convertToDto(it) }
    }
    
    fun convertToDto(sms: OutboundSms): MessageDto {
      // Adjust for timezone (UTC+8)
      val adjustedDate = dateTimeUtils.adjustFromDatabaseTime(sms.dateRequested)
      
      return MessageDto(
        id = sms.smsId,
        message = sms.message,
        alertLevel = sms.alertLevel,
        dateRequested = adjustedDate,
        dateSent = sms.dateSent?.let { dateTimeUtils.adjustFromDatabaseTime(it) },
        status = sms.status,
        mobileNo = sms.mobileNo,
        requestedBy = sms.requestedBy
      )
    }
    
    fun getUnreadCount(userId: Int): Long {
      return outboundSmsRepository.countByPersonPersonIdAndDateSentIsNull(userId)
    }
  }
  ```

- [ ] **Sub-task 5.1.2**: Implement notification service
  ```kotlin
  // NotificationService.kt
  @Service
  class NotificationService(
    private val webSocketService: NotificationWebSocketService,
    private val outboundSmsRepository: OutboundSmsRepository,
    private val objectMapper: ObjectMapper
  ) {
    
    fun processNewMessage(jsonPayload: String) {
      try {
        val notification = objectMapper.readValue(
          jsonPayload,
          OutboundSmsNotification::class.java
        )
        
        val message = outboundSmsRepository.findById(notification.smsId)
          .orElse(null) ?: return
        
        // Send via WebSocket
        webSocketService.notifyNewMessage(message)
        
        // Log notification
        logger.info("Processed notification for SMS ID: ${notification.smsId}")
      } catch (e: Exception) {
        logger.error("Error processing notification: ${e.message}", e)
      }
    }
    
    companion object {
      private val logger = LoggerFactory.getLogger(NotificationService::class.java)
    }
  }
  ```

### Task 5.2: Utility Services

- [ ] **Sub-task 5.2.1**: Create datetime utility
  ```kotlin
  // DateTimeUtils.kt
  @Component
  class DateTimeUtils {
    
    // Database stores timestamps in UTC+8 without timezone info
    private val DATABASE_OFFSET_HOURS = 8L
    
    fun adjustFromDatabaseTime(localDateTime: LocalDateTime): LocalDateTime {
      // Subtract 8 hours to get actual UTC time
      return localDateTime.minusHours(DATABASE_OFFSET_HOURS)
    }
    
    fun adjustToDatabaseTime(localDateTime: LocalDateTime): LocalDateTime {
      // Add 8 hours when writing to database
      return localDateTime.plusHours(DATABASE_OFFSET_HOURS)
    }
    
    fun formatRelativeTime(dateTime: LocalDateTime): String {
      val now = LocalDateTime.now()
      val duration = Duration.between(dateTime, now)
      
      return when {
        duration.toMinutes() < 1 -> "just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
        duration.toHours() < 24 -> "${duration.toHours()} hours ago"
        duration.toDays() < 7 -> "${duration.toDays()} days ago"
        else -> dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
      }
    }
  }
  ```

## Phase 6: Testing Implementation

### Task 6.1: Unit Tests

- [ ] **Sub-task 6.1.1**: Create service tests
  ```kotlin
  // AuthServiceTest.kt
  @ExtendWith(MockKExtension::class)
  class AuthServiceTest {
    
    @MockK
    private lateinit var personRepository: PersonRepository
    
    @MockK
    private lateinit var jwtTokenProvider: JwtTokenProvider
    
    @MockK
    private lateinit var passwordEncoder: PasswordEncoder
    
    @InjectMockKs
    private lateinit var authService: AuthService
    
    @Test
    fun `should authenticate with valid credentials`() {
      // Given
      val username = "testuser"
      val password = "password"
      val person = Person(
        personId = 1,
        username = username,
        password = password,
        name = "Test User"
      )
      
      every { personRepository.findByUsernameAndActive(username, true) } returns person
      every { jwtTokenProvider.generateToken(username, 1) } returns "token"
      
      // When
      val result = authService.authenticate(username, password)
      
      // Then
      assertThat(result.token).isEqualTo("token")
      assertThat(result.username).isEqualTo(username)
      verify { personRepository.findByUsernameAndActive(username, true) }
    }
  }
  ```

### Task 6.2: Integration Tests

- [ ] **Sub-task 6.2.1**: Create controller integration tests
  ```kotlin
  // AuthControllerIntegrationTest.kt
  @SpringBootTest
  @AutoConfigureMockMvc
  @TestContainers
  class AuthControllerIntegrationTest {
    
    @Container
    val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
      withDatabaseName("mrr_test")
      withUsername("mrr_user")
      withPassword("test_password")
    }
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should login successfully`() {
      // Given
      val request = LoginRequest("testuser", "password")
      
      // When & Then
      mockMvc.perform(
        post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(ObjectMapper().writeValueAsString(request))
      )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.token").exists())
      .andExpect(jsonPath("$.username").value("testuser"))
    }
  }
  ```

### Task 6.3: WebSocket Tests

- [ ] **Sub-task 6.3.1**: Create WebSocket integration tests
  ```kotlin
  // WebSocketIntegrationTest.kt
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  class WebSocketIntegrationTest {
    
    @LocalServerPort
    private var port: Int = 0
    
    private lateinit var stompClient: WebSocketStompClient
    private lateinit var stompSession: StompSession
    
    @BeforeEach
    fun setup() {
      stompClient = WebSocketStompClient(SockJsClient(
        listOf(WebSocketTransport(StandardWebSocketClient()))
      ))
      stompClient.messageConverter = MappingJackson2MessageConverter()
      
      val url = "ws://localhost:$port/ws"
      stompSession = stompClient.connect(url, object : StompSessionHandlerAdapter() {})
        .get(5, TimeUnit.SECONDS)
    }
    
    @Test
    fun `should receive message notification`() {
      // Test WebSocket message reception
    }
  }
  ```

## Phase 7: Error Handling and Monitoring

### Task 7.1: Global Exception Handling

- [ ] **Sub-task 7.1.1**: Create exception handler
  ```kotlin
  // GlobalExceptionHandler.kt
  @ControllerAdvice
  class GlobalExceptionHandler {
    
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse(
          error = "INVALID_CREDENTIALS",
          message = "Invalid username or password",
          timestamp = LocalDateTime.now()
        ))
    }
    
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(
          error = "RESOURCE_NOT_FOUND",
          message = ex.message ?: "Resource not found",
          timestamp = LocalDateTime.now()
        ))
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
      val errors = ex.bindingResult.fieldErrors.map { 
        "${it.field}: ${it.defaultMessage}"
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(
          error = "VALIDATION_ERROR",
          message = "Validation failed",
          details = errors,
          timestamp = LocalDateTime.now()
        ))
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
      logger.error("Unexpected error occurred", ex)
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse(
          error = "INTERNAL_ERROR",
          message = "An unexpected error occurred",
          timestamp = LocalDateTime.now()
        ))
    }
    
    companion object {
      private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
  }
  ```

### Task 7.2: Monitoring Setup

- [ ] **Sub-task 7.2.1**: Configure actuator endpoints
  ```kotlin
  // HealthIndicator.kt
  @Component
  class DatabaseHealthIndicator(
    private val dataSource: DataSource
  ) : HealthIndicator {
    
    override fun health(): Health {
      return try {
        dataSource.connection.use { connection ->
          if (connection.isValid(1)) {
            Health.up()
              .withDetail("database", "PostgreSQL")
              .withDetail("status", "Connected")
              .build()
          } else {
            Health.down()
              .withDetail("database", "PostgreSQL")
              .withDetail("status", "Connection invalid")
              .build()
          }
        }
      } catch (e: Exception) {
        Health.down()
          .withDetail("database", "PostgreSQL")
          .withDetail("error", e.message)
          .build()
      }
    }
  }
  ```

## Phase 8: Deployment and Documentation

### Task 8.1: API Documentation

- [ ] **Sub-task 8.1.1**: Configure OpenAPI/Swagger
  ```kotlin
  // OpenApiConfig.kt
  @Configuration
  class OpenApiConfig {
    
    @Bean
    fun openAPI(): OpenAPI {
      return OpenAPI()
        .info(Info()
          .title("MRR Alert API")
          .description("Backend API for MRR Alert notification system")
          .version("1.0.0")
          .contact(Contact()
            .name("MRR Support")
            .email("support@mrralert.com")
          )
        )
        .addSecurityItem(SecurityRequirement().addList("Bearer Authentication"))
        .components(Components()
          .addSecuritySchemes("Bearer Authentication",
            SecurityScheme()
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")
          )
        )
        .servers(listOf(
          Server().url("http://localhost:8080").description("Local server"),
          Server().url("https://api.mrralert.com").description("Production server")
        ))
    }
  }
  ```

### Task 8.2: Docker Deployment

- [ ] **Sub-task 8.2.1**: Create Dockerfile
  ```dockerfile
  # Dockerfile
  FROM amazoncorretto:17-alpine AS builder
  
  WORKDIR /app
  COPY mvnw .
  COPY .mvn .mvn
  COPY pom.xml .
  
  RUN ./mvnw dependency:go-offline -B
  
  COPY src src
  RUN ./mvnw clean package -DskipTests
  
  FROM amazoncorretto:17-alpine
  
  RUN apk add --no-cache curl
  
  WORKDIR /app
  
  COPY --from=builder /app/target/*.jar app.jar
  
  EXPOSE 8080
  
  HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1
  
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

- [ ] **Sub-task 8.2.2**: Create docker-compose.yml
  ```yaml
  # docker-compose.yml
  version: '3.8'
  
  services:
    mrr-sb:
      build:
        context: .
        dockerfile: Dockerfile
      ports:
        - "8080:8080"
      environment:
        - SPRING_PROFILES_ACTIVE=docker
        - DB_PASSWORD=${DB_PASSWORD}
        - JWT_SECRET=${JWT_SECRET}
        - DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/mrr
      networks:
        - mrr-network
      restart: unless-stopped
      healthcheck:
        test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
        interval: 30s
        timeout: 10s
        retries: 3
  
  networks:
    mrr-network:
      driver: bridge
  ```

## Commands Reference

### Development Commands

```bash
# Build project
./mvnw clean compile

# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run -Dspring.profiles.active=local

# Package application
./mvnw clean package

# Run with specific profile
java -jar target/mrr-sb-1.0.0.jar --spring.profiles.active=production

# Generate API documentation
./mvnw springdoc-openapi:generate

# Check for dependency updates
./mvnw versions:display-dependency-updates

# Docker commands
docker build -t mrr-sb:latest .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker mrr-sb:latest
docker-compose up -d
docker-compose logs -f mrr-sb
```

### Testing Commands

```bash
# Run unit tests only
./mvnw test -Dtest="*Test"

# Run integration tests only
./mvnw test -Dtest="*IntegrationTest"

# Run with coverage
./mvnw clean test jacoco:report

# Run specific test class
./mvnw test -Dtest="AuthServiceTest"

# Run tests with TestContainers
./mvnw test -Dspring.profiles.active=test
```

## Performance Targets

- **API Response Time**: < 200ms for standard endpoints
- **WebSocket Latency**: < 100ms for message delivery
- **Database Query Time**: < 50ms for indexed queries
- **Concurrent Users**: Support 1000+ concurrent WebSocket connections
- **Message Throughput**: Handle 100+ messages/second
- **JWT Token Validation**: < 10ms
- **Startup Time**: < 30 seconds
- **Memory Usage**: < 512MB heap under normal load

## Security Checklist

- [ ] JWT tokens with proper expiration
- [ ] CORS properly configured
- [ ] SQL injection prevention via JPA
- [ ] XSS protection headers
- [ ] Rate limiting on authentication endpoints
- [ ] Secure password storage (BCrypt)
- [ ] HTTPS enforcement in production
- [ ] Input validation on all endpoints
- [ ] Proper error message sanitization
- [ ] Database connection with minimal privileges (read-only)

## Success Metrics

- All tests passing with > 80% code coverage
- API documentation auto-generated and accessible
- WebSocket connections stable and reconnecting automatically
- Real-time notifications delivered within 100ms
- Proper timezone handling for database timestamps
- Docker container running successfully
- Health check endpoints responding correctly
- Graceful shutdown handling
- Proper logging and monitoring in place
- Support for both plain text and BCrypt passwords

## Notes

- Database is read-only, no write operations allowed
- Timestamps in database are stored in UTC+8 without timezone info
- Support both plain text and BCrypt passwords for migration period
- WebSocket uses STOMP protocol for easier client integration
- JWT tokens used for stateless authentication
- PostgreSQL LISTEN/NOTIFY for real-time updates
- Spring Boot DevTools enabled for faster development
- Actuator endpoints for production monitoring
- TestContainers for integration testing with real PostgreSQL