package github.cweijan.http.test.action;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.testIntegration.createTest.CreateTestAction;
import com.intellij.util.IncorrectOperationException;
import github.cweijan.http.test.config.Constant;
import github.cweijan.http.test.core.GenerateContext;
import github.cweijan.http.test.core.Generator;
import github.cweijan.http.test.core.MethodCreator;
import github.cweijan.http.test.ui.CreateHttpTestDialog;
import github.cweijan.http.test.util.MvcUtil;
import github.cweijan.http.test.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

import static github.cweijan.http.test.util.PsiUtils.checkAndAddModule;

/**
 * @author cweijan
 * @since 2020/10/31 21:53
 */
public class HttpTestForClassAction extends PsiElementBaseIntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {

        PsiClass sourceClass = PsiUtils.getContainingClass(psiElement);

        final PsiPackage srcPackage = JavaDirectoryService.getInstance().getPackage(sourceClass.getContainingFile().getContainingDirectory());
        final Module srcModule = ModuleUtilCore.findModuleForPsiElement(sourceClass);
        Module testModule = CreateTestAction.suggestModuleForTests(project, srcModule);

        CreateHttpTestDialog testDialog = new CreateHttpTestDialog(project, "CreateHttpTest", sourceClass, srcPackage, testModule);

        checkAndAddModule(project, sourceClass);

        ApplicationManager.getApplication().invokeLater(() -> {
            if (testDialog.showAndGet()) {
                GenerateContext generateContext = testDialog.getGenerateContext();
                PsiUtils.doWrite(project, () -> {
                    PsiClass testClass = Generator.getOrCreateTestClass(generateContext);
                    CodeInsightUtil.positionCursorAtLBrace(project, testClass.getContainingFile(), testClass);
                    for (MemberInfo memberInfo : generateContext.methods) {
                        MethodCreator.createMethod(project, sourceClass, testClass, (PsiMethod) memberInfo.getMember());
                    }
                    JavaCodeStyleManager.getInstance(project).shortenClassReferences(testClass);
                    return testClass;
                });
            }
        });

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {

        PsiElement psiClass = psiElement.getParent();
        return (psiClass instanceof PsiClass) && MvcUtil.isController((PsiClass) psiClass);
    }

    @NotNull
    @Override
    public @IntentionFamilyName String getFamilyName() {
        return Constant.FONT_FAMILY;
    }

    @NotNull
    @Override
    @SuppressWarnings("all")
    public @IntentionName String getText() {
        return Constant.TEST_TEXT;
    }

}
