package dev.gaphunter.ratelimiterfallbackcompanion.model

import com.intellij.psi.PsiElement

/** One `fallbackMethod = "name"` reference that doesn't match any method name in the enclosing class. */
data class FallbackHit(val nameElement: PsiElement, val fallbackMethodName: String, val annotationName: String)
