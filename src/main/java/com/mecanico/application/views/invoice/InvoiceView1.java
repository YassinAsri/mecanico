package com.mecanico.application.views.invoice;

import com.mecanico.application.entity.Client;
import com.mecanico.application.entity.Invoice;
import com.mecanico.application.entity.Reference;
import com.mecanico.application.services.ClientService;
import com.mecanico.application.services.EmailService;
import com.mecanico.application.services.InvoiceService;
import com.mecanico.application.services.ReferenceService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Scope;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Factuur")
@Route("invoice1/:invoiceId?/:action?(edit)")
@Menu(order = 5, icon = LineAwesomeIconUrl.SELLCAST)
@Scope("prototype")
@VaadinSessionScope
public class InvoiceView1 extends VerticalLayout implements BeforeEnterObserver {

    private final ClientService clientService;
    private final InvoiceService invoiceService;
    private final ReferenceService referenceService;
    private final EmailService emailService;

    // Form fields for invoice details
    private final DateTimePicker dateField = new DateTimePicker("Factuur datum");
    private final TextField invoiceNr = new TextField("Factuur nr");
    private final ComboBox<Client> clientComboBox = new ComboBox<>("Client");
    private final TextField plateNr = new TextField("Kenteken");
    private final TextField email = new TextField("Email");
    private final TextField type = new TextField("Type/Model");
    private final TextField telNr = new TextField("Telefoon nr");
    private final TextField km = new TextField("KM stand");

    private static int nrOfFields = 15;
    private TextField[] refIdFields = new TextField[nrOfFields];
    private TextField[] refNameFields = new TextField[nrOfFields];
    private ComboBox<String>[] refDescFields = new ComboBox[nrOfFields];
    private TextField[] refNumberFields = new TextField[nrOfFields];
    private TextField[] refPriceFields = new TextField[nrOfFields];
    private TextField[] refSubTotFields = new TextField[nrOfFields];

    private final TextField workDesc = new TextField("Aarbeidsomschrijving ex BTW");
    private final TextField workAmount = new TextField("Prijs");
    private final TextField priceExBtw = new TextField("Totaal prijs ex BTW (€)");
    private final TextField btw = new TextField("BTW %");
    private final TextField btwAmount = new TextField("BTW (€)");
    private final TextField priceIncBtw = new TextField("Totaal Prijs inc BTW (€)");

    private final Button saveButton = new Button("Opslaan");
    private final Button invButton = new Button("Factuur");
    private final Button emailButton = new Button("Factuur");
    private final Button cancelButton = new Button("Annuleren");

    private Invoice currentInvoice;
    private List<String> distinctReferences;

    // Initially empty list of references
    public InvoiceView1(ClientService clientService, InvoiceService invoiceService, ReferenceService referenceService, EmailService emailService) {
        this.clientService = clientService;
        this.invoiceService = invoiceService;
        this.referenceService = referenceService;
        this.emailService = emailService;

        distinctReferences = this.referenceService.getReferences();        
        setSizeFull();

        FormLayout invoiceForm = createGeneralInvoicePane();
        VerticalLayout girdLayout = createInvoiceGridPane();
        FormLayout workForm = createInvoiceWorkPane();
        HorizontalLayout buttonLayout = createButtonLayout();
        add(invoiceForm, girdLayout, workForm, buttonLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            if (event.getRouteParameters() != null && event.getRouteParameters().get("invoiceId") != null && event.getRouteParameters().get("invoiceId").isPresent()) {
                invoiceNr.setValue(event.getRouteParameters().get("invoiceId").get());
                currentInvoice = invoiceService.findById(Long.parseLong(event.getRouteParameters().get("invoiceId").get()));
                dateField.setLocale(new java.util.Locale("nl", "NL"));
                dateField.setValue(currentInvoice.getDate());
                telNr.setValue(currentInvoice.getClient().getPhoneNr());
                email.setValue(currentInvoice.getClient().getEmail());
                km.setValue(currentInvoice.getKm());
                plateNr.setValue(currentInvoice.getPlateNr());
                type.setValue(currentInvoice.getType());
                clientComboBox.setValue(currentInvoice.getClient());
                for (int i = 0; i < currentInvoice.getReferences().size(); i++) {
                    refIdFields[i + 1].setValue(String.valueOf(currentInvoice.getReferences().get(i).getId()));
                    refNameFields[i + 1].setValue(currentInvoice.getReferences().get(i).getReference());
                    refDescFields[i + 1].setValue(currentInvoice.getReferences().get(i).getName());
                    refNumberFields[i + 1].setValue(String.valueOf(currentInvoice.getReferences().get(i).getNumber()));
                    refPriceFields[i + 1].setValue(String.valueOf(currentInvoice.getReferences().get(i).getPrice()));
                    refSubTotFields[i + 1].setValue(String.valueOf(currentInvoice.getReferences().get(i).getSubtotal()));
                }
                priceExBtw.setValue(String.format(Locale.US, "%.2f", currentInvoice.getPriceExBtw()));
                btwAmount.setValue(String.format(Locale.US, "%.2f", currentInvoice.getPriceExBtw() * 21 / 100));
                priceIncBtw.setValue(String.format(Locale.US, "%.2f", currentInvoice.getPriceIncBtw()));
                workAmount.setValue(String.format(Locale.US, "%.2f", currentInvoice.getWorkAmount()));
                btw.setValue(String.format(Locale.US, "%.2f", currentInvoice.getBtwPercentage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notification errorNotification = new Notification("Fout " + e.getMessage());
            errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            errorNotification.setDuration(6000);
            errorNotification.setPosition(Notification.Position.MIDDLE);
            errorNotification.open();
        }

    }

    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, invButton, emailButton, cancelButton);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveInvoice());

        invButton.addClickListener(e -> show());
        invButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        //emailButton.addClickListener(e -> email());
        //emailButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addClickListener(e -> cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return buttonLayout;
    }

