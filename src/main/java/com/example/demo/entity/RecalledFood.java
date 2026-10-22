package com.example.demo.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@Entity
@Table(name = "recalled_foods")
public class RecalledFood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The registry relationship is represented by registryItemId in API
    // responses rather than serializing the entire parent object.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_item_id")
    @JsonIgnore
    private RegistryItem registryItem;

    @Column(nullable = false, length = 200)
    private String product;

    @Column(nullable = false, length = 255)
    private String contaminant;

    @Column(name = "recall_date", nullable = false)
    private LocalDate recallDate;

    @Column(name = "manu_date", nullable = false)
    private LocalDate manuDate;

    @Column(length = 100)
    private String batch;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getContaminant() {
        return contaminant;
    }

    public void setContaminant(String contaminant) {
        this.contaminant = contaminant;
    }

    public LocalDate getRecallDate() {
        return recallDate;
    }

    public void setRecallDate(LocalDate recallDate) {
        this.recallDate = recallDate;
    }

    public LocalDate getManuDate() {
        return manuDate;
    }

    public void setManuDate(LocalDate manuDate) {
        this.manuDate = manuDate;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public RegistryItem getRegistryItem() {
        return registryItem;
    }

    public void setRegistryItem(RegistryItem registryItem) {
        this.registryItem = registryItem;
    }

    /** Exposes the foreign-key value without serializing the whole registry item. */
    @JsonProperty("registryItemId")
    public Long getRegistryItemId() {
        return registryItem == null ? null : registryItem.getId();
    }
}
