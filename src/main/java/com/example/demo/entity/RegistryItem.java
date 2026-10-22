package com.example.demo.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Entity
@Table(name = "registry_items")
public class RegistryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The owner is represented by userId in API responses rather than a nested
    // User object, preventing a circular JSON graph.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false, length = 200)
    private String product;

    @Column(nullable = true, length = 100)
    private String batch;

    @OneToMany(mappedBy = "registryItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RecalledFood> recalledFoods;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /** Exposes the foreign-key value without serializing the whole User object. */
    @JsonProperty("userId")
    public Long getUserId() {
        return user == null ? null : user.getId();
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public List<RecalledFood> getRecalledFoods() {
        return recalledFoods;
    }

    public void setRecalledFoods(List<RecalledFood> recalledFoods) {
        this.recalledFoods = recalledFoods;
    }
}
