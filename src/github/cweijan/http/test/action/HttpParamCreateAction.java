package github.cweijan.http.test.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import github.cweijan.http.test.config.Constant;
import github.cweijan.http.test.util.MvcUtil;
import github.cweijan.http.test.util.PsiUtils;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author cweijan
 * @since 2020/11/04 16:36
 */
public class HttpParamCreateAction extends PsiElementBaseIntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiClass sourceClass = PsiUtils.getContainingClass(element);
        String currentPackage = PsiUtils.getPackage(method).getParentPackage().getQualifiedName();

        String requestClass = WordUtils.capitalize(method.getName()) + "DTO";
        String requestClassFull = currentPackage + ".dto." + requestClass;
        String returnClass = WordUtils.capitalize(method.getName()) + "VO";
        String returnClassFull = currentPackage + ".vo." + returnClass;

        PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);

        PsiParameter parameter = elementFactory.createParameterFromText(requestClass + " " + method.getName() + "DTO", method);
        method.getParameterList().add(parameter);

        if (method.getReturnType().getCanonicalText().equals("void")) {
            PsiTypeElement returnType = elementFactory.createTypeElement(elementFactory.createTypeByFQClassName(returnClass));
            method.getReturnTypeElement().replace(returnType);
        }

        if(MvcUtil.isController(sourceClass) && !MvcUtil.isRequest(method)){
            String annotationByName = MvcUtil.getAnnotationByName(method.getName());
            PsiUtils.addAnnotation(method,annotationByName);
        }

        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null) {
            return false;
        }
        return method.getParameterList().getParametersCount() == 0;
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
        return Constant.PARAM_CREATE_TEXT;
    }

}
