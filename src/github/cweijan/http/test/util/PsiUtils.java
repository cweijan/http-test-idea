/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */

package github.cweijan.http.test.util;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.JavaProjectModelModificationService;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testIntegration.createTest.CreateTestAction;
import github.cweijan.http.test.config.Constant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author bruce.ge
 * @Date 2017/1/30
 * @Description
 */
public class PsiUtils {
    public static boolean isNotSystemClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        String qualifiedName = psiClass.getQualifiedName();
        return qualifiedName != null && !qualifiedName.startsWith("java.");
    }

    public static boolean isValidSetMethod(PsiMethod m) {
        return m.hasModifierProperty("public") &&
                !m.hasModifierProperty("static") &&
                m.getParameterList().getParametersCount() == 1 &&
                (m.getName().startsWith("set") || m.getName().startsWith("with"));
    }

    public static void addSetMethodToList(PsiClass psiClass, List<PsiMethod> methodList) {
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod method : methods) {
            if (isValidSetMethod(method)) {
                methodList.add(method);
            }
        }
    }

    @NotNull
    public static List<PsiMethod> extractSetMethods(PsiClass psiClass) {
        List<PsiMethod> methodList = new ArrayList<>();
        while (isNotSystemClass(psiClass)) {
            addSetMethodToList(psiClass, methodList);
            psiClass = psiClass.getSuperClass();
        }
        return methodList;
    }

    public static boolean isRequest(PsiMember psiMember) {
        for (PsiAnnotation annotation : psiMember.getAnnotations()) {
            if (annotation.getQualifiedName().startsWith("org.springframework.web.bind.annotation")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isController(PsiClass psiClass) {
        String[] qualifiedNames = new String[]{"org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation"};
        while (psiClass != null) {
            for (PsiAnnotation annotation : psiClass.getAnnotations()) {
                for (String qualifiedName : qualifiedNames) {
                    if (qualifiedName.equals(annotation.getQualifiedName())) {
                        return true;
                    }
                }
            }
            psiClass = psiClass.getSuperClass();
        }
        return false;
    }

    public static String getQualifiedName(PsiClass psiClass) {
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiClass.getContainingFile().getContainingDirectory());
        if (psiPackage == null) {
            return psiClass.getName();
        }
        return psiPackage.getQualifiedName() + "." + psiClass.getName();

    }

    public static PsiClass getContainingClass(PsiElement element) {
        final PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
        if (psiClass == null) {
            final PsiFile containingFile = element.getContainingFile();
            if (containingFile instanceof PsiClassOwner) {
                final PsiClass[] classes = ((PsiClassOwner) containingFile).getClasses();
                if (classes.length == 1) {
                    return classes[0];
                }
            }
        }
        return psiClass;
    }


    public static void doWrite(Project project, Computable<PsiElement> computable) {
        CommandProcessor.getInstance().executeCommand(project, () -> {
            DumbService.getInstance(project).withAlternativeResolveEnabled(() -> {
                PostprocessReformattingAspect.getInstance(project).postponeFormattingInside(() -> {
                    ApplicationManager.getApplication().runWriteAction(computable);
                });
            });
        }, CodeInsightBundle.message("intention.create.test", new Object[0]), PsiUtils.class);
    }

    public static void checkAndAddModule(@NotNull Project project, PsiClass sourceClass) {
        final Module srcModule = ModuleUtilCore.findModuleForPsiElement(sourceClass);
        Module testModule = CreateTestAction.suggestModuleForTests(project, srcModule);
        GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(testModule);
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(Constant.DEPENDENCY_ANNOTATION, scope);
        if (psiClass == null) {
            JavaProjectModelModificationService.getInstance(project)
                    .addDependency(testModule, Constant.TESTNG_DESCRIPTOR, DependencyScope.TEST);
        }
    }
}