    private void cancel() {

    }

    private String readFileFromTemplate(String template) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(template);
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void show() {
        String main = readFileFromTemplate("template/invoice_main.html");
        String invoiceRef = readFileFromTemplate("template/invoice_ref.html");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = currentInvoice.getDate().format(formatter);
        main = main.replace("$DATE", formattedDate);
        main = main.replace("$INV_NR", String.valueOf(currentInvoice.getId()));
        main = main.replace("$CLIENT_NAME", currentInvoice.getClient().getName());
        main = main.replace("$CLIENT_ADDRESS", currentInvoice.getClient().getAddress());
        main = main.replace("$CLIENT_PC", currentInvoice.getClient().getZip() + " " + currentInvoice.getClient().getCity());

        String refContent = "";
        for (Reference reference : currentInvoice.getReferences()) {
            String ref = new String(invoiceRef);
            ref = ref.replace("$REF", reference.getReference());
            ref = ref.replace("$DES", reference.getName());
            ref = ref.replace("$NUMBER", String.valueOf(reference.getNumber()));
            ref = ref.replace("$PRICE", String.format(Locale.US, "%.2f", reference.getPrice()));
            ref = ref.replace("$SUB", String.format(Locale.US, "%.2f", reference.getSubtotal()));
            refContent += ref;
        }

        main = main.replace("$INV_REF", refContent);
        main = main.replace("$LOON", String.format(Locale.US, "%.2f", currentInvoice.getWorkAmount()));
        main = main.replace("$SUBTOTAL", String.format(Locale.US, "%.2f", currentInvoice.getPriceExBtw()));
        main = main.replace("$BTW_PERC", String.format(Locale.US, "%.2f", currentInvoice.getBtwPercentage()));
        main = main.replace("$BTW_AMOUNT", String.format(Locale.US, "%.2f", currentInvoice.getBtwPrice()));        
        main = main.replace("$TOTAL", String.format(Locale.US, "%.2f", currentInvoice.getPriceIncBtw()));

        createDialog(main);
    }

