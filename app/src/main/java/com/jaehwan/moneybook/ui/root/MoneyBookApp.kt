package com.jaehwan.moneybook.ui.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.category.ui.CategoryFormDialog
import com.jaehwan.moneybook.category.ui.CategoryList
import com.jaehwan.moneybook.category.ui.CategoryViewModel
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.ui.LedgerScreen
import com.jaehwan.moneybook.transaction.ui.LedgerViewModel
import com.jaehwan.moneybook.transaction.ui.TransactionFormDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyBookApp(viewModel: CategoryViewModel = hiltViewModel()) {
    val ledgerViewModel: LedgerViewModel = hiltViewModel()
    val categories by viewModel.categories.collectAsState()
    val ledgerRows by ledgerViewModel.ledgerRows.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var destination by remember { mutableStateOf(MainDestination.Category) }
    var showCategoryForm by remember { mutableStateOf(false) }
    var categoryBeingEdited by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryPendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    var showTransactionForm by remember { mutableStateOf(false) }
    var transactionBeingEdited by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionPendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "MoneyBook",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("카테고리") },
                    selected = destination == MainDestination.Category,
                    onClick = {
                        destination = MainDestination.Category
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                NavigationDrawerItem(
                    label = { Text("가계부") },
                    selected = destination == MainDestination.Ledger,
                    onClick = {
                        destination = MainDestination.Ledger
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "메뉴 열기"
                                )
                            }
                        )
                    },
                    title = {
                        Text(
                            when (destination) {
                                MainDestination.Category -> "카테고리"
                                MainDestination.Ledger -> "가계부"
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            floatingActionButton = {
                when (destination) {
                    MainDestination.Category -> {
                        FloatingActionButton(
                            onClick = {
                                categoryBeingEdited = null
                                showCategoryForm = true
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "카테고리 추가")
                        }
                    }
                    MainDestination.Ledger -> {
                        FloatingActionButton(
                            onClick = {
                                transactionBeingEdited = null
                                showTransactionForm = true
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "거래 추가")
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (destination) {
                    MainDestination.Category -> {
                        CategoryList(
                            categories = categories,
                            onEdit = { category ->
                                categoryBeingEdited = category
                                showCategoryForm = true
                            },
                            onDeleteRequest = { category ->
                                categoryPendingDelete = category
                            }
                        )
                    }
                    MainDestination.Ledger -> {
                        LedgerScreen(
                            rows = ledgerRows,
                            categoriesEmpty = categories.isEmpty(),
                            onEdit = { tx ->
                                transactionBeingEdited = tx
                                showTransactionForm = true
                            },
                            onDeleteRequest = { tx ->
                                transactionPendingDelete = tx
                            }
                        )
                    }
                }

                if (showCategoryForm) {
                    CategoryFormDialog(
                        initialCategory = categoryBeingEdited,
                        onDismiss = {
                            showCategoryForm = false
                            categoryBeingEdited = null
                        },
                        onConfirm = { name, iconKey ->
                            val editing = categoryBeingEdited
                            if (editing == null) {
                                viewModel.addCategory(name, iconKey)
                            } else {
                                viewModel.updateCategory(
                                    editing.copy(
                                        name = name,
                                        iconKey = iconKey,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                            showCategoryForm = false
                            categoryBeingEdited = null
                        }
                    )
                }

                if (showTransactionForm) {
                    TransactionFormDialog(
                        initial = transactionBeingEdited,
                        categories = categories,
                        onDismiss = {
                            showTransactionForm = false
                            transactionBeingEdited = null
                        },
                        onConfirm = { entity ->
                            if (entity.id == 0L) {
                                ledgerViewModel.insertTransaction(entity)
                            } else {
                                ledgerViewModel.updateTransaction(entity)
                            }
                            showTransactionForm = false
                            transactionBeingEdited = null
                        }
                    )
                }

                categoryPendingDelete?.let { toDelete ->
                    AlertDialog(
                        onDismissRequest = { categoryPendingDelete = null },
                        title = { Text("카테고리 삭제") },
                        text = {
                            Text(
                                "\"${toDelete.name}\" 카테고리를 삭제할까요?\n" +
                                    "이 카테고리를 쓰는 거래 내역이 있으면 함께 삭제될 수 있습니다."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteCategory(toDelete)
                                    categoryPendingDelete = null
                                }
                            ) {
                                Text("삭제")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { categoryPendingDelete = null }) {
                                Text("취소")
                            }
                        }
                    )
                }

                transactionPendingDelete?.let { toDelete ->
                    AlertDialog(
                        onDismissRequest = { transactionPendingDelete = null },
                        title = { Text("거래 삭제") },
                        text = { Text("이 거래를 삭제할까요?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    ledgerViewModel.deleteTransaction(toDelete)
                                    transactionPendingDelete = null
                                }
                            ) {
                                Text("삭제")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { transactionPendingDelete = null }) {
                                Text("취소")
                            }
                        }
                    )
                }
            }
        }
    }
}
