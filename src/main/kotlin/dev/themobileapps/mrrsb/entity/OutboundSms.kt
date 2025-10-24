package dev.themobileapps.mrrsb.entity

import jakarta.persistence.*
import java.time.LocalDateTime

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

    @Column(name = "date_requested", nullable = false)
    val dateRequested: LocalDateTime,

    @Column(name = "date_sent")
    val dateSent: LocalDateTime? = null,

    @Column(name = "mobile_no", nullable = false, length = 20)
    val mobileNo: String,

    @Column(name = "status", nullable = false, length = 50)
    val status: String,

    @Column(name = "requested_by", nullable = false, length = 100)
    val requestedBy: String
)
