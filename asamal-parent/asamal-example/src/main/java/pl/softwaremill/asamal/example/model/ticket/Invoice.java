package pl.softwaremill.asamal.example.model.ticket;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import pl.softwaremill.asamal.example.model.BaseEntity;
import pl.softwaremill.asamal.example.model.security.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "INVOICE")
public class Invoice extends BaseEntity{
    
    public static final int SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Ticket> tickets;

    @Column(name = "name", nullable = false)
    @NotNull
    @Size(min = 3)
    private String name;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "vat")
    private String vat;

    @Column(name = "address", nullable = false)
    @NotNull
    @Size(min = 3)
    private String address;

    @Column(name = "postal_code", nullable = false)
    @NotNull
    @Size(min = 3)
    private String postalCode;

    @Column(name = "city", nullable = false)
    @NotNull
    @Size(min = 3)
    private String city;

    @Column(name = "country", nullable = false)
    @NotNull
    @Size(min = 3)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "methods")
    private PaymentMethod method;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date_created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "date_paid")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datePaid;

    @Column(name = "due_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @ManyToOne
    private Discount discount;

    @Column(name = "editable", nullable = false)
    private Boolean editable;

    @Column(name = "invoice_number")
    private Long invoiceNumber;
    
    public Invoice() {
        editable = true;
    }

    public Invoice(Set<Ticket> tickets, String name, String companyName, String vat, String address,
                   String postalCode, String city, String country, InvoiceStatus status, PaymentMethod method, User user, Date dateCreated,
                   Date datePaid, Discount discount, Boolean editable, Long invoiceNumber) {
        this();

        this.tickets = tickets;
        this.name = name;
        this.companyName = companyName;
        this.vat = vat;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.status = status;
        this.method = method;
        this.user = user;
        this.dateCreated = dateCreated;
        this.datePaid = datePaid;
        this.discount = discount;
        this.editable = editable;
        this.dueDate = new Date(dateCreated.getTime() + SEVEN_DAYS);
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(Date datePaid) {
        this.datePaid = datePaid;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public Multimap<TicketCategory, Ticket> getTicketsByCategory() {
        Multimap<TicketCategory, Ticket> ret = HashMultimap.create();

        for (Ticket ticket : tickets) {
            ret.put(ticket.getTicketCategory(), ticket);
        }

        return ret;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Long getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Long invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "tickets=" + tickets +
                ", name='" + name + '\'' +
                ", companyName='" + companyName + '\'' +
                ", vat='" + vat + '\'' +
                ", address='" + address + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", status=" + status +
                ", method=" + method +
                ", user=" + user +
                ", dateCreated=" + dateCreated +
                ", datePaid=" + datePaid +
                ", dueDate=" + dueDate +
                ", discount=" + discount +
                ", editable=" + editable +
                ", invoiceNumber=" + invoiceNumber +
                '}';
    }
}
