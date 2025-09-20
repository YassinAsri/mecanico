package com.mecanico.application.views.expense;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.mecanico.application.entity.Expense;
import com.mecanico.application.services.ExpenseService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.beans.factory.annotation.Value;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import jakarta.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@PageTitle("Uitgaven")
@Route("expenses")
@Menu(order = 8, icon = LineAwesomeIconUrl.TH_SOLID)
@Scope("prototype")
@VaadinSessionScope
public class ExpenseCrudView extends VerticalLayout {

    @Value("${app.vat.percentage}")
    private String defaultVatPercentage;

    private final ExpenseService expenseService;
    private final Grid<Expense> grid = new Grid<>(Expense.class);
    private final DatePicker dateField = new DatePicker("Datum");
    private final TextField bonField = new TextField("Bon Nr");
    private final TextField descField = new TextField("Omschrijving");
    private final TextField priceField = new TextField("Price inc BTW");
    private final TextField btwField = new TextField("BTW %");
    private final TextField btwPriceField = new TextField("Price exc BTW");
    private final TextField btwPr = new TextField("BTW bedrag");
    // Hidden ID field
    private final TextField idField = new TextField("ID");
    private final Button saveButton = new Button("Opslaan");
    private final Button newButton = new Button("Nieuw");
    private final Button deleteButton = new Button("Verwijder");
    private final TextField filterField = new TextField("Filter op naam");
    private final DatePicker startDate = new DatePicker("Van");
    private final DatePicker endDate = new DatePicker("Tot");
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("nl-NL"));

    public ExpenseCrudView(ExpenseService expenseService) {
        this.expenseService = expenseService;
        dateField.setLocale(new java.util.Locale("nl", "NL"));
        bonField.setWidth("150px");
        descField.setWidth("400px");
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        setSizeFull();

        // Form Layout
        HorizontalLayout formLayout = new HorizontalLayout();
        formLayout.add(dateField, bonField, descField, priceField, btwField, btwPriceField, btwPr);
        // Add the hidden ID field (will not be displayed)
        idField.setVisible(false); // Hide the ID field
        formLayout.add(idField);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveExpense());

        newButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newButton.addClickListener(e -> newExpense());
        deleteButton.addClickListener(e -> deleteExpense());
        // Buttons Layout
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, newButton, deleteButton);

        filterField.setWidth("500px");
        filterField.setClearButtonVisible(true);
        filterField.setValueChangeMode(ValueChangeMode.LAZY); // Using lazy value change mode

        // Add value change listeners to update the grid filter as the user types
        filterField.addValueChangeListener(e -> updateGridFilter());
        startDate.addValueChangeListener(e -> updateGridFilter());
        endDate.addValueChangeListener(e -> updateGridFilter());

        HorizontalLayout searchLayout = new HorizontalLayout(filterField, startDate, endDate);
        searchLayout.setWidthFull();
        searchLayout.setAlignItems(Alignment.BASELINE);
        searchLayout.setSpacing(true);

        // Grid setup
        grid.setColumns("date", "invNr", "description");
        grid.getColumnByKey("date").setHeader("DATUM");
        grid.getColumnByKey("invNr").setHeader("FACTUUR NR");
        grid.getColumnByKey("description").setHeader("OMSCHRIJVING");
        
        grid.addColumn(invoice -> {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.'); // Set the dot as the decimal separator
            DecimalFormat df = new DecimalFormat("#.00", symbols); // Format with 2 decimal places
            return df.format(invoice.getPrice());
        })
        .setHeader("PRIJS (INC BTW)")
        .setAutoWidth(true)
        .setSortProperty("price"); 
              
        grid.addColumn(invoice -> {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.'); // Set the dot as the decimal separator
            DecimalFormat df = new DecimalFormat("#.00", symbols); // Format with 2 decimal places
            return df.format(invoice.getBtwPercentage());
        })
        .setHeader("BTW %")
        .setAutoWidth(true)
        .setSortProperty("btwPercentage"); 
        
         grid.addColumn(invoice -> {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.'); // Set the dot as the decimal separator
            DecimalFormat df = new DecimalFormat("#.00", symbols); // Format with 2 decimal places
            return df.format(invoice.getBtwPrice());
        })
        .setHeader("PRIJS (EXC BTW)")
        .setAutoWidth(true)
        .setSortProperty("btwPrice"); 
         
         grid.addColumn(invoice -> {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.'); // Set the dot as the decimal separator
            DecimalFormat df = new DecimalFormat("#.00", symbols); // Format with 2 decimal places
            return df.format(invoice.getBtwValue());
        })
        .setHeader("BTW BEDRAG")
        .setAutoWidth(true)
        .setSortProperty("btwValue"); 
      
        
        grid.addClassName("header-gray-background-grid");

        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(e -> updateForm(e.getValue()));

        priceField.addValueChangeListener(e -> calculateBtwPrice());
        btwField.addValueChangeListener(e -> calculateBtwPrice());

        // Layout Setup
        add(formLayout, buttonLayout, searchLayout, grid);
        updateGrid();
    }

    @PostConstruct
    public void init() {
        try {
            double vatPercentage = Double.parseDouble(defaultVatPercentage);
            btwField.setValue(formatNumber(vatPercentage));
        } catch (NumberFormatException e) {
            btwField.setValue("0.00");
            Notification.show("Ongeldig BTW-percentage in configuratie.", 3000, Notification.Position.BOTTOM_START);
        }
    }

    private void saveExpense() {
        try {
            if (descField.getValue() == null || descField.getValue().trim().equals("")) {
                Notification.show("Omschrijving is vereist!", 3000, Notification.Position.BOTTOM_START);
                return;
            }
            double price = Double.parseDouble(priceField.getValue());
            double btw = Double.parseDouble(btwField.getValue());
            double btwValue = Double.parseDouble(btwPr.getValue());
            double cal = 1 + (btw/100);
            double btwPrice = price / cal;
            Expense expense = new Expense();
            expense.setDate(dateField.getValue());
            expense.setInvNr(bonField.getValue());
            expense.setDescription(descField.getValue());
            expense.setPrice(price);
            expense.setBtwPercentage(btw);
            expense.setBtwPrice(btwPrice);
            expense.setBtwValue(btwValue);
            if (!idField.getValue().isEmpty()) {
                expense.setId(Long.parseLong(idField.getValue())); // Set the client ID from the hidden field
            }

            expenseService.save(expense);
            // Show a success notification
            Notification.show("Uitgave is toegevoegd!", 3000, Notification.Position.BOTTOM_START);
            updateGrid();
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            // Show an error notification if something goes wrong
            Notification.show("Fout bij het opslaan van de uitgave.", 3000, Notification.Position.BOTTOM_START);
        }
    }

    private void deleteExpense() {
        Expense expense = grid.asSingleSelect().getValue();

        if (expense != null) {
            // Create the confirmation dialog
            Dialog confirmDialog = new Dialog();
            // Create the confirmation message
            Div message = new Div();
            message.setText("Weet je zeker dat je deze uitgave wilt verwijderen?");
            // Create the 'Cancel' button
            Button cancelButton = new Button("Annuleren", event -> confirmDialog.close());
            Button deleteButton = new Button("Verwijderen", event -> {
                expenseService.delete(expense); // Perform the delete action
                updateGrid(); // Update the grid
                clearForm(); // Clear the form
                confirmDialog.close(); // Close the dialog
                Notification.show("Uitgave is verwijderd!", 3000, Notification.Position.BOTTOM_START);
            });
            // Add components to the dialog
            confirmDialog.add(message, cancelButton, deleteButton);

            // Optional: Add some styling
            confirmDialog.setWidth("500px");
            confirmDialog.setHeight("100px");

            // Open the confirmation dialog
            confirmDialog.open();
        }
    }

    private void updateForm(Expense expense) {
        if (expense != null) {
            idField.setValue(String.valueOf(expense.getId()));
            dateField.setValue(expense.getDate());
            bonField.setValue(String.valueOf(expense.getInvNr()));
            descField.setValue(expense.getDescription());
            priceField.setValue(formatNumber(expense.getPrice()));
            btwField.setValue(formatNumber(expense.getBtwPercentage()));
            btwPriceField.setValue(formatNumber(expense.getBtwPrice()));
            btwPr.setValue(formatNumber(expense.getBtwValue()));
        } else {
            clearForm();
        }
    }

    private void clearForm() {
        idField.clear();
        dateField.clear();
        bonField.clear();
        descField.clear();
        priceField.clear();
        btwPriceField.clear();
        btwPr.clear();
    }

    private void newExpense() {
        clearForm();  // Clear the form fields for a new entry
    }

    private void updateGrid() {
        List<Expense> expenses = expenseService.findAll();
        grid.setItems(expenses);
    }

    private void updateGridFilter() {
        ListDataProvider<Expense> dataProvider = (ListDataProvider<Expense>) grid.getDataProvider();
        String filter = filterField.getValue();
        // Combine the filters into one
        dataProvider.setFilter(expense -> {
            boolean matchesDescription = (filter == null || filter.trim().isEmpty())
                    || expense.getDescription().toLowerCase().contains(filter.toLowerCase());
            boolean matchesStartDate = (startDate.getValue() == null) || expense.getDate().isAfter(startDate.getValue());
            boolean matchesEndDate = (endDate.getValue() == null) || expense.getDate().isBefore(endDate.getValue());

            // Combine the filters with AND logic
            return matchesDescription && matchesStartDate && matchesEndDate;
        });
    }

    private void calculateBtwPrice() {
        try {
            // Get the price and VAT percentage
            double price = Double.parseDouble(priceField.getValue());
            double vatPercentage = Double.parseDouble(btwField.getValue());
            double vatAmount = price / (1 + (vatPercentage/100));
            double btw = (vatAmount * vatPercentage) / 100;
            btwPriceField.setValue(formatNumber(vatAmount));
            btwPr.setValue(formatNumber(btw));
        } catch (NumberFormatException e) {
            //e.printStackTrace();
            btwPriceField.clear();
        }
    }
    
    private String formatNumber(double price) {
        String formatted = numberFormat.format(price);        
        formatted = formatted.replace(',', '.');        
        return formatted;
    }
}
