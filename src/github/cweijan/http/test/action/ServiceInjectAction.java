package github.cweijan.http.test.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Query;
import github.cweijan.http.test.template.TestTemplate;
import github.cweijan.http.test.util.MvcUtil;
import github.cweijan.http.test.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author cweijan
 * @since 2020/11/05 21:53
 */
public class ServiceInjectAction extends HttpParamCreateAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        super.invoke(project, editor, element);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);
        PsiParameter parameter = null;
        try {
            parameter = method.getParameterList().getParameter(0);
        } catch (Exception e) {
            return;
        }

        PsiClass sourceClass = PsiUtils.getContainingClass(element);


        Query<PsiReference> psiReferences = ReferencesSearch.search(sourceClass).filtering(psiReference ->
                (psiReference instanceof PsiElement) && (((PsiElement) psiReference).getParent().getParent() instanceof PsiField)
        );

        PsiReference psiReference = psiReferences.findFirst();
        if (psiReference instanceof PsiElement) {
            PsiClass psiClass = PsiUtils.getContainingClass((PsiElement) psiReference);

            String fieldName = ((PsiFieldImpl) ((PsiJavaCodeReferenceElementImpl) psiReference).getParent().getParent()).getName();

            String template = TestTemplate.getInstance().loadControllerMethodTemplate()
                    .replace(":return", method.getReturnType().getCanonicalText())
                    .replace(":name", method.getName())
                    .replace(":field", fieldName)
                    .replace(":Param", parameter.getType().getCanonicalText())
                    .replace(":param", parameter.getName());

            PsiMethod controllerMethod = JVMElementFactories.getFactory(psiClass.getLanguage(), project).createMethodFromText(template, psiClass);
            controllerMethod.getModifierList().addAnnotation(MvcUtil.getAnnotationByName(method.getName()));

            String interfaceTemplate = ":return :name(:Param :param);".replace(":name", method.getName())
                    .replace(":return", method.getReturnType().getCanonicalText())
                    .replace(":Param", parameter.getType().getCanonicalText())
                    .replace(":param", parameter.getName());

            PsiMethod interfaceMethod = JVMElementFactories.getFactory(psiClass.getLanguage(), project)
                    .createMethodFromText(interfaceTemplate, psiClass);
            for (PsiClass psiInterface : sourceClass.getInterfaces()) {
                psiInterface.add(interfaceMethod);
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(interfaceMethod);
            }

            psiClass.add(controllerMethod);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(controllerMethod);

        }


    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (super.isAvailable(project, editor, element)) {
            return false;
        }
        if (true) {
            return false;
        }
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null) {
            return false;
        }
        PsiClass sourceClass = PsiUtils.getContainingClass(element);
        if (sourceClass.isInterface() || sourceClass.isEnum() || sourceClass.isAnnotationType() || sourceClass.isRecord()) {
            return false;
        }

        return method.getParameterList().getParametersCount() == 0;
    }
}
