package github.cweijan.http.test.action.helper;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import github.cweijan.http.test.config.Constant;
import github.cweijan.http.test.util.MvcUtil;
import github.cweijan.http.test.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author cweijan
 * @since 2020/11/05 0:22
 */
public class FieldToPrivateAction extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {

        PsiClass psiClass = (PsiClass) psiElement.getParent();
        for (PsiField field : psiClass.getAllFields()) {
            if (notPrivateField(field)) {
                continue;
            }
            PsiUtil.setModifierProperty(field, PsiModifier.PRIVATE, true);
        }
    }

    private boolean notPrivateField(PsiField field) {
        return field.hasModifierProperty(PsiModifier.PUBLIC) ||
                field.hasModifierProperty(PsiModifier.PRIVATE) ||
                field.hasModifierProperty(PsiModifier.PROTECTED) ||
                field.hasModifierProperty(PsiModifier.STATIC) ||
                field.hasModifierProperty(PsiModifier.FINAL);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        PsiElement parentElement = psiElement.getParent();
        if (!(parentElement instanceof PsiClass)) {
            return false;
        }
        PsiClass psiClass = (PsiClass) parentElement;

        if(!MvcUtil.isSimpleClass(psiClass)){
            return false;
        }

        // 如果有指定注解, 跳过
        boolean hasAnnotation = PsiUtils.hasAnnotation(psiClass,
                new String[]{
                        "org.springframework.stereotype.Controller",
                        "org.springframework.web.bind.annotation.RestController",
                        "org.springframework.stereotype.Service"
                });
        if (hasAnnotation) {
            return false;
        }

        for (PsiField field : ((PsiClass) psiClass).getFields()) {
            if (notPrivateField(field)) {
                continue;
            }
            // 有field且field均不满足以上条件, 启用
            return true;
        }

        // 如果没有field, 跳过
        return false;
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
        return "将Field转为private";
    }
}
