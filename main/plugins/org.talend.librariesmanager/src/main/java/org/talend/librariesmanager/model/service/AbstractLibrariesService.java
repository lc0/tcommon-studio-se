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
package org.talend.librariesmanager.model.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.ISVNProviderServiceInCoreRuntime;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.ModuleNeeded.ELibraryInstallStatus;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.librariesmanager.i18n.Messages;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public abstract class AbstractLibrariesService implements ILibrariesService {

    private static Logger log = Logger.getLogger(AbstractLibrariesService.class);

    private final List<IChangedLibrariesListener> listeners = new ArrayList<IChangedLibrariesListener>();

    private ILibraryManagerService repositoryBundleService = (ILibraryManagerService) GlobalServiceRegister.getDefault()
            .getService(ILibraryManagerService.class);

    // protected String LIBS = "libs";

    @Override
    public abstract URL getRoutineTemplate();

    @Override
    public abstract URL getBeanTemplate();

    @Override
    public ELibraryInstallStatus getLibraryStatus(String libName) throws BusinessException {
        for (ModuleNeeded current : ModulesNeededProvider.getModulesNeeded()) {
            if (current.getModuleName().equals(libName)) {
                return current.getStatus();
            }
        }
        throw new BusinessException(Messages.getString("ModulesNeededProvider.Module.Exception", libName)); //$NON-NLS-1$
    }

    @Override
    public void deployLibrary(URL source) throws IOException {
        deployLibrary(source, true);
    }

    private void deployLibrary(URL source, boolean reset) throws IOException {

        // fix for bug 0020953
        // if jdk is not 1.5, need decode %20 for space.
        final String decode = URLDecoder.decode(source.getFile(), "UTF-8");

        final File sourceFile = new File(decode);
        final File targetFile = new File(LibrariesManagerUtils.getLibrariesPath(ECodeLanguage.JAVA) + File.separatorChar
                + sourceFile.getName());

        if (!repositoryBundleService.contains(source.getFile())) {
            repositoryBundleService.deploy(sourceFile.toURI());
        }

        ModulesNeededProvider.userAddImportModules(targetFile.getPath(), sourceFile.getName(), ELibraryInstallStatus.INSTALLED);
        if (reset) {
            resetAndRefreshLocal(new String[] { sourceFile.getName() });
        }

    }

    @Override
    public void deployLibrarys(URL[] source) throws IOException {
        String[] namse = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            URL url = source[i];
            namse[i] = new File(url.toString()).getName();
            deployLibrary(url, false);
        }
        resetAndRefreshLocal(namse);
    }

    private RepositoryContext getRepositoryContext() {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        return (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
    }

    private void resetAndRefreshLocal(final String names[]) {
        resetModulesNeeded();

        // for feature 12877
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final String projectLabel = currentProject.getTechnicalLabel();

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject eclipseProject = workspace.getRoot().getProject(projectLabel);

        // synchronize .Java project for all new jars.
        try {
            for (String name : names) {
                String path = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
                path = path + File.separatorChar + projectLabel + File.separatorChar
                        + ERepositoryObjectType.getFolderName(ERepositoryObjectType.LIBS) + File.separatorChar + name;
                File libsTargetFile = new File(path);
                File source = new File(LibrariesManagerUtils.getLibrariesPath(ECodeLanguage.JAVA) + File.separatorChar + name);
                synJavaLibs(source);
            }
        } catch (IOException e) {
            CommonExceptionHandler.process(e);
        }

        // if svn libs setup from tac then ...
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            ISVNProviderServiceInCoreRuntime service = (ISVNProviderServiceInCoreRuntime) GlobalServiceRegister.getDefault()
                    .getService(ISVNProviderServiceInCoreRuntime.class);
            if (service != null) {
                File libFile = new File(LibrariesManagerUtils.getLibrariesPath(ECodeLanguage.JAVA));
                // check local or remote
                boolean localConnectionProvider = true;
                IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
                if (proxyRepositoryFactory != null) {
                    try {
                        localConnectionProvider = proxyRepositoryFactory.isLocalConnectionProvider();
                    } catch (PersistenceException e) {
                        //
                    }
                }
                if (!localConnectionProvider && service.isSvnLibSetupOnTAC() && service.isInSvn(libFile.getAbsolutePath())
                        && !getRepositoryContext().isOffline()) {
                    List jars = new ArrayList();
                    for (String name : names) {
                        jars.add(libFile.getAbsolutePath() + File.separatorChar + name);
                    }
                    service.deployNewJar(jars);
                    return;
                }
            }

            // if libs are stored in the project
            final RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit(currentProject, "") {

                @Override
                public void run() throws PersistenceException {
                    try {
                        for (String name : names) {
                            String path = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
                            path = path + File.separatorChar + projectLabel + File.separatorChar
                                    + ERepositoryObjectType.getFolderName(ERepositoryObjectType.LIBS) + File.separatorChar + name;
                            File libsTargetFile = new File(path);
                            File source = new File(LibrariesManagerUtils.getLibrariesPath(ECodeLanguage.JAVA)
                                    + File.separatorChar + name);
                            FilesUtils.copyFile(source, libsTargetFile);
                        }
                        eclipseProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
                    } catch (IOException e) {
                        CommonExceptionHandler.process(e);
                    } catch (CoreException e) {
                        CommonExceptionHandler.process(e);
                    }
                }
            };
            repositoryWorkUnit.setAvoidUnloadResources(true);
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IRepositoryService.class)) {
                new Thread() {

                    @Override
                    public void run() {
                        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                                IRepositoryService.class);
                        service.getProxyRepositoryFactory().executeRepositoryWorkUnit(repositoryWorkUnit);
                    }
                }.start();
            }
        }

    }

    /**
     * DOC ycbai Comment method "synJavaLibs".
     * 
     * <p>
     * Synchronize the lib of the same name with this lib in .Java\lib.
     * </p>
     * 
     * @param lib
     * @throws IOException
     */
    private void synJavaLibs(File lib) throws IOException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject javaProject = root.getProject(JavaUtils.JAVA_PROJECT_NAME);
        IFolder javaLibFolder = javaProject.getFolder(JavaUtils.JAVA_LIB_DIRECTORY);
        if (!javaLibFolder.exists()) {
            return;
        }
        File libFolder = javaLibFolder.getLocation().toFile();
        for (File externalLib : libFolder.listFiles(FilesUtils.getAcceptJARFilesFilter())) {
            if (externalLib.getName().equals(lib.getName())) {
                FilesUtils.copyFile(lib, externalLib);
            }
        }
    }

    protected void addResolvedClasspathPath(String libName) {
    }

    @Override
    public void undeployLibrary(String jarName) throws IOException {
        ILibraryManagerService service = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                ILibraryManagerService.class);
        if (service.delete(jarName)) {
            ModulesNeededProvider.userRemoveUnusedModules(jarName);
            fireLibrariesChanges();
        } else {
            CommonExceptionHandler.process(new Exception("Can not remove the module " + jarName
                    + ", this is certainly a module from a component provider and not a user module"));
        }
    }

    @Override
    public List<Problem> getProblems(INode node, IElement element) {
        List<Problem> toReturn = new ArrayList<Problem>();
        List<ModuleNeeded> list = node.getComponent().getModulesNeeded();
        List<ModuleNeeded> modulesNeeded = ModulesNeededProvider.getModulesNeeded();
        for (ModuleNeeded module : modulesNeeded) {
            for (ModuleNeeded current : list) {
                if (current.getContext().equals(module.getContext()) && current.getModuleName().equals(module.getModuleName())) {
                    if (module.getStatus() == ELibraryInstallStatus.NOT_INSTALLED
                            && current.isRequired(node.getElementParameters())) {
                        Problem problem = new Problem(element, "Module " + current.getModuleName() + " required", //$NON-NLS-1$ //$NON-NLS-2$
                                ProblemStatus.ERROR);
                        problem.setKey("Module_" + current.getModuleName());//$NON-NLS-1$
                        toReturn.add(problem);
                    }
                }

            }
        }
        return toReturn;
    }

    @Override
    public void resetModulesNeeded() {
        ModulesNeededProvider.reset();
        checkLibraries();
    }

    @Override
    public void checkLibraries() {
        this.checkInstalledLibraries();
        fireLibrariesChanges();
    }

    public abstract void checkInstalledLibraries();

    @Override
    public void addChangeLibrariesListener(IChangedLibrariesListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeLibrariesListener(IChangedLibrariesListener listener) {
        listeners.remove(listener);
    }

    private void fireLibrariesChanges() {
        for (IChangedLibrariesListener current : listeners) {
            current.afterChangingLibraries();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.general.ILibrariesService#resetModulesNeededForCurrentJob(org.talend.core.model.properties
     * .Item)
     */
    @Override
    public void updateModulesNeededForCurrentJob(IProcess process) {
        ModulesNeededProvider.resetCurrentJobNeededModuleList(process);
        checkLibraries();

    }

}
