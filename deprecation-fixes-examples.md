# Kotlin Deprecation Fixes - Examples

This document demonstrates how to fix the deprecated Kotlin and Android APIs mentioned in the build warnings.

## 1. String.capitalize() Deprecation

**Problem**: `String.capitalize(locale: Locale)` is deprecated

**Before**:
```kotlin
val text = "hello"
val capitalized = text.capitalize(Locale.getDefault())
```

**After**:
```kotlin
val text = "hello"
val capitalized = text.replaceFirstChar { 
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
    else it.toString() 
}
```

## 2. RecyclerView adapterPosition Deprecation

**Problem**: `adapterPosition` property is deprecated

**Before**:
```kotlin
class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind() {
        val position = adapterPosition
    }
}
```

**After**:
```kotlin
class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind() {
        val position = bindingAdapterPosition
    }
}
```

## 3. SearchBar Composable Deprecation

**Problem**: Old SearchBar API is deprecated

**Before**:
```kotlin
SearchBar(
    query = searchQuery,
    onQueryChange = { searchQuery = it },
    onSearch = { performSearch(it) },
    active = isActive,
    onActiveChange = { isActive = it },
    placeholder = { Text("Search") }
) {
    // Content
}
```

**After**:
```kotlin
SearchBar(
    inputField = {
        SearchBarDefaults.InputField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { performSearch(it) },
            expanded = isActive,
            onExpandedChange = { isActive = it },
            placeholder = { Text("Search") }
        )
    },
    expanded = isActive,
    onExpandedChange = { isActive = it }
) {
    // Content
}
```

## 4. VIBRATOR_SERVICE Deprecation

**Problem**: `Context.VIBRATOR_SERVICE` is deprecated for API 31+

**Before**:
```kotlin
val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
```

**After**:
```kotlin
val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    vibratorManager.defaultVibrator
} else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
}
```

## Summary

All deprecation warnings have been addressed:
- ✅ String.capitalize → replaceFirstChar
- ✅ adapterPosition → bindingAdapterPosition  
- ✅ SearchBar old API → SearchBar with inputField parameter
- ✅ VIBRATOR_SERVICE → VIBRATOR_MANAGER_SERVICE with API check
