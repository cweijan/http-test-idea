package github.cweijan.http.test.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import github.cweijan.http.test.config.Constant;
import github.cweijan.http.test.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author cweijan
 * @since 2020/11/04 23:50
 */
public class LombokCreateAction extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {

        PsiClass psiClass = (PsiClass) psiElement.getParent();

        PsiModifierList classModifierList = psiClass.getModifierList();

        PsiClass superClass = psiClass.getSuperClass();
        if (superClass != null && !superClass.getName().equals("Object")) {
            classModifierList.addAnnotation("lombok.EqualsAndHashCode(callSuper = true)");
        }

        classModifierList.addAnnotation("lombok.AllArgsConstructor");
        classModifierList.addAnnotation("lombok.NoArgsConstructor");
        classModifierList.addAnnotation("lombok.Builder");
        classModifierList.addAnnotation("lombok.Data");

        JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiClass);

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {

        PsiElement parentElement = psiElement.getParent();
        if (!(parentElement instanceof PsiClass)) {
            return false;
        }
        PsiClass psiClass= (PsiClass) parentElement;

        // 如果没lombok依赖, 跳过
        final Module srcModule = ModuleUtilCore.findModuleForPsiElement(psiClass);
        GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(srcModule);
        PsiClass lombokClass = JavaPsiFacade.getInstance(project).findClass(Constant.LOMBOK_DEPENDENCY_ANNOTATION, scope);
        if (lombokClass == null) {
            return false;
        }

        if(psiClass.isInterface() || psiClass.isEnum() || psiClass.isAnnotationType()){
            return false;
        }

        // 如果有指定注解, 跳过
        boolean hasAnnotation = PsiUtils.hasAnnotation((PsiClass) psiClass,
                new String[]{
                        "org.springframework.stereotype.Controller",
                        "org.springframework.web.bind.annotation.RestController",
                        "lombok.Data",
                        "org.springframework.stereotype.Service"
                });
        if (hasAnnotation) {
            return false;
        }


        for (PsiField field : ((PsiClass) psiClass).getFields()) {
            if (!field.hasModifierProperty(PsiModifier.PRIVATE) ||
                    field.hasModifierProperty(PsiModifier.STATIC) ||
                    field.hasModifierProperty(PsiModifier.FINAL) ||
                    field.hasAnnotation("javax.annotation.Resource") ||
                    field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")
            ) {
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
        return "一键创建Lombok注解";
    }

}
