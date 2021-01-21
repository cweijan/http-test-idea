package github.cweijan.http.test.action;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import github.cweijan.http.test.config.Constant;
import github.cweijan.http.test.template.TestTemplate;
import github.cweijan.http.test.util.ArrayUtil;
import github.cweijan.http.test.util.MvcUtil;
import github.cweijan.http.test.util.PsiUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author cweijan
 * @since 2020/11/05 21:53
 */
public class ServiceInjectAction extends PsiElementBaseIntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);

        String param = String.join(",", ArrayUtil.mapArray(method.getParameterList().getParameters(), psiParameter ->
                psiParameter.getType().getCanonicalText() + " " + psiParameter.getName()
        ));
        String invokeParam = String.join(",", ArrayUtil.mapArray(method.getParameterList().getParameters(), PsiParameter::getName));

        PsiClass sourceClass = PsiUtils.getContainingClass(element);

        Collection<PsiReference> all = ReferencesSearch.search(sourceClass).findAll();

        for (PsiClass parentInterface : sourceClass.getInterfaces()) {
            if (StringUtils.difference(((PsiJavaFileImpl) sourceClass.getContainingFile()).getPackageName(), ((PsiJavaFileImpl) parentInterface.getContainingFile()).getPackageName()).length() < 10) {
                all.addAll(ReferencesSearch.search(parentInterface).findAll());
            }
        }
        all = all.stream().filter(psiReference ->
                (psiReference instanceof PsiElement) && (((PsiElement) psiReference).getParent().getParent() instanceof PsiField)
        ).collect(Collectors.toList());

        for (PsiReference psiReference : all) {

            PsiClass referClass = PsiUtils.getContainingClass((PsiElement) psiReference);
            if (!MvcUtil.isController(referClass)) {
                continue;
            }

            String fieldName = ((PsiFieldImpl) ((PsiJavaCodeReferenceElementImpl) psiReference).getParent().getParent()).getName();

            String template = TestTemplate.getInstance().loadControllerMethodTemplate()
                    .replace(":return", method.getReturnType().getCanonicalText())
                    .replace(":name", method.getName())
                    .replace(":field", fieldName)
                    .replace(":hasReturn", !"void".equals(method.getReturnType().getCanonicalText()) ? "return" : "")
                    .replace(":Param", param)
                    .replace(":param", invokeParam);

            PsiMethod newReferMethod = JVMElementFactories.getFactory(referClass.getLanguage(), project).createMethodFromText(template, referClass);

            String interfaceTemplate = ":return :name(:Param);".replace(":name", method.getName())
                    .replace(":return", method.getReturnType().getCanonicalText())
                    .replace(":Param", param);
            PsiMethod interfaceMethod = JVMElementFactories.getFactory(referClass.getLanguage(), project)
                    .createMethodFromText(interfaceTemplate, referClass);
            for (PsiClass psiInterface : sourceClass.getInterfaces()) {
                if (PsiUtils.findMethod(psiInterface, interfaceMethod) != null) {
                    continue;
                }
                psiInterface.add(interfaceMethod);
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(interfaceMethod);
            }

            PsiMethod oldMethod = PsiUtils.findMethod(referClass, newReferMethod);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(newReferMethod);
            if (oldMethod != null) {
                CodeInsightUtil.positionCursor(project, referClass.getContainingFile(), oldMethod);
            } else {
                if (MvcUtil.isController(referClass)) {
                    String annotationByName = MvcUtil.getAnnotationByName(method.getName());
                    PsiUtils.addAnnotation(newReferMethod, annotationByName);
                }
                PsiElement addedMethod = referClass.add(newReferMethod);
                CodeInsightUtil.positionCursor(project, referClass.getContainingFile(), addedMethod);
            }
        }

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if(!PsiUtils.inMethodSign(element)){
            return false;
        }
        PsiClass sourceClass = PsiUtils.getContainingClass(element);
        return MvcUtil.isSimpleClass(sourceClass) && !MvcUtil.isController(sourceClass);
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
        return "委托方法";
    }

}
