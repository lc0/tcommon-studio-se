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
package org.talend.repository.localprovider.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.ResourceModelUtils;

/**
 * Provides utilities methods relative to model on IResource.<br/>
 * 
 * $Id$
 * 
 */
public final class LocalResourceModelUtils extends ResourceModelUtils {

    private LocalResourceModelUtils() {
    }

    public static IFolder getFolder(Project project, ERepositoryObjectType type) throws PersistenceException {
        IProject fsProject = LocalResourceModelUtils.getProject(project);
        return ResourceUtils.getFolder(fsProject, ERepositoryObjectType.getFolderName(type), true);
    }
}
