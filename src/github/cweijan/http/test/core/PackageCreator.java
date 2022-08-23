package github.cweijan.http.test.core;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JavaProjectRootsUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.testIntegration.createTest.CreateTestAction;
import com.intellij.testIntegration.createTest.CreateTestUtils;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This code refer from CreateTestDialog.
 * @author cweijan
 * @since 2020/10/31 15:38
 */
@SuppressWarnings("all")
public  class PackageCreator {

    private Project project;
    private Module targetModule;

    public PackageCreator(Project project, Module targetModule) {
        this.project = project;
        this.targetModule = targetModule;
    }

    @Nullable
    public PsiDirectory createPackage(String packageName) throws IncorrectOperationException {
        PackageWrapper targetPackage = new PackageWrapper(PsiManager.getInstance(this.project), packageName);
        VirtualFile selectedRoot = (VirtualFile)ReadAction.compute(() -> {
            List<VirtualFile> testFolders = CreateTestUtils.computeTestRoots(targetModule);
            ArrayList roots;
            if (testFolders.isEmpty()) {
                roots = new ArrayList();
                List<String> urls = CreateTestUtils.computeSuitableTestRootUrls(targetModule);
                Iterator var5 = urls.iterator();

                while(var5.hasNext()) {
                    String url = (String)var5.next();

                    try {
                        ContainerUtil.addIfNotNull(roots, VfsUtil.createDirectories(VfsUtilCore.urlToPath(url)));
                    } catch (IOException var8) {
                        throw new RuntimeException(var8);
                    }
                }

                if (roots.isEmpty()) {
                    JavaProjectRootsUtil.collectSuitableDestinationSourceRoots(this.targetModule, roots);
                }

                if (roots.isEmpty()) {
                    return null;
                }
            } else {
                roots = new ArrayList(testFolders);
            }

            if (roots.size() == 1) {
                return (VirtualFile)roots.get(0);
            } else {
                PsiDirectory defaultDir = this.chooseDefaultDirectory(targetPackage.getDirectories(), roots);
                return MoveClassesOrPackagesUtil.chooseSourceRoot(targetPackage, roots, defaultDir);
            }
        });
        return selectedRoot == null ? null : WriteCommandAction.writeCommandAction(this.project).withName(CodeInsightBundle.message("create.directory.command", new Object[0])).compute(() -> {
            return RefactoringUtil.createPackageDirectoryInSourceRoot(targetPackage, selectedRoot);
        });
    }


    @Nullable
    private PsiDirectory chooseDefaultDirectory(PsiDirectory[] directories, List<VirtualFile> roots) {
        List<PsiDirectory> dirs = new ArrayList();
        PsiManager psiManager = PsiManager.getInstance(this.project);
        Iterator var5 = ModuleRootManager.getInstance(this.targetModule).getSourceRoots(JavaSourceRootType.TEST_SOURCE).iterator();

        while(var5.hasNext()) {
            VirtualFile file = (VirtualFile)var5.next();
            PsiDirectory dir = psiManager.findDirectory(file);
            if (dir != null) {
                dirs.add(dir);
            }
        }

        if (!dirs.isEmpty()) {
            var5 = dirs.iterator();

            PsiDirectory dir;
            String dirName;
            do {
                if (!var5.hasNext()) {
                    return (PsiDirectory)dirs.get(0);
                }

                dir = (PsiDirectory)var5.next();
                dirName = dir.getVirtualFile().getPath();
            } while(dirName.contains("generated"));

            return dir;
        } else {
            PsiDirectory[] var13 = directories;
            int var14 = directories.length;

            for(int var16 = 0; var16 < var14; ++var16) {
                PsiDirectory dir = var13[var16];
                VirtualFile file = dir.getVirtualFile();
                Iterator var10 = roots.iterator();

                while(var10.hasNext()) {
                    VirtualFile root = (VirtualFile)var10.next();
                    if (VfsUtilCore.isAncestor(root, file, false)) {
                        PsiDirectory rootDir = psiManager.findDirectory(root);
                        if (rootDir != null) {
                            return rootDir;
                        }
                    }
                }
            }

            return (PsiDirectory) ModuleManager.getInstance(this.project).getModuleDependentModules(this.targetModule).stream().flatMap((module) -> {
                return ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.TEST_SOURCE).stream();
            }).map((rootx) -> {
                return psiManager.findDirectory(rootx);
            }).findFirst().orElse(null);
        }
    }


}
