package com.mecanico.application.views.book;

import com.mecanico.application.entity.Book;
import com.mecanico.application.entity.Expense;
import com.mecanico.application.entity.Invoice;
import com.mecanico.application.services.EmailService;
import com.mecanico.application.services.ExpenseService;
import com.mecanico.application.services.InvoiceService;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@PageTitle("Kasboek")
@Route("book")
@Menu(order = 8, icon = LineAwesomeIconUrl.TH_SOLID)
@Scope("prototype")
@VaadinSessionScope
public class CashBook extends VerticalLayout {

    private final ExpenseService expenseService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    private final DatePicker startDate = new DatePicker("Van");
    private final DatePicker endDate = new DatePicker("Tot");
    private final Button searchButton = new Button("Zoeken");
    private final Button emailButton = new Button("Email");
    private String htmlContent = "";

    private final HtmlComponent htmlComponent = new HtmlComponent(Tag.DIV);

    public CashBook(ExpenseService expenseService, InvoiceService invoiceService, EmailService emailService) {
        this.expenseService = expenseService;
        this.invoiceService = invoiceService;
        this.emailService = emailService;

        configureComponents();
        layoutUI();
    }

    private void configureComponents() {
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        emailButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        startDate.setWidth("250px");
        endDate.setWidth("250px");
        searchButton.setWidth("100px");
        emailButton.setWidth("100px");

        htmlComponent.setWidthFull();

        searchButton.addClickListener(e -> search(startDate.getValue(), endDate.getValue()));
        emailButton.addClickListener(e -> email());
    }

    private void layoutUI() {
        HorizontalLayout formLayout = new HorizontalLayout(startDate, endDate, searchButton, emailButton);
        formLayout.setAlignItems(Alignment.BASELINE);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        add(formLayout, htmlComponent);
    }

    private void search(LocalDate start, LocalDate end) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.'); // Optional: adds thousands separators
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.'); // Optional: adds thousands separators
        DecimalFormat df = new DecimalFormat("#0.00", symbols);
        df.setDecimalSeparatorAlwaysShown(true);

        List<Book> books = getCashBook(start, end);

        StringBuilder html = new StringBuilder();
        html.append(getCSS());
        html.append("<div class='table-wrapper'>");
        html.append(buildTableHeader());

        for (Book book : books) {
            html.append(buildTableRow(book));
        }

        html.append(buildTotalsRow(books, df));
        html.append("</table></div>");

