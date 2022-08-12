/**
 * This file Copyright (c) 2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 * <p>
 * <p>
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 * <p>
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 * <p>
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 * <p>
 * Any modifications to this file must keep this entire header
 * intact.
 */
package org.vaadin.addons.ui.grid;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.stream.Stream;

import com.vaadin.data.provider.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.HierarchicalQuery;

/**
 * {@link com.vaadin.data.provider.HierarchicalDataProvider} for file system.
 */
public class HierarchicalFileDataProvider extends AbstractBackEndHierarchicalDataProvider<File, FileFilter> implements ConfigurableFilterDataProvider<File, FileFilter, FileFilter> {

    private transient FileFilter filter;

    private final File root;

    public HierarchicalFileDataProvider(File root) {
        this.root = root;
    }

    @Override
    protected Stream<File> fetchChildrenFromBackEnd(HierarchicalQuery<File, FileFilter> query) {
        File parent = query.getParentOptional().orElse(root);
        File[] array = parent.listFiles(filter);
        if (array == null) return Stream.empty();
        Stream<File> stream = Arrays.stream(array);
        if (query.getInMemorySorting() != null) {
            stream = stream.sorted(query.getInMemorySorting());
        }
        return stream;
    }

    @Override
    public int getChildCount(HierarchicalQuery<File, FileFilter> query) {
        return (int) fetchChildrenFromBackEnd(query).count();
    }

    @Override
    public boolean hasChildren(File item) {
        return item.isDirectory();
    }

    @Override
    public void setFilter(FileFilter filter) {
        this.filter = filter;
        refreshAll();
    }
}