    private void createDialog(String content) {
        Dialog dialog = new Dialog();
        HtmlComponent htmlComponent = new HtmlComponent(Tag.DIV);
        htmlComponent.getElement().setProperty("innerHTML", content);
        Button closeButton = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        Button printButton = new Button(VaadinIcon.PRINT.create(), e -> printContent(htmlComponent));
        Button emailButton = new Button(VaadinIcon.PAPERPLANE.create(), e -> email(content));
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.add(printButton, emailButton, closeButton);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END); // Right-aligned buttons
        headerLayout.getStyle()
                .set("background-color", "white")
                .set("padding", "5px 0px") // Reduced padding to bring buttons closer to the top
                .set("border-radius", "10px 0px 0 0")
                .set("position", "sticky")
                .set("top", "0px")
                .set("z-index", "100") // Ensure it's above the content
                .set("box-shadow", "none") // No shadow for the header
                .set("margin", "0"); // Ensure no margin at the top to reduce space
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.add(htmlComponent);
        contentLayout.setHeightFull(); // Let the content take the available height
        contentLayout.getStyle().set("overflow-y", "auto").set("max-height", "calc(100vh - 160px)"); // Calculate height minus header and padding
        dialogLayout.add(headerLayout, contentLayout);
        dialog.setWidth("auto");
        dialog.setHeight("auto");
        dialog.getElement().getStyle().set("border-radius", "15px"); // Rounded corners
        dialog.getElement().getStyle().set("box-shadow", "0px 15px 30px rgba(0, 0, 0, 0.1)"); // Light shadow for floating effect
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void email(String content) {
        try {
            emailService.sendEmail(currentInvoice.getClient().getEmail(), "INVOICE", content);
            Notification.show("Email is verzonden.", 3000, Notification.Position.MIDDLE);
        } catch(Exception e) {
            Notification.show("Fout. Email is niet verzonden " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void printContent(HtmlComponent htmlComponent) {
        // Use JavaScript to print the content of the HtmlComponent only
        UI.getCurrent().getPage().executeJs("var content = $0.outerHTML;"
                + "var printWindow = window.open('', '', 'width=600,height=600');"
                + "printWindow.document.write('<html><head><style>' + document.querySelector('style').outerHTML + '</style></head><body>' + content + '</body></html>');"
                + "printWindow.document.close(); "
                + "printWindow.print();", htmlComponent.getElement());
    }

    private void showReport() {
        Dialog popupDialog = new Dialog();
        String test = readFileFromTemplate("template/invoice.html");
        try {
            InputStream inputStream = new ByteArrayInputStream(test.getBytes("UTF-8"));
            Html htmlContent = new Html(inputStream);
            popupDialog.add(htmlContent);
            popupDialog.setCloseOnEsc(true);
            popupDialog.setCloseOnOutsideClick(true);
            popupDialog.setWidth("1200px");  // Width in pixels
            popupDialog.setHeight("800px");
            popupDialog.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FormLayout createInvoiceWorkPane() {
        workAmount.setValueChangeMode(ValueChangeMode.EAGER);
        workAmount.addValueChangeListener(event -> setPrices());
        workDesc.setValue("Aarbeidsloon");
        priceExBtw.setEnabled(false);
        priceExBtw.getElement().getStyle().set("opacity", "1").set("background-color", "white").set("color", "black").set("font-weight", "normal");
        btw.setValue("21.00");
        btw.setEnabled(false);
        btwAmount.setEnabled(false);

        priceIncBtw.setWidthFull();
        priceIncBtw.setEnabled(false);

        HorizontalLayout btwLayout = new HorizontalLayout(priceExBtw, btw, btwAmount);
        FormLayout workForm = new FormLayout();
        workForm.add(workDesc, workAmount, btwLayout, priceIncBtw);
        return workForm;
    }

    private VerticalLayout createInvoiceGridPane() {
        VerticalLayout girdLayout = new VerticalLayout();
        girdLayout.setSpacing(false);
        girdLayout.getElement().getStyle()
                .set("border", "1px solid #000") // Black border for the grid
                .set("border-radius", "5px") // Optional rounded corners
                .set("padding", "10px") // Padding inside the grid
                .set("height", "400px") // Set height
                .set("overflow-y", "auto"); // Enable vertical scrolling

        for (int i = 0; i < nrOfFields; i++) {
            refIdFields[i] = new TextField();
            refNameFields[i] = new TextField();
            refDescFields[i] = new ComboBox<>();
            refDescFields[i].setItems(distinctReferences);
            refDescFields[i].setAllowCustomValue(true);
            final int index = i;
            refDescFields[i].addCustomValueSetListener(event -> {   
                refDescFields[index].setValue(event.getDetail());
            });
            refNumberFields[i] = new TextField();
            refPriceFields[i] = new TextField();
            refSubTotFields[i] = new TextField();

            // Set specific width for each colum
            refIdFields[i].setVisible(false);
            refNameFields[i].setWidth("150px"); // Set width for Ref Name
            refDescFields[i].setWidth("500px"); // Set width for Description
            refNumberFields[i].setWidth("150px"); // Set width for Ref Number
            refPriceFields[i].setWidth("150px"); // Set width for Price
            refSubTotFields[i].setWidth("100%"); // Make Sub Total take the remaining width

            // Set the fields as editable and remove background styling for non-header rows
            if (i == 0) {
                refIdFields[i].setValue("ID");
                refIdFields[i].setEnabled(false);
                //refIdFields[i].setWidth("20px");  // Set width for Ref ID
                refNameFields[i].setValue("REFERENTIE"); // Set width for Ref Name
                refNameFields[i].setEnabled(false);
                refNameFields[i].getElement().getStyle().set("background-color", "#e0f7fa");

                refDescFields[i].setValue("BESCHRIJVING"); // Set width for Description
                refDescFields[i].setEnabled(false);
                refDescFields[i].getElement().getStyle().set("background-color", "#e0f7fa");

                refNumberFields[i].setValue("AANTAL"); // Set width for Ref Number
                refNumberFields[i].setEnabled(false);
                refNumberFields[i].getElement().getStyle().set("background-color", "#e0f7fa");

                refPriceFields[i].setValue("PRIJS (€)"); // Set width for Price
                refPriceFields[i].setEnabled(false);
                refPriceFields[i].getElement().getStyle().set("background-color", "#e0f7fa");

                refSubTotFields[i].setValue("SUBTOTAAL (€) "); // Make Sub Total take the remaining width 
                refSubTotFields[i].setEnabled(false);
                refSubTotFields[i].getElement().getStyle().set("background-color", "#e0f7fa");
            } else {
                refNumberFields[i].setPattern("\\d+(\\.\\d{1,2})?"); // Only digits with up to 2 decimal places
                refPriceFields[i].setPattern("\\d+(\\.\\d{1,2})?");  // Only digits with up to 2 decimal places
                // Add ValueChangeListener to refNumberFields (quantity) and refPriceFields (price)
                refNumberFields[i].addValueChangeListener(event -> updatePrices(index));
                refPriceFields[i].addValueChangeListener(event -> updatePrices(index));
                refDescFields[i].addValueChangeListener(event -> {
                    String value = event.getValue();
                    refDescFields[index].setValue(value != null ? value.toUpperCase() : "");
                });
            }

            // Remove margin and padding from the TextFields
            refIdFields[i].getElement().getStyle().set("margin", "1px").set("padding", "1px");
            refNameFields[i].getElement().getStyle().set("margin", "1px").set("padding", "1px");
            refDescFields[i].getElement().getStyle().set("margin", "1px").set("padding", "1px");
            refNumberFields[i].getElement().getStyle().set("margin", "1px").set("padding", "1px");
            refPriceFields[i].getElement().getStyle().set("margin", "1px").set("padding", "1px");
            refSubTotFields[i].getElement().getStyle().set("margin", "1px").set("padding", "1px");

            // Create a HorizontalLayout for each row and set its margin and padding to zero
            HorizontalLayout horLayout = new HorizontalLayout(
                    refIdFields[i],
                    refNameFields[i],
                    refDescFields[i],
                    refNumberFields[i],
                    refPriceFields[i],
                    refSubTotFields[i]);

            horLayout.setWidthFull();  // Ensure HorizontalLayout occupies the full width
            horLayout.setSpacing(false);  // Enable spacing between fields
            horLayout.setMargin(false);  // Disable margin around the layout
            horLayout.getElement().getStyle().set("padding", "0").set("margin", "0");

            // Add 1px spacing between fields
            for (int j = 0; j < horLayout.getComponentCount(); j++) {
                horLayout.getComponentAt(j).getElement().getStyle().set("margin-right", "1px");
            }

            girdLayout.add(horLayout);  // Add the row to the grid
        }
        return girdLayout;
    }

    private void setPrices() {
        double exBtw = 0;
        for (int i = 1; i < nrOfFields; i++) {
            if (refSubTotFields[i].getValue() != null && !refSubTotFields[i].getValue().trim().equals("")) {
                exBtw += Double.parseDouble(refSubTotFields[i].getValue());
            }
        }
        if (workAmount.getValue() != null && !workAmount.getValue().trim().equals("")) {
            exBtw += Double.parseDouble(workAmount.getValue());
        }
        priceExBtw.setValue(String.format(Locale.US, "%.2f", exBtw));
        btwAmount.setValue(String.format(Locale.US, "%.2f", exBtw * 21 / 100));
        double incBtw = exBtw + exBtw * 21 / 100;
        priceIncBtw.setValue(String.format(Locale.US, "%.2f", incBtw));
    }

    private void updatePrices(int index) {
        String quantityStr = refNumberFields[index].getValue();
        String priceStr = refPriceFields[index].getValue();

        // Try to parse the values into doubles
        Double quantity = (quantityStr != null && !quantityStr.isEmpty()) ? Double.parseDouble(quantityStr) : null;
        Double price = (priceStr != null && !priceStr.isEmpty()) ? Double.parseDouble(priceStr) : null;

        // If both quantity and price are available, calculate the subtotal and set it
        if (quantity != null && price != null) {
            Double subtotal = quantity * price;
            refSubTotFields[index].setValue(String.format(Locale.US, "%.2f", subtotal)); // Format to 2 decimal places
        } else {
            // If either is missing, leave the subtotal empty
            refSubTotFields[index].setValue("");
        }
        setPrices();
    }

    private FormLayout createGeneralInvoicePane() {
        FormLayout invoiceForm = new FormLayout();
        clientComboBox.setWidth("500px");
        clientComboBox.setItems(clientService.findAll());
        clientComboBox.setItemLabelGenerator(Client::getName);
        clientComboBox.addValueChangeListener(event -> {
            Client selectedClient = event.getValue();
            if (selectedClient != null) {
                email.setValue(selectedClient.getEmail());
                telNr.setValue(selectedClient.getPhoneNr());
            } else {
                email.clear();
                telNr.clear();
            }
        });
        dateField.setLocale(new java.util.Locale("nl", "NL"));
        dateField.setValue(LocalDateTime.now());

        HorizontalLayout clientLayout = new HorizontalLayout();
        telNr.setWidth("100%");
        km.setWidth("100%");
        clientLayout.add(clientComboBox, email, telNr);
        HorizontalLayout carInfoLayout = new HorizontalLayout();
        carInfoLayout.add(plateNr, type, km);
        invoiceNr.setEnabled(false);
        invoiceForm.add(dateField, invoiceNr, clientLayout, carInfoLayout);

        return invoiceForm;
    }

    private String validate() {
        String error = null;
        if (clientComboBox.getValue() == null) {
            error = "Voer een geldige klant in\n";
        }
        if (dateField.getValue() == null) {
            error += "Voer een geldige datum in\n";
        }
        if (workAmount.getValue() == null || workAmount.getValue().trim().equals("")) {
            error += "Voer een geldige prijs in\n";
        }
        return error;
    }

    private void saveInvoice() {
        try {
            String validation = validate();
            if (validation != null) {
                Notification.show("Please fill in all fields." + validation, 3000, Notification.Position.BOTTOM_START);
                return;
            }

            if (currentInvoice == null) {
                currentInvoice = new Invoice();
            }

            currentInvoice.setClient(clientComboBox.getValue());
            currentInvoice.setDate(dateField.getValue());
            currentInvoice.setPlateNr(plateNr.getValue());
            currentInvoice.setType(type.getValue());
            currentInvoice.setKm(km.getValue());
            currentInvoice.setDescription(workDesc.getValue());
            currentInvoice.setPriceExBtw(Double.parseDouble(priceExBtw.getValue()));
            currentInvoice.setPriceIncBtw(Double.parseDouble(priceIncBtw.getValue()));
            currentInvoice.setWorkAmount(Double.parseDouble(workAmount.getValue()));
            currentInvoice.setBtwPercentage(Double.parseDouble(btw.getValue()));
            currentInvoice.setBtwPrice(Double.parseDouble(btwAmount.getValue()));
            for (int i = 1; i < nrOfFields; i++) {
                if (refNameFields[i].getValue() == null || refNameFields[i].getValue().trim().equals("")) {
                    continue;
                }
                Reference reference;
                if (currentInvoice == null || i > currentInvoice.getReferences().size()) {
                    reference = new Reference();
                    currentInvoice.getReferences().add(reference);
                } else {
                    reference = currentInvoice.getReferences().get(i - 1);
                }
                reference.setInvoice(currentInvoice);
                if (refIdFields[i].getValue() != null && !refIdFields[i].getValue().trim().equals("")) {
                    reference.setId(Long.parseLong(refIdFields[i].getValue().trim()));
                }
                reference.setName(refDescFields[i].getValue());
                reference.setReference(refNameFields[i].getValue());
                reference.setNumber(Integer.parseInt(refNumberFields[i].getValue().trim()));
                reference.setPrice(Double.parseDouble(refPriceFields[i].getValue().trim()));
                reference.setSubtotal(Double.parseDouble(refSubTotFields[i].getValue().trim()));

            }

            currentInvoice = invoiceService.save(currentInvoice);
            invoiceNr.setValue(String.valueOf(currentInvoice.getId()));
            Notification.show("Invoice saved successfully!", 3000, Notification.Position.BOTTOM_START);
            updateGrid(currentInvoice.getReferences());

        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Fout tijden opslaan " + e.getMessage(), 3000, Notification.Position.BOTTOM_START);
        }
    }

    private void updateGrid(List<Reference> references) {

        // Example logic for displaying invoices (can be customized)
        // List<Invoice> invoices = invoiceService.findAll(); 
        // referenceGrid.setItems(invoices); // Set items in the grid (add a proper list of invoices if necessary)
    }

}
