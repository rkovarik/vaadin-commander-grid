package org.vaadin.addons.ui.grid;

import com.vaadin.data.PropertySet;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.HierarchicalDataProvider;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * Uses performance of flat {@link com.vaadin.ui.Grid} (paging) to show hierarchical data by showing only one hierarchy level at time.
 * To be used with heavy {@link com.vaadin.data.provider.BackEndDataProvider} which is not optimized to show all children at once in a {@link com.vaadin.ui.TreeGrid}.
 */
public class CommanderGrid<T, F> extends Grid<T> {

    private HierarchicalDataProvider<T, Object> hierarchicalDataProvider;
    private CommanderGridDataProviderWrapper<T, Object> commanderGridDataProviderWrapper;

    public CommanderGrid(PropertySet<T> propertySet, HierarchicalDataProvider<T, F> hierarchicalDataProvider) {
        super(propertySet);
        setDataProvider(hierarchicalDataProvider);
        addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick()) {
                goInto(event.getItem());
            }
        });
    }

    public void addKeyAction(final int keyCode, final int modifier, Runnable runnable) {
        addShortcutListener(new ShortcutListener(null, keyCode, new int[modifier]) {
            @Override
            public void handleAction(Object sender, Object target) {
                runnable.run();
            }
        });
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        if (!(dataProvider instanceof HierarchicalDataProvider)) {
            throw new IllegalArgumentException("CommanderGrid only accepts hierarchical data providers");
        }
        hierarchicalDataProvider = (HierarchicalDataProvider<T, Object>) dataProvider;
        commanderGridDataProviderWrapper = new CommanderGridDataProviderWrapper<>(hierarchicalDataProvider);
        super.setDataProvider(commanderGridDataProviderWrapper);
    }

    public CommanderGrid<T, F> withFirstColumn(String columnId) {
        Column<T, ?> column = getColumn(columnId);
        removeColumn(column);
        addColumn(wrapFirstColumnValueProvider(column.getValueProvider()),
                (ValueProvider) column.getPresentationProvider(),
                new HtmlRenderer())
                .setId(column.getId())
                .setSortable(column.isSortable())
                .setCaption(column.getCaption())
                .setAssistiveCaption(column.getAssistiveCaption())
                .setDescriptionGenerator(column.getDescriptionGenerator())
                .setEditable(column.isEditable())
                .setExpandRatio(column.getExpandRatio())
                .setHandleWidgetEvents(column.isHandleWidgetEvents())
                .setStyleGenerator(column.getStyleGenerator())
                .setResizable(column.isResizable())
                .setMinimumWidthFromContent(column.isMinimumWidthFromContent())
                .setMaximumWidth(column.getMaximumWidth())
                .setHidable(column.isHidable());
        super.setColumnOrder(columnId);
        return this;
    }

    public void goInto(T item) {
        if (isRoot(item)) {
            commanderGridDataProviderWrapper.goToParentFolder();
        } else {
            commanderGridDataProviderWrapper.goIntoFolder(item);
        }
        scrollToStart();
    }

    public void goToParentFolder() {
        commanderGridDataProviderWrapper.goToParentFolder();
    }

    private ValueProvider<T, ?> wrapFirstColumnValueProvider(ValueProvider<T, ?> valueProvider) {
        ValueProvider<T, ?> maybeWrappedValueProvider;
        maybeWrappedValueProvider = item -> {
            Object value = valueProvider.apply(item);
            if (isRoot(item)) {
                value = "..";
            } else if (hierarchicalDataProvider.hasChildren(item)) {
                value = VaadinIcons.FOLDER.getHtml() + "<span>" + value;
            }
            return value;
        };
        return maybeWrappedValueProvider;
    }

    private boolean isRoot(T item) {
        return commanderGridDataProviderWrapper.getParent() != null && commanderGridDataProviderWrapper.getId(item).equals(commanderGridDataProviderWrapper.getId(commanderGridDataProviderWrapper.getParent()));
    }
}
