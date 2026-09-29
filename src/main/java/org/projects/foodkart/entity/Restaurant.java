package org.projects.foodkart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String city;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentActiveOrders = 0;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer ratingCount = 0;

    @Builder.Default
    private Boolean isActive = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "restaurant_pincodes", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "pin_code")
    @Builder.Default
    private Set<String> serviceablePinCodes = new HashSet<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
        item.setRestaurant(this);
    }

    public void removeMenuItem(MenuItem item) {
        menuItems.remove(item);
        item.setRestaurant(null);
    }

    public boolean canAcceptOrder() {
        return isActive && (currentActiveOrders < maxCapacity);
    }
}
