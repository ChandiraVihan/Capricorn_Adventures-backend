package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProfitLossDashboardResponseDTO {

    private String month;
    private boolean monthToDate;
    private LocalDateTime generatedAt;
    private SummaryCardDTO revenue;
    private SummaryCardDTO costOfSales;
    private SummaryCardDTO grossMargin;
    private SummaryCardDTO netProfitPreTax;
    private SummaryCardDTO netProfitPostTax;
    private ProductBreakdownDTO productBreakdown;
    private TaxSummaryDTO taxSummary;
    private List<LineItemDTO> lineItems;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public boolean isMonthToDate() {
        return monthToDate;
    }

    public void setMonthToDate(boolean monthToDate) {
        this.monthToDate = monthToDate;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public SummaryCardDTO getRevenue() {
        return revenue;
    }

    public void setRevenue(SummaryCardDTO revenue) {
        this.revenue = revenue;
    }

    public SummaryCardDTO getCostOfSales() {
        return costOfSales;
    }

    public void setCostOfSales(SummaryCardDTO costOfSales) {
        this.costOfSales = costOfSales;
    }

    public SummaryCardDTO getGrossMargin() {
        return grossMargin;
    }

    public void setGrossMargin(SummaryCardDTO grossMargin) {
        this.grossMargin = grossMargin;
    }

    public SummaryCardDTO getNetProfitPreTax() {
        return netProfitPreTax;
    }

    public void setNetProfitPreTax(SummaryCardDTO netProfitPreTax) {
        this.netProfitPreTax = netProfitPreTax;
    }

    public SummaryCardDTO getNetProfitPostTax() {
        return netProfitPostTax;
    }

    public void setNetProfitPostTax(SummaryCardDTO netProfitPostTax) {
        this.netProfitPostTax = netProfitPostTax;
    }

    public ProductBreakdownDTO getProductBreakdown() {
        return productBreakdown;
    }

    public void setProductBreakdown(ProductBreakdownDTO productBreakdown) {
        this.productBreakdown = productBreakdown;
    }

    public TaxSummaryDTO getTaxSummary() {
        return taxSummary;
    }

    public void setTaxSummary(TaxSummaryDTO taxSummary) {
        this.taxSummary = taxSummary;
    }

    public List<LineItemDTO> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItemDTO> lineItems) {
        this.lineItems = lineItems;
    }

    public static class SummaryCardDTO {
        private String title;
        private BigDecimal actual;
        private BigDecimal budget;
        private BigDecimal variance;
        private String varianceColor;
        private BigDecimal monthOverMonthChangePercent;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public BigDecimal getActual() {
            return actual;
        }

        public void setActual(BigDecimal actual) {
            this.actual = actual;
        }

        public BigDecimal getBudget() {
            return budget;
        }

        public void setBudget(BigDecimal budget) {
            this.budget = budget;
        }

        public BigDecimal getVariance() {
            return variance;
        }

        public void setVariance(BigDecimal variance) {
            this.variance = variance;
        }

        public String getVarianceColor() {
            return varianceColor;
        }

        public void setVarianceColor(String varianceColor) {
            this.varianceColor = varianceColor;
        }

        public BigDecimal getMonthOverMonthChangePercent() {
            return monthOverMonthChangePercent;
        }

        public void setMonthOverMonthChangePercent(BigDecimal monthOverMonthChangePercent) {
            this.monthOverMonthChangePercent = monthOverMonthChangePercent;
        }
    }

    public static class ProductBreakdownDTO {
        private BigDecimal hotelRevenue;
        private BigDecimal adventureRevenue;
        private BigDecimal thirdPartyCommission;

        public BigDecimal getHotelRevenue() {
            return hotelRevenue;
        }

        public void setHotelRevenue(BigDecimal hotelRevenue) {
            this.hotelRevenue = hotelRevenue;
        }

        public BigDecimal getAdventureRevenue() {
            return adventureRevenue;
        }

        public void setAdventureRevenue(BigDecimal adventureRevenue) {
            this.adventureRevenue = adventureRevenue;
        }

        public BigDecimal getThirdPartyCommission() {
            return thirdPartyCommission;
        }

        public void setThirdPartyCommission(BigDecimal thirdPartyCommission) {
            this.thirdPartyCommission = thirdPartyCommission;
        }
    }

    public static class TaxSummaryDTO {
        private BigDecimal taxRatePercent;
        private BigDecimal taxAmount;
        private BigDecimal netProfitPreTax;
        private BigDecimal netProfitPostTax;

        public BigDecimal getTaxRatePercent() {
            return taxRatePercent;
        }

        public void setTaxRatePercent(BigDecimal taxRatePercent) {
            this.taxRatePercent = taxRatePercent;
        }

        public BigDecimal getTaxAmount() {
            return taxAmount;
        }

        public void setTaxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
        }

        public BigDecimal getNetProfitPreTax() {
            return netProfitPreTax;
        }

        public void setNetProfitPreTax(BigDecimal netProfitPreTax) {
            this.netProfitPreTax = netProfitPreTax;
        }

        public BigDecimal getNetProfitPostTax() {
            return netProfitPostTax;
        }

        public void setNetProfitPostTax(BigDecimal netProfitPostTax) {
            this.netProfitPostTax = netProfitPostTax;
        }
    }

    public static class LineItemDTO {
        private String category;
        private String label;
        private BigDecimal amount;

        public LineItemDTO() {
        }

        public LineItemDTO(String category, String label, BigDecimal amount) {
            this.category = category;
            this.label = label;
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
