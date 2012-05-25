package pl.softwaremill.asamal.example.service.ticket;

import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.json.ViewUsers;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Named("tickets")
public class TicketService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Messages messages;

    @Inject
    private ConfigurationBean configurationBean;

    @Transactional
    public List<TicketCategory> getTicketCategories() {
        return entityManager.createQuery("select t from TicketCategory t order by t.fromDate").getResultList();
    }

    @Transactional
    public void addTicketCategory(TicketCategory ticketCategory) throws TicketsExceededException {
        // check first if it won't be too much, once we add it
        Long maxAllowed = Long.parseLong(configurationBean.getProperty(Conf.TICKETS_MAX));

        Long allocatedTickets = countAllocatedTickets();

        if (allocatedTickets == null)
            allocatedTickets = 0l;

        long totalNewNumber = allocatedTickets + ticketCategory.getNumberOfTickets();

        if (maxAllowed < totalNewNumber) {
            throw new TicketsExceededException(messages.getFromBundle("tickets.number.exceeded",
                    maxAllowed - allocatedTickets));
        }

        entityManager.persist(ticketCategory);
    }

    @Transactional
    public TicketCategory loadCategory(Long id) {
        return entityManager.find(TicketCategory.class, id);
    }

    @Transactional
    public void updateTicketCategory(TicketCategory ticketCat) {
        entityManager.merge(ticketCat);
    }

    @Transactional
    public void deleteTicketCategory(Long id) {
        TicketCategory category = entityManager.find(TicketCategory.class, id);

        entityManager.remove(category);
    }

    @Transactional
    public Long countAllocatedTickets() {
        return (Long) entityManager.createQuery(
                "select sum(t.numberOfTickets) from TicketCategory t")
                .getSingleResult();
    }

    @Transactional
    public List<TicketCategory> getAvailableCategories() {
        return entityManager.createQuery(
                "select t from TicketCategory t where :now between t.fromDate and t.toDate order by t.name")
                .setParameter("now", new Date())
                .getResultList();
    }

    @Transactional
    public TicketCategory getTicketCategory(String name) {
        return (TicketCategory) entityManager.createQuery("select t from TicketCategory t where t.name = :name")
                .setParameter("name", name)
                .getSingleResult();
    }

    @Transactional
    public Integer getNumberOfBookedTicketsInCategory(TicketCategory ticketCategory) {
        return ((Long) entityManager.createQuery(
                "select count(t) from Ticket t where t.ticketCategory = :ticketCategory")
                .setParameter("ticketCategory", ticketCategory)
                .getSingleResult()).intValue();
    }

    @Transactional
    public void addInvoice(Invoice invoice) {
        entityManager.persist(invoice);
    }

    @Transactional
    public Invoice updateInvoice(Invoice invoice) {
        return entityManager.merge(invoice);
    }

    @Transactional
    public List<Invoice> getInvoicesForUser(User user) {
        return entityManager.createQuery("select i from Invoice i where i.user = :user").
                setParameter("user", user).getResultList();
    }

    public Invoice loadInvoice(Long invoiceId) {
        return entityManager.find(Invoice.class, invoiceId);
    }

    @Transactional
    public ViewUsers getAllSoldTickets(Integer pageNumber, Integer resultsPerPage) {
        List<String> optionLabels = entityManager.createQuery("select t.label from TicketOptionDefinition t")
                .getResultList();

        return new ViewUsers(entityManager.createQuery(
                "select new pl.softwaremill.asamal.example.model.json.ViewTicket(t) from Ticket t" +
                        " where t.invoice.status = :status order by t.invoice.datePaid, t.id")
                .setParameter("status", InvoiceStatus.PAID)
                .setFirstResult(pageNumber * resultsPerPage).
                        setMaxResults(resultsPerPage).getResultList(),
                optionLabels);
    }

    @Transactional
    public Long countAllSoldTickets() {
        return (Long) entityManager.createQuery(
                "select count(t) from Ticket t" +
                        " where t.invoice.status = :status order by t.invoice.datePaid, t.id")
                .setParameter("status", InvoiceStatus.PAID)
                .getSingleResult();
    }

    @Transactional
    public Long getSoldByCategory(TicketCategory category) {
        return (Long) entityManager.createQuery(
                "select count(t) from Ticket t " +
                        "where t.ticketCategory = :category and t.invoice.status = :status")
                .setParameter("status", InvoiceStatus.PAID)
                .setParameter("category", category)
                .getSingleResult();
    }

    @Transactional
    public Long getNotPaidByCategory(TicketCategory category) {
        return (Long) entityManager.createQuery(
                "select count(t) from Ticket t " +
                        "where t.ticketCategory = :category and t.invoice.status = :status")
                .setParameter("status", InvoiceStatus.UNPAID)
                .setParameter("category", category)
                .getSingleResult();
    }
}
