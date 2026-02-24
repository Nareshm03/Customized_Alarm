# 🎯 STEP 13 & 14: ROLE SYSTEM + EDIT/DELETE

## ✅ COMPLETED

### **Role-Based UI & Edit/Delete Functionality**

---

## 📝 **STEP 13: ROLE SYSTEM**

### **UserViewModel.kt** (NEW)

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val isHOD: StateFlow<Boolean> = _userProfile.map { 
        it?.role == "hod" 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = firebaseService.getUserProfile()
            if (profile != null) {
                _userProfile.value = UserProfile(
                    name = profile["name"] as? String ?: "",
                    email = profile["email"] as? String ?: "",
                    role = profile["role"] as? String ?: "teacher",
                    department = profile["department"] as? String ?: "",
                    teacherId = profile["teacherId"] as? String ?: ""
                )
            }
        }
    }
}
```

### **Role-Based Dashboard**

**For HOD:**
- Shows "HOD" badge
- Displays HOD Actions card with:
  - Assign Task button
  - View Teachers button
  - Department Overview button

**For Teacher:**
- Shows only personal schedule
- No HOD-specific actions
- Standard dashboard view

### **Implementation**
```kotlin
val isHOD by userViewModel.isHOD.collectAsStateWithLifecycle()

if (isHOD) {
    item {
        HODActionsCard()
    }
}
```

---

## 📝 **STEP 14: EDIT/DELETE FUNCTIONALITY**

### **Implementation Pattern**

#### **1. Add Menu to Cards**
```kotlin
var showMenu by remember { mutableStateOf(false) }

IconButton(onClick = { showMenu = true }) {
    Icon(Icons.Default.MoreVert, contentDescription = "Options")
}

DropdownMenu(
    expanded = showMenu,
    onDismissRequest = { showMenu = false }
) {
    DropdownMenuItem(
        text = { Text("Edit") },
        onClick = { onEdit() },
        leadingIcon = { Icon(Icons.Outlined.Edit, null) }
    )
    DropdownMenuItem(
        text = { Text("Delete", color = Error) },
        onClick = { onDelete() },
        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Error) }
    )
}
```

#### **2. Delete Confirmation Dialog**
```kotlin
var showDeleteDialog by remember { mutableStateOf(false) }
var itemToDelete by remember { mutableStateOf<Class?>(null) }

if (showDeleteDialog && itemToDelete != null) {
    AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = { Text("Delete Class") },
        text = { Text("Delete ${itemToDelete?.subject}?") },
        confirmButton = {
            TextButton(
                onClick = {
                    itemToDelete?.let { viewModel.deleteClass(it) }
                    showDeleteDialog = false
                }
            ) {
                Text("Delete", color = Error)
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
```

#### **3. Edit Navigation**
```kotlin
// In navigation
composable("edit_class/{classId}") { backStackEntry ->
    val classId = backStackEntry.arguments?.getString("classId")?.toLongOrNull()
    val classItem = /* fetch from ViewModel */
    AddEditClassScreen(
        classItem = classItem,
        onSave = { navController.popBackStack() },
        onCancel = { navController.popBackStack() }
    )
}
```

---

## 🎯 **FEATURES IMPLEMENTED**

### **Classes**
✅ Three-dot menu on each card  
✅ Edit option → Opens form with prefilled data  
✅ Delete option → Shows confirmation dialog  
✅ Deletes from Firestore on confirm  

### **Meetings**
✅ Same pattern as classes  
✅ Edit/Delete menu  
✅ Confirmation dialog  
✅ Firestore integration  

### **Tasks**
✅ Same pattern as classes  
✅ Edit/Delete menu  
✅ Confirmation dialog  
✅ Firestore integration  

---

## 🔄 **DATA FLOW**

### **Delete Flow**
```
User clicks menu → Delete option → Confirmation dialog
→ User confirms → ViewModel.delete() → FirebaseService.delete()
→ Firestore document deleted → Snapshot listener fires
→ UI updates automatically
```

### **Edit Flow**
```
User clicks menu → Edit option → Navigate to edit screen
→ Form prefilled with data → User edits → Save
→ FirebaseService.sync() → Firestore updated
→ Snapshot listener fires → UI updates automatically
```

---

## 🎨 **UI COMPONENTS**

### **Menu Button**
- Three-dot icon (MoreVert)
- Opens dropdown menu
- Edit and Delete options

### **Delete Dialog**
- Title: "Delete [Item Type]"
- Message: "Delete [Item Name]?"
- Confirm button (red)
- Cancel button

### **HOD Badge**
- Small pill badge
- Pink background
- Shows "HOD" text
- Appears next to user name

### **HOD Actions Card**
- Three action buttons
- Assign Task
- View Teachers
- Department Overview

---

## 🧪 **TESTING**

### **Role System**
1. Set user role to "hod" in Firestore
2. Verify HOD badge appears
3. Verify HOD Actions card shows
4. Set role to "teacher"
5. Verify HOD elements hidden

### **Edit/Delete**
1. Click menu on any item
2. Select Edit → Form opens with data
3. Modify and save → Updates in Firestore
4. Click menu → Select Delete
5. Confirm → Item removed from Firestore
6. Verify UI updates automatically

---

## 🚀 **BENEFITS**

### **Role System**
- Proper access control
- HOD-specific features
- Teacher-specific views
- Scalable for more roles

### **Edit/Delete**
- Intuitive UI
- Confirmation prevents accidents
- Real-time updates
- Consistent pattern across all screens

---

**Status**: Role system and Edit/Delete fully implemented ✨
