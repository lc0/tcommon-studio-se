// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.process;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * functions used only for graphical purpose for INode (components)
 */
public interface IGraphicalNode extends INode {

    public Dimension getSize();

    public void setSize(Dimension size);

    public void setLocation(Point location);

    public Point getLocation();

    public void checkAndRefreshNode();

    public boolean isCheckProperty();

    public void setCheckProperty(boolean b);

    public void setErrorFlag(boolean b);

    public void setCompareFlag(boolean b);

    public void setErrorInfo(String errorInfo);

    public void setErrorInfoChange(String errorInfo, Object value);
}
