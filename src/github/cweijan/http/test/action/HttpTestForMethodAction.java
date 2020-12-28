package github.cweijan.http.test.action;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import github.cweijan.http.test.config.Constant;
import github.cweijan.http.test.core.GenerateContext;
import github.cweijan.http.test.core.Generator;
import github.cweijan.http.test.core.MethodCreator;
import github.cweijan.http.test.util.MvcUtil;
import github.cweijan.http.test.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author cweijan
 * @since 2020/10/26 23:59
 */
public class HttpTestForMethodAction extends PsiElementBaseIntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        PsiClass sourceClass = PsiUtils.getContainingClass(element);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (sourceClass == null || method == null) return;

        GenerateContext generateContext = new GenerateContext();
        generateContext.project = project;
        generateContext.sourceClass = PsiUtils.getContainingClass(element);

        PsiUtils.checkAndAddModule(project, sourceClass);

        PsiClass testClass = Generator.getOrCreateTestClass(generateContext);

        PsiUtils.doWrite(project, () -> {
            PsiElement addedMethod = MethodCreator.createMethod(project, sourceClass, testClass, method);
            CodeInsightUtil.positionCursor(project, testClass.getContainingFile(), addedMethod);
            return testClass;
        });

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiClass psiClass = PsiUtils.getContainingClass(element);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null || psiClass == null) {
            return false;
        }
        return MvcUtil.isController(psiClass) && MvcUtil.isRequest(method);
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