        try {
            htmlContent = html.toString();
            htmlComponent.getElement().setProperty("innerHTML", html.toString());
        } catch (Exception e) {
            showError("Fout: " + e.getMessage());
        }
    }

    private String buildTableHeader() {
        return """
            <table class='styled-table'>
                <tr>
                    <td></td>
                    <td colspan='6' align='center'><b>UITGAVEN</b></td>
                    <td colspan='6' align='center'><b>INKOMSTEN</b></td>
                </tr>
                <tr>
                    <td>Datum</td>
                    <td><b>ID</b></td>
                    <td class='omschrijving'><b>Omschrijving</b></td>
                    <td><b>Prijs Inc BTW</b></td>
                    <td><b>BTW%</b></td>
                    <td><b>Prijs Ex BTW</b></td>
                    <td><b>BTW Bedrag</b></td>
                    <td><b>Factuur Nr</b></td>
                    <td class='klant'><b>Klant</b></td>
                    <td><b>Prijs Inc BTW</b></td>
                    <td><b>BTW%</b></td>
                    <td><b>Prijs Ex BTW</b></td>
                    <td><b>BTW Bedrag</b></td>
                </tr>
        """;
    }

    private String buildTableRow(Book book) {
        return String.format("""
            <tr>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
                <td class='omschrijving' align='right'>%s</td>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
                <td class='klant' align='right'>%s</td>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
                <td align='right'>%s</td>
            </tr>
        """,
                book.getDate(),
                safe(book.getExpInvNr()),
                safe(book.getExpDescription()),
                format(book.getExpPrice()),
                format(book.getExpBtwPercentage()),
                format(book.getExpBtwPrice()),
                format(book.getExpBtwValue()),
                safe(book.getInvId()),
                safe(book.getInvClientName()),
                format(book.getInvPriceIncBtw()),
                format(book.getInvBtwPercentage()),
                format(book.getInvPriceExBtw()),
                format(book.getInvBtwValue())
        );
    }

    private String buildTotalsRow(List<Book> books, DecimalFormat df) {
        double totalExPrice = books.stream().mapToDouble(Book::getExpPrice).sum();
        double totalExBtwPrice = books.stream().mapToDouble(Book::getExpBtwPrice).sum();
        double totalExBtwValue = books.stream().mapToDouble(Book::getExpBtwValue).sum();
        double totalInPriceIncBtw = books.stream().mapToDouble(Book::getInvPriceIncBtw).sum();
        double totalInPriceExBtw = books.stream().mapToDouble(Book::getInvPriceExBtw).sum();
        double totalInBtwValue = books.stream().mapToDouble(Book::getInvBtwValue).sum();

        return String.format("""
            <tr>
                <td align='right'><b>Totaal</b></td><td></td><td></td>
                <td align='right'><b>%s</b></td><td></td>
                <td align='right'><b>%s</b></td>
                <td align='right'><b>%s</b></td><td></td><td></td>
                <td align='right'><b>%s</b></td><td></td>
                <td align='right'><b>%s</b></td>
                <td align='right'><b>%s</b></td>
            </tr>
            </table>
        """,
                df.format(totalExPrice),
                df.format(totalExBtwPrice),
                df.format(totalExBtwValue),
                df.format(totalInPriceIncBtw),
                df.format(totalInPriceExBtw),
                df.format(totalInBtwValue)
        );
    }

    private String getCSS() {
        return """
        <style>
            .table-wrapper {
                overflow-x: auto;
                margin-top: 20px;
                padding: 0;
                border: none;
                background-color: transparent;
            }

            .styled-table {
                border-collapse: collapse;
                width: 100%;
                font-family: Arial, sans-serif;
                font-size: 14px;
            }

            .styled-table td {
                border: 1px solid #ccc;
                padding: 8px;
                text-align: right;
            }

            .styled-table tr:nth-child(even) {
                background-color: #f2f2f2;
            }

            .styled-table tr:first-child td {
                background-color: #eaeaea;
                font-weight: bold;
                text-align: center;
            }

            .omschrijving {
                width: 250px;
                text-align: left;
                font-weight: normal;
            }

            .klant {
                width: 300px;
                text-align: left;
               font-weight: normal;
            }
        </style>
    """;
    }

    private List<Book> getCashBook(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseService.search(startDate, endDate);
        List<Invoice> invoices = invoiceService.search(startDate, endDate);
        List<Book> result = new ArrayList<>();

        while (!startDate.isAfter(endDate)) {
            final LocalDate currentDate = startDate;

            List<Expense> dailyExpenses = expenses.stream()
                    .filter(e -> e.getDate().equals(currentDate))
                    .collect(Collectors.toList());

            List<Invoice> dailyInvoices = invoices.stream()
                    .filter(i -> i.getDate().toLocalDate().equals(currentDate))
                    .collect(Collectors.toList());

            int max = Math.max(dailyExpenses.size(), dailyInvoices.size());

            for (int i = 0; i < max; i++) {
                Book book = new Book();
                if (i < dailyExpenses.size()) {
                    Expense e = dailyExpenses.get(i);
                    book.setDate(e.getDate());
                    book.setExpInvNr(String.valueOf(e.getId()));
                    book.setExpDescription(e.getDescription());
                    book.setExpPrice(e.getPrice());
                    book.setExpBtwPercentage(e.getBtwPercentage());
                    book.setExpBtwPrice(e.getBtwPrice());
                    book.setExpBtwValue(e.getBtwValue());
                }

                if (i < dailyInvoices.size()) {
                    Invoice inv = dailyInvoices.get(i);
                    book.setDate(inv.getDate().toLocalDate());
                    book.setInvClientName(inv.getClient().getName());
                    book.setInvId(inv.getId());
                    book.setInvPriceIncBtw(inv.getPriceIncBtw());
                    book.setInvBtwPercentage(inv.getBtwPercentage());
                    book.setInvPriceExBtw(inv.getPriceExBtw());
                    book.setInvBtwValue(inv.getBtwPrice());
                }

                result.add(book);
            }

            startDate = startDate.plusDays(1);
        }

        return result;
    }

    private void email() {
        try {
        if (htmlContent == null || htmlContent.isEmpty()) {
            Notification.show("Er is geen inhoud om te verzenden. Voer eerst een zoekopdracht uit.", 3000, Notification.Position.MIDDLE);
            return;
        }

        emailService.sendEmail("urmail@yahoo.com", "Kasboek Overzicht", htmlContent);
        Notification.show("Email is verzonden.", 3000, Notification.Position.MIDDLE);
    } catch (Exception e) {
        Notification.show("Fout. Email is niet verzonden: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
    }
    }

    private void showError(String message) {
        Notification notification = new Notification(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setDuration(5000);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.open();
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "";
    }

    private String format(double value) {
        return value != 0 ? df.format(value) : "";
    }
    
    private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator(',');
    symbols.setGroupingSeparator('.'); // Optional: for thousands separator
    return new DecimalFormat("#0.00", symbols);
}
    
    private final DecimalFormat df = createDecimalFormat();
}
