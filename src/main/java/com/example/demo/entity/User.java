package com.example.demo.entity;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; 

    // Google's stable OpenID Connect subject identifies the account without
    // storing a short-lived bearer or ID token in the database.
    @Column(name = "google_sub", length = 128, unique = true)
    @JsonIgnore
    private String googleSub;

    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    // Do not serialize the back-reference; otherwise User -> RegistryItem -> User
    // creates a recursive JSON response. Codex assisted with documenting this
    // serialization boundary while preserving the JPA relationship.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RegistryItem> registryItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("user")
    public String getUsername() {
        return username;
    }

    @JsonProperty("user")
    public void setUsername(String username) {
        this.username = username;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
