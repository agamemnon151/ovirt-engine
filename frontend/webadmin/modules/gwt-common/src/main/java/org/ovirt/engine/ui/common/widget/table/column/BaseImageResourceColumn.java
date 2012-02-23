package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;

/**
 * Column for rendering {@link ImageResource} instances.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class BaseImageResourceColumn<T> extends Column<T, ImageResource> {

    public BaseImageResourceColumn(Cell<ImageResource> cell) {
        super(cell);
    }

}
