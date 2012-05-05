package pl.softwaremill.asamal.example.model.ticket;

import pl.softwaremill.asamal.example.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Discounts for tickets
 *
 * User: szimano
 */
@Entity
@Table(name = "DISCOUNT")
public class Discount extends BaseEntity {

    @Column(name = "discount_code", unique = true, nullable = false)
    @NotNull
    private String discountCode;

    @Column(name = "discount_amount", nullable = false)
    @NotNull
    @Min(1) @Max(100)
    private Integer discountAmount;

    @Column(name = "number_of_uses", nullable = false)
    @NotNull
    private Integer numberOfUses;

    @Column(name = "late_discount")
    private String lateDiscount;

    @OneToMany(mappedBy = "discount")
    private Set<Invoice> invoices;

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getNumberOfUses() {
        return numberOfUses;
    }

    public void setNumberOfUses(Integer numberOfUses) {
        this.numberOfUses = numberOfUses;
    }

    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    public String getLateDiscount() {
        return lateDiscount;
    }

    public void setLateDiscount(String lateDiscount) {
        this.lateDiscount = lateDiscount;
    }
}
