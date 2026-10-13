package com.example.demo.entity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "registry_items")
public class RegistryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String product;

    @Column(nullable = true)
    private String batch;

    @OneToMany(mappedBy = "registryItem", cascade = CascadeType.ALL, orphanRemoval = true)
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
