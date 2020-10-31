package github.cweijan.http.test;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import github.cweijan.http.test.core.Generator;
import org.jetbrains.annotations.NotNull;

/**
 * @author cweijan
 * @since 2020/10/26 23:59
 */
public class CreateHttpTestAction extends PsiElementBaseIntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        PsiClass psiClass = getContainingClass(element);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (psiClass == null || method == null) return;

        PsiElement testClass = Generator.generateTestClass(project, getContainingClass(element));

        CommandProcessor.getInstance().executeCommand(project, () -> {
            DumbService.getInstance(project).withAlternativeResolveEnabled(() -> {
                PostprocessReformattingAspect.getInstance(project).postponeFormattingInside(() -> {
                    ApplicationManager.getApplication().runWriteAction((Computable<PsiElement>) () -> {
                        PsiJavaFile containingFile = (PsiJavaFile) testClass.getContainingFile();
                        Generator.importClass(containingFile, method);
                        PsiMethod testMethod = Generator.generateMethodContent(project,psiClass, method);
                        testClass.add(testMethod);
                        return testClass;
                    });
                });
            });
        }, CodeInsightBundle.message("intention.create.test", new Object[0]), this);


//        JavaTestCreator javaTestCreator = new JavaTestCreator();
//        javaTestCreator.createTest(project, editor, element.getContainingFile());


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

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiClass psiClass = getContainingClass(element);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null || psiClass == null) {
            return false;
        }

        PsiFile psiFile = psiClass.getContainingFile();
        return psiFile instanceof PsiJavaFile;
    }

    @NotNull
    @Override
    public @IntentionFamilyName String getFamilyName() {
        return "Microsoft yaHei";
    }

    @NotNull
    @Override
    @SuppressWarnings("all")
    public @IntentionName String getText() {
        return "创建Http测试用例";
    }
}
