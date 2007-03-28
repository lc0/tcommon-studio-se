// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.commons.ui.swt.tableviewer.tableeditor;

import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;

/**
 * DOC amaumont class global comment. Detailled comment <br/> $Id: TableEditorInitializer.java,v 1.1 2006/06/02 15:24:10
 * amaumont Exp $
 */
public abstract class TableEditorContent {

    /**
     * You can override this method if necessary.
     * 
     * @param table
     * @return TableEditor
     */
    public TableEditor createTableEditor(Table table) {
        return new TableEditor(table);
    }

    public abstract Control initialize(Table table, TableEditor tableEditor, TableViewerCreatorColumn currentColumn,
            Object currentRowObject, Object currentCellValue);

}
