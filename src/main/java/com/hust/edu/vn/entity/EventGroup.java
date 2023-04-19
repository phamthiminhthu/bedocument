package com.hust.edu.vn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "event_group")
public class EventGroup {
    @Id
    @Column(name = "event_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupDoc group;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_stime", nullable = false)
    private Instant eventStime;

    @Column(name = "event_etime", nullable = false)
    private Instant eventEtime;

    @Column(name = "event_mode")
    private Byte eventMode;

    @Lob
    @Column(name = "event_address")
    private String eventAddress;

    @Lob
    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}