package dev.themobileapps.mrrsb.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "person")
data class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val personId: Int = 0,

    @Column(name = "username", unique = true, nullable = false, length = 100)
    val username: String,

    @Column(name = "password", length = 255)
    val password: String,

    @Column(name = "name", nullable = false, length = 200)
    val name: String,

    @Column(name = "mobile_no", length = 20)
    val mobileNo: String? = null,

    @Column(name = "active")
    val active: Boolean = true,

    @Column(name = "date_created")
    val dateCreated: LocalDateTime? = null,

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    val outboundMessages: List<OutboundSms> = emptyList()
)
