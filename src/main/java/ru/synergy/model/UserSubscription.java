package ru.synergy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_subscription")
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;

    @ElementCollection
    @CollectionTable(name = "subscription_payments", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "payment_id")
    private List<String> paymentIds = new ArrayList<>();
}
