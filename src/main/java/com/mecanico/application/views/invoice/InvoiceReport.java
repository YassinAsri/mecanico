/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.views.invoice;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import org.springframework.context.annotation.Scope;

@PageTitle("Rapport")
@Route("Report")
@Menu(order = 8, icon = LineAwesomeIconUrl.COLUMNS_SOLID)
@Scope("prototype")
@VaadinSessionScope
public class InvoiceReport extends VerticalLayout {

    public InvoiceReport() {
        try {
            String parent = readFileFromTemplate("template/main.html");
            String reference = readFileFromTemplate("template/reference.html");
            String test = readFileFromTemplate("template/invoice.html");
            String html = parent.replace("${body}", reference);

            // Convert the HTML string into an InputStream
            InputStream inputStream = new ByteArrayInputStream(test.getBytes("UTF-8"));

            Html htl = new Html(inputStream);
       
            add(htl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Notification.show("Error encoding the HTML string to bytes.");
        }
    }

    private String readFileFromTemplate(String template) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(template);
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
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

}
