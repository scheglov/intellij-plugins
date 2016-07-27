package org.intellij.plugins.postcss.references;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.css.CssStylesheet;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;

public class PostCssCustomSelectorReference extends PsiPolyVariantReferenceBase<PsiElement> {
  public PostCssCustomSelectorReference(PsiElement psiElement) {
    super(psiElement, TextRange.from(0, psiElement.getTextLength()));
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    PsiFile file = myElement.getContainingFile();
    CssStylesheet styleSheet = PsiTreeUtil.getParentOfType(myElement, CssStylesheet.class);
    if (styleSheet != null) {
      final Set<VirtualFile> importedFiles = CssUtil.getImportedFiles(file, myElement, false);
      final Project project = myElement.getProject();
      final ArrayList<ResolveResult> result = new ArrayList<>();

      final GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(myElement);
      PostCssCustomSelectorIndex.process(StringUtil.trimStart(myElement.getText(), "--"), project, scope, selector -> {
        if (importedFiles.contains(selector.getContainingFile().getVirtualFile())) {
          result.add(new PsiElementResolveResult(selector, true));
        }
        return true;
      });

      return result.toArray(new ResolveResult[result.size()]);
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return super.handleElementRename("--" + newElementName);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    if (!(element instanceof PostCssCustomSelector)) return false;
    return StringUtil.trimStart(getCanonicalText(), "--").equalsIgnoreCase(((PostCssCustomSelector)element).getName());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }
}