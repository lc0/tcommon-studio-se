// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.ui.swt.advanced.dataeditor.button;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.i18n.Messages;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedControlViewer;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 * 
 */
public abstract class SaveAsGenericSchemaPushButton extends ExtendedPushButton {

    public SaveAsGenericSchemaPushButton(Composite parent, AbstractExtendedTableViewer extendedViewer) {
        super(parent, extendedViewer, "Save as generic schema", // Messages.getString("ExportPushButton.ExportButton.Tip"),
                ImageProvider.getImage(EImage.SAVE_ICON)); //$NON-NLS-1$
    }

    @Override
    protected abstract Command getCommandToExecute();
}
