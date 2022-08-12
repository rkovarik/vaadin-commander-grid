package org.vaadin.ui.demo;

import java.io.File;
import java.io.FileFilter;

import javax.servlet.annotation.WebServlet;

import org.vaadin.addons.ui.grid.CommanderGrid;
import org.vaadin.addons.ui.grid.HierarchicalFileDataProvider;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.BeanPropertySet;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {

        // Initialize our new UI component
        HierarchicalFileDataProvider dataProvider = new HierarchicalFileDataProvider(new File("/"));

        CommanderGrid<File, FileFilter> grid = new CommanderGrid<>(BeanPropertySet.get(File.class), dataProvider).withFirstColumn("name");
        grid.addKeyAction(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.SHIFT, () -> grid.asSingleSelect().getSelectedItem().ifPresent(grid::goInto));
        grid.addKeyAction(ShortcutAction.KeyCode.BACKSPACE, ShortcutAction.ModifierKey.SHIFT, grid::goToParentFolder);

        TextField filterComponent = new TextField();
        filterComponent.addValueChangeListener(event -> dataProvider.setFilter(file -> file.getName().contains(event.getValue())));
        grid.getHeaderRow(0).getCell("name").setComponent(filterComponent);

        grid.focus();
        grid.setSizeFull();
        final VerticalLayout layout = new VerticalLayout();
        layout.addComponents(grid);
        layout.setSizeFull();
        setContent(layout);
    }
}
