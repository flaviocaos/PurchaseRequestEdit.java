package br.com.firsti.packages.purchase.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Date;

import br.com.firsti.packages.organization.entities.Collaborator;
import br.com.firsti.packages.product.entities.Product;
import br.com.firsti.packages.product.entities.ProductType;
import br.com.firsti.packages.stock.entities.Warehouse;

@Entity
@Table(schema = "purchase")
public class PurchaseRequest {

    public enum PurchaseRequestStatus {
        PENDING,
        PURCHASED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "requesterId", nullable = false, updatable = true)
    private Collaborator requester;

    @ManyToOne
    @JoinColumn(name = "warehouseId", nullable = false, updatable = true)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "productTypeId", nullable = false, updatable = true)
    private ProductType productType;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = true, updatable = true)
    private Product product;

    @Column(nullable = true, updatable = true)
    private String description;

    @Column(precision = 9, scale = 2, nullable = false, updatable = true)
    private BigDecimal quantity;

    @ManyToOne
    @JoinColumn(name = "purchaseId", nullable = true, updatable = true)
    private Purchase purchase;

    @Column(nullable = false, updatable = false)
    private Date creation;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "purchase."PurchaseRequestStatus" NOT NULL", updatable = true)
    private PurchaseRequestStatus status;

    public PurchaseRequest() {}

    public PurchaseRequest(Warehouse warehouse, ProductType productType, Product product, String description,
                           BigDecimal quantity) {

        if (warehouse == null) {
            throw new IllegalArgumentException("The 'warehouse' cannot be null.");
        }

        if (productType == null) {
            throw new IllegalArgumentException("The 'productType' cannot be null.");
        }

        if (product != null && !product.getType().equals(productType)) {
            throw new IllegalArgumentException("The 'product' type is not the same as the productType.");
        }

        if (quantity == null) {
            throw new IllegalArgumentException("The 'quantity' cannot be null.");
        }

        this.warehouse = warehouse;
        this.productType = productType;
        this.product = product;
        this.description = description;
        this.quantity = quantity;

        this.creation = new Date();
        this.status = PurchaseRequestStatus.PENDING;
    }

    public int getId() { return id; }
    public Warehouse getWarehouse() { return warehouse; }
    public ProductType getProductType() { return productType; }
    public Collaborator getRequester() { return requester; }
    public Product getProduct() { return product; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public Purchase getPurchase() { return purchase; }
    public PurchaseRequestStatus getStatus() { return status; }
    public Date getCreation() { return creation; }

    public void setStatus(PurchaseRequestStatus status) { this.status = status; }
    public void setRequester(Collaborator requester) { this.requester = requester; }
    public void setCreation(Date creation) { this.creation = creation; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public void setProductType(ProductType productType) { this.productType = productType; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public void setDescription(String description) { this.description = description; }
}
