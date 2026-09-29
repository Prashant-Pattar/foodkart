package org.projects.foodkart.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String description;
    private double price;
    private String catagory;
    private boolean available;
    @OneToMany(cascade ={CascadeType.ALL}, fetch = FetchType.EAGER)
    @Column(name = "restaurant_id")
    private long restaurantId;
}
