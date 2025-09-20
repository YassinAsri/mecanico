package com.mecanico.application.views.client;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.mecanico.application.entity.Client;
import com.mecanico.application.services.ClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;

import java.util.List;
import org.springframework.context.annotation.Scope;

@PageTitle("Klanten")
@Route("Client")
@Menu(order = 0, icon = LineAwesomeIconUrl.PERSON_BOOTH_SOLID)
@Scope("prototype")
@VaadinSessionScope
public class ClientCrudView extends VerticalLayout {

    private final ClientService clientService;
    private final Grid<Client> grid = new Grid<>(Client.class);
    private final TextField nameField = new TextField("Naam");
    private final TextField addressField = new TextField("Adres");
    private final TextField zipField = new TextField("Postcode");
    private final TextField cityField = new TextField("Plaats");
    private final TextField emailField = new TextField("Email");
    private final TextField phoneNrField = new TextField("Telefoon nr.");
    // Hidden ID field
    private final TextField idField = new TextField("ID");
    private final Button saveButton = new Button("Opslaan");
    private final Button newButton = new Button("Nieuw");  // New button
    private final TextField filterField = new TextField("Filter op naam");

    public ClientCrudView(ClientService clientService) {
        this.clientService = clientService;

        setSizeFull();
        emailField.setWidth("400px");
        zipField.setWidth("400px");
        phoneNrField.setWidth("100%");
        cityField.setWidth("100%");
        
        HorizontalLayout phoneHl = new HorizontalLayout(emailField,  phoneNrField);
        HorizontalLayout NameHl = new HorizontalLayout(nameField, phoneHl);
        HorizontalLayout postHl = new HorizontalLayout(zipField, cityField);
        HorizontalLayout address = new HorizontalLayout(addressField, phoneHl);
        // Form Layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, phoneHl, addressField, postHl);
        // Add the hidden ID field (will not be displayed)
        idField.setVisible(false); // Hide the ID field
        formLayout.add(idField);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveClient());
 
        newButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newButton.addClickListener(e -> newClient());  // Reset form to create a new client

        // Buttons Layout
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton,  newButton);

        // Filter setup
        //filterField.setPlaceholder("Filter op naam...");
        filterField.setWidth("500px");
        filterField.setClearButtonVisible(true);
        filterField.setValueChangeMode(ValueChangeMode.LAZY);  // Change to lazy mode
        filterField.addValueChangeListener(e -> updateGridFilter(e.getValue()));

        // Grid setup
        grid.addClassName("header-gray-background-grid");
        grid.setColumns("name", "address", "zip", "city", "email", "phoneNr");
        grid.getColumnByKey("name").setHeader("Klantnaam");
        grid.getColumnByKey("address").setHeader("Adres");
        grid.getColumnByKey("zip").setHeader("Postcode");
        grid.getColumnByKey("city").setHeader("Plaats");
        grid.getColumnByKey("email").setHeader("Email");
        grid.getColumnByKey("phoneNr").setHeader("Telefoon nr.");
        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(e -> updateForm(e.getValue()));

        // Layout Setup
        //add(formLayout, buttonLayout, filterField, grid);
        // Layout Setup
        add(formLayout, buttonLayout, filterField, grid);

        updateGrid();
        updateGrid();
    }

    private void saveClient() {
        if(nameField.getValue() == null || nameField.getValue().trim().equals("")){
            Notification.show("Klantnaam is vereist!", 3000, Notification.Position.BOTTOM_START);
            return;
        }
        Client client = new Client();
        client.setName(nameField.getValue());
        client.setAddress(addressField.getValue());
        client.setZip(zipField.getValue());
        client.setCity(cityField.getValue());
        client.setEmail(emailField.getValue());
        client.setPhoneNr(phoneNrField.getValue());
        // If the ID field is not empty, this means we're updating an existing client
        if (!idField.getValue().isEmpty()) {
            client.setId(Long.parseLong(idField.getValue())); // Set the client ID from the hidden field
        }

        try {
            clientService.save(client);
            // Show a success notification
            Notification.show("Klant is toegevoegd!", 3000, Notification.Position.BOTTOM_START);
            updateGrid();
            clearForm();
        } catch (Exception e) {
            // Show an error notification if something goes wrong
            Notification.show("Fout bij het opslaan van de klant.", 3000, Notification.Position.BOTTOM_START);
        }
    }

    private void deleteClient() {
        Client client = grid.asSingleSelect().getValue();
        if (client != null) {
            clientService.delete(client);
            updateGrid();
            clearForm();
        }
    }

    private void updateForm(Client client) {
        if (client != null) {
            nameField.setValue(client.getName());
            addressField.setValue(client.getAddress() != null ? client.getAddress() : "");
            zipField.setValue(client.getZip() != null ? client.getZip() : "");
            cityField.setValue(client.getCity() != null ? client.getCity() : "");
            emailField.setValue(client.getEmail() != null ? client.getEmail() : "");
            phoneNrField.setValue(client.getPhoneNr() != null ? client.getPhoneNr() : "");
            // Set the ID field (which is hidden) with the client's ID
            idField.setValue(String.valueOf(client.getId()));
        } else {
            clearForm();
        }
    }

    private void clearForm() {
        nameField.clear();
        addressField.clear();
        zipField.clear();
        cityField.clear();
        emailField.clear();
        phoneNrField.clear();
        idField.clear();
    }

    private void newClient() {
        clearForm();  // Clear the form fields for a new entry
    }

    private void updateGrid() {
        List<Client> clients = clientService.findAll();
        grid.setItems(clients);
    }

    // Update grid filter based on the text entered in the filter field
    private void updateGridFilter(String filter) {
        ListDataProvider<Client> dataProvider = (ListDataProvider<Client>) grid.getDataProvider();

        // Apply filter (filter by client name as an example)
        dataProvider.setFilter(client -> client.getName().toLowerCase().contains(filter.toLowerCase()));
    }
}