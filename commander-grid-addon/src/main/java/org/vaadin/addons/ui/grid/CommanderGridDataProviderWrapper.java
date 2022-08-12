package org.vaadin.addons.ui.grid;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.stream.Stream;

import com.vaadin.data.provider.DataProviderWrapper;
import com.vaadin.data.provider.HierarchicalDataProvider;
import com.vaadin.data.provider.HierarchicalQuery;
import com.vaadin.data.provider.Query;

/**
 * Wraps a {@link com.vaadin.data.provider.HierarchicalDataProvider} into flat {@link com.vaadin.data.provider.DataProvider} and provides {@link #goToParentFolder()} and {@link #goIntoFolder(Object)} instead.
 * To be used by {@link org.vaadin.addons.ui.grid.CommanderGrid}
 */
public class CommanderGridDataProviderWrapper<T, F> extends DataProviderWrapper<T, F, F> {

    private final HierarchicalDataProvider<T, F> hierarchicalDataProvider;
    private transient LinkedList<T> parents = new LinkedList<>();

    public CommanderGridDataProviderWrapper(HierarchicalDataProvider<T, F> dataProvider) {
        super(dataProvider);
        this.hierarchicalDataProvider = dataProvider;
    }

    @Override
    public int size(Query<T, F> query) {
        return hierarchicalDataProvider.getChildCount(toHierarchicalQuery(query)) + (parents .isEmpty() ? 0 : 1);
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {
        Stream<T> stream = hierarchicalDataProvider.fetchChildren(toHierarchicalQuery(query));
        stream = parents.isEmpty() ? stream : Stream.concat(Stream.of(parents.getLast()), stream);
        return stream.skip(query.getOffset()).limit(query.getLimit());
    }

    @Override
    protected F getFilter(Query query) {
        return null;
    }

    public void goIntoFolder(T parent) {
        this.parents.add(parent);
        refreshAll();
    }

    public void goToParentFolder() {
        if (!parents.isEmpty()) {
            this.parents.removeLast();
            refreshAll();
        }
    }

    public T getParent() {
        return parents.isEmpty() ? null : parents.getLast();
    }

    private HierarchicalQuery<T, F> toHierarchicalQuery(Query<T, F> query) {
        return new HierarchicalQuery<>(
                query.getOffset(),
                query.getLimit(),
                query.getSortOrders(),
                (Comparator) query.getInMemorySorting(),
                getFilter(query),
                getParent());
    }
}
