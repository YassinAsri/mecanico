package com.mecanico.application.views.gridwithfilters;

import com.mecanico.application.entity.Invoice;
import com.mecanico.application.services.InvoiceService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Factuuroverzicht")
@Route("grid-with-filters")
@Menu(order = 2, icon = LineAwesomeIconUrl.BUY_N_LARGE)
@Uses(Icon.class)
@Scope("prototype")
@VaadinSessionScope
public class GridwithFiltersView extends Div {

    private Grid<Invoice> grid;
    private Filters filters;
    private final InvoiceService invoiceService;

    public GridwithFiltersView(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid());
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }
    
    

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<Invoice> {

        private final TextField clientName = new TextField("Klantnaam");
        private final TextField kenteken = new TextField("Kenteken");
        private final DatePicker startDate = new DatePicker("Datum");
        private final DatePicker endDate = new DatePicker();

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            //clientName.setPlaceholder("Klantnaam");
            startDate.setLocale(new java.util.Locale("nl", "NL"));
            endDate.setLocale(new java.util.Locale("nl", "NL"));

            // Action buttons
            Button resetBtn = new Button("Reseten");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                clientName.clear();
                kenteken.clear();
                startDate.clear();
                endDate.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Zoeken");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(clientName, kenteken, createDateRangeFilter(), actions);
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("Van");
            endDate.setPlaceholder("Tot");

            // For screen readers
            startDate.setAriaLabel("From date");
            startDate.setWidth("300px");
            endDate.setAriaLabel("To date");
            endDate.setWidth("300px");
            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setWidth("600px");
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        @Override
        public Predicate toPredicate(Root<Invoice> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!clientName.isEmpty()) {
                String lowerCaseFilter = clientName.getValue().toLowerCase();
                Predicate clientName = criteriaBuilder.like(criteriaBuilder.lower(root.get("client").get("name")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(clientName));
            }
            if (!kenteken.isEmpty()) {
                String databaseColumn = "plateNr";
                String ignore = "?";

                String lowerCaseFilter = ignoreCharacters(ignore, kenteken.getValue().toLowerCase());
                Predicate phoneMatch = criteriaBuilder.like(
                        ignoreCharacters(ignore, criteriaBuilder, criteriaBuilder.lower(root.get(databaseColumn))),
                        "%" + lowerCaseFilter + "%");
                predicates.add(phoneMatch);

            }
            if (startDate.getValue() != null) {
                String databaseColumn = "date";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
                        criteriaBuilder.literal(startDate.getValue())));
            }
            if (endDate.getValue() != null) {
                String databaseColumn = "date";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()),
                        root.get(databaseColumn)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(Invoice.class, false);
        grid.addClassName("header-gray-background-grid");
        // Default column for Invoice ID
        grid.addColumn("id").setHeader("Factuur nr").setAutoWidth(true);
        // Date column with custom format, preserving sorting
        grid.addColumn(invoice -> {
            if (invoice.getDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                return invoice.getDate().format(formatter);
            } else {
                return "";
            }
        }).setHeader("Factuurdatum").setAutoWidth(true).setSortProperty("date"); // Sort based on date

        // Client-related columns
        grid.addColumn(invoice -> invoice.getClient() != null ? invoice.getClient().getName() : "")
                .setHeader("Klantnaam")
                .setAutoWidth(true)
                .setSortProperty("client.name"); // Sort based on client name

        grid.addColumn(invoice -> invoice.getClient() != null ? invoice.getClient().getPhoneNr() : "")
                .setHeader("Klant tel. nr.")
                .setAutoWidth(true)
                .setSortProperty("client.phoneNr"); // Sort based on client phone number

        // Plate number column with custom display
        grid.addColumn(invoice -> invoice.getPlateNr() != null ? invoice.getPlateNr() : "")
                .setHeader("Kenteken")
                .setAutoWidth(true)
                .setSortProperty("plateNr"); // Sort based on plate number

        // Price excluding VAT column with custom format
        grid.addColumn(invoice -> {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.'); // Set the dot as the decimal separator
            DecimalFormat df = new DecimalFormat("#.00", symbols); // Format with 2 decimal places
            return df.format(invoice.getPriceExBtw());
        })
        .setHeader("Prijs ex BTW")
        .setAutoWidth(true)
        .setSortProperty("priceExBtw"); // Sort based on price excluding VAT

        // Price including VAT column with custom format
        grid.addColumn(invoice -> {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.'); // Set the dot as the decimal separator
            DecimalFormat df = new DecimalFormat("#.00", symbols); // Format with 2 decimal places
            return df.format(invoice.getPriceIncBtw());
        })
        .setHeader("Prijs inc BTW")
        .setAutoWidth(true)
        .setSortProperty("priceIncBtw"); // Sort based on price including VAT
        // Set the data for the grid with pagination and sorting
        grid.setItems(query -> invoiceService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        // Add double-click listener for navigating to the invoice details page
        grid.addItemDoubleClickListener(event -> {
            Invoice selectedInvoice = event.getItem();
            if (selectedInvoice != null) {
                Long invoiceId = selectedInvoice.getId();
                UI.getCurrent().navigate("invoice1/" + invoiceId + "/edit");
            }
        });

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
