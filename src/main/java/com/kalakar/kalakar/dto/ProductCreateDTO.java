package com.kalakar.kalakar.dto;

import java.math.BigDecimal;

public class ProductCreateDTO {
    private String name;
    private String category;
    private String fabric;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String badge;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFabric() { return fabric; }
    public void setFabric(String fabric) { this.fabric = fabric; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
}
