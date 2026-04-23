package com.jaehwan.moneybook.ui.root

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.category.ui.CategoryFormDialog
import com.jaehwan.moneybook.category.ui.CategoryList
import com.jaehwan.moneybook.category.ui.CategoryViewModel
import com.jaehwan.moneybook.splitmember.ui.SplitEditorScreen
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import com.jaehwan.moneybook.transaction.ui.LedgerScreen
import com.jaehwan.moneybook.transaction.ui.TradeAllTransactionsScreen
import com.jaehwan.moneybook.transaction.ui.TradeScreen
import com.jaehwan.moneybook.transaction.ui.LedgerViewModel
import com.jaehwan.moneybook.transaction.ui.TransactionDetailScreen
import com.jaehwan.moneybook.transaction.ui.LedgerRow
import com.jaehwan.moneybook.transaction.ui.TransactionEntryScreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyBookApp(viewModel: CategoryViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val ledgerViewModel: LedgerViewModel = hiltViewModel()
    val categories by viewModel.categories.collectAsState()
    val ledgerRows by ledgerViewModel.ledgerRows.collectAsState()
    val installmentSummary by ledgerViewModel.installmentSummary.collectAsState()
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    var destination by remember { mutableStateOf(MainDestination.Home) }
    var workingPopup by remember { mutableStateOf<MainDestination?>(null) }
    var settingsSection by remember { mutableStateOf(SettingsSection.Root) }
    var showCategoryForm by remember { mutableStateOf(false) }
    var categoryBeingEdited by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryPendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    var showTransactionEntry by remember { mutableStateOf(false) }
    var transactionBeingEdited by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionPendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    var showSplitEditor by remember { mutableStateOf(false) }
    var splitEditorTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var splitPrefill by remember { mutableStateOf<Triple<Int, Long, String?>?>(null) }
    var selectedDetailTransactionId by remember { mutableStateOf<Long?>(null) }
    val selectedDetailRow = selectedDetailTransactionId?.let { selectedId ->
        ledgerRows.firstOrNull { it.transaction.id == selectedId }
    }
    var lastBackPressedAt by remember { mutableStateOf(0L) }
    var tradeSubRoute by remember { mutableStateOf(TradeSubRoute.Monthly) }

    BackHandler {
        when {
            selectedDetailTransactionId != null -> selectedDetailTransactionId = null
            showSplitEditor -> {
                showSplitEditor = false
                splitEditorTransaction = null
                splitPrefill = null
            }
            showTransactionEntry -> {
                showTransactionEntry = false
                transactionBeingEdited = null
            }
            showCategoryForm -> {
                showCategoryForm = false
                categoryBeingEdited = null
            }
            transactionPendingDelete != null -> transactionPendingDelete = null
            categoryPendingDelete != null -> categoryPendingDelete = null
            workingPopup != null -> workingPopup = null
            settingsSection != SettingsSection.Root -> settingsSection = SettingsSection.Root
            destination == MainDestination.Trade && tradeSubRoute == TradeSubRoute.All -> {
                tradeSubRoute = TradeSubRoute.Monthly
            }
            destination != MainDestination.Home -> destination = MainDestination.Home
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressedAt < 2000L) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressedAt = now
                    Toast.makeText(context, "한 번 더 뒤로가기를 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                listOf(
                    MainDestination.Home to ("홈" to Icons.Default.Home),
                    MainDestination.Trade to ("거래" to Icons.Default.Menu),
                    MainDestination.Report to ("리포트" to Icons.Default.Add),
                    MainDestination.Settings to ("설정" to Icons.Default.Settings),
                ).forEach { (dest, meta) ->
                    val (label, icon) = meta
                    NavigationBarItem(
                        selected = destination == dest,
                        onClick = {
                            selectedDetailTransactionId = null
                            when (dest) {
                                MainDestination.Home,
                                MainDestination.Settings,
                                MainDestination.Trade -> {
                                    if (dest == MainDestination.Trade && destination != MainDestination.Trade) {
                                        tradeSubRoute = TradeSubRoute.Monthly
                                    }
                                    destination = dest
                                }
                                MainDestination.Report -> workingPopup = dest
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                    )
                }
            }
        },
        floatingActionButton = {
            when {
                destination == MainDestination.Settings && settingsSection == SettingsSection.CategoryManager -> {
                    FloatingActionButton(
                        onClick = {
                            categoryBeingEdited = null
                            showCategoryForm = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "카테고리 추가")
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (destination) {
                MainDestination.Home -> {
                    LedgerScreen(
                        rows = ledgerRows,
                        installmentSummary = installmentSummary,
                        categoriesEmpty = categories.isEmpty(),
                        onEdit = { tx ->
                            if (TransactionType.fromKey(tx.type) == TransactionType.SPLIT) {
                                splitEditorTransaction = tx
                                splitPrefill = null
                                showSplitEditor = true
                            } else {
                                transactionBeingEdited = tx
                                showTransactionEntry = true
                            }
                        },
                        onDeleteRequest = { tx ->
                            transactionPendingDelete = tx
                        },
                        onSplitMemberPaidToggle = { member ->
                            ledgerViewModel.updateSplitMember(member)
                        },
                        onOpenDetail = { row -> selectedDetailTransactionId = row.transaction.id },
                        showActionButtons = false,
                        allowInlineSplitExpand = false,
                        onViewAll = { workingPopup = MainDestination.Trade },
                    )
                }

                MainDestination.Trade -> {
                    when (tradeSubRoute) {
                        TradeSubRoute.Monthly -> {
                            TradeScreen(
                                rows = ledgerRows,
                                installmentSummary = installmentSummary,
                                onAddClick = {
                                    transactionBeingEdited = null
                                    showTransactionEntry = true
                                },
                                onOpenDetail = { row -> selectedDetailTransactionId = row.transaction.id },
                                onSplitMemberPaidToggle = { member -> ledgerViewModel.updateSplitMember(member) },
                                onInstallmentPaidToggle = { payment -> ledgerViewModel.updateInstallmentPayment(payment) },
                                onOpenAllTransactions = { tradeSubRoute = TradeSubRoute.All },
                            )
                        }
                        TradeSubRoute.All -> {
                            TradeAllTransactionsScreen(
                                rows = ledgerRows,
                                onBack = { tradeSubRoute = TradeSubRoute.Monthly },
                                onAddClick = {
                                    transactionBeingEdited = null
                                    showTransactionEntry = true
                                },
                                onOpenDetail = { row -> selectedDetailTransactionId = row.transaction.id },
                                onSplitMemberPaidToggle = { member -> ledgerViewModel.updateSplitMember(member) },
                                onInstallmentPaidToggle = { payment -> ledgerViewModel.updateInstallmentPayment(payment) },
                                onDeleteTransactions = { txs -> ledgerViewModel.deleteTransactions(txs) },
                            )
                        }
                    }
                }

                MainDestination.Settings -> {
                    if (settingsSection == SettingsSection.CategoryManager) {
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
                    } else if (settingsSection == SettingsSection.LegacyLedger) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextButton(onClick = { settingsSection = SettingsSection.Root }) {
                                Text("설정으로 돌아가기")
                            }
                            LedgerScreen(
                                rows = ledgerRows,
                                installmentSummary = installmentSummary,
                                categoriesEmpty = categories.isEmpty(),
                                onEdit = { tx ->
                                    if (TransactionType.fromKey(tx.type) == TransactionType.SPLIT) {
                                        splitEditorTransaction = tx
                                        splitPrefill = null
                                        showSplitEditor = true
                                    } else {
                                        transactionBeingEdited = tx
                                        showTransactionEntry = true
                                    }
                                },
                                onDeleteRequest = { tx ->
                                    transactionPendingDelete = tx
                                },
                                onSplitMemberPaidToggle = { member ->
                                    ledgerViewModel.updateSplitMember(member)
                                },
                                onOpenDetail = { row -> selectedDetailTransactionId = row.transaction.id },
                                showActionButtons = true,
                                allowInlineSplitExpand = true,
                                onViewAll = { workingPopup = MainDestination.Trade },
                            )
                        }
                    } else {
                        SettingsScreen(
                            onOpenCategoryManager = { settingsSection = SettingsSection.CategoryManager },
                            onOpenLegacyTrade = { settingsSection = SettingsSection.LegacyLedger },
                        )
                    }
                }
                MainDestination.Report -> Unit
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

            if (showTransactionEntry) {
                TransactionEntryScreen(
                    categories = categories,
                    initial = transactionBeingEdited,
                    initialInstallmentPlan = transactionBeingEdited?.let { tx ->
                        ledgerRows.firstOrNull { it.transaction.id == tx.id }?.installmentPlan
                    },
                    onDismiss = {
                        showTransactionEntry = false
                        transactionBeingEdited = null
                    },
                    onSaveNormal = { entity, installmentInput ->
                        if (entity.id == 0L) {
                            if (installmentInput != null) {
                                ledgerViewModel.insertTransactionWithInstallment(
                                    transaction = entity,
                                    installmentTotalAmount = installmentInput.totalAmount,
                                    installmentMonths = installmentInput.months,
                                    installmentStartDate = installmentInput.startDate,
                                )
                            } else {
                                ledgerViewModel.insertTransaction(entity)
                            }
                        } else {
                            if (installmentInput != null) {
                                ledgerViewModel.updateTransactionWithInstallment(
                                    transaction = entity,
                                    installmentTotalAmount = installmentInput.totalAmount,
                                    installmentMonths = installmentInput.months,
                                    installmentStartDate = installmentInput.startDate,
                                )
                            } else {
                                ledgerViewModel.updateTransaction(entity)
                                ledgerViewModel.clearInstallment(entity.id)
                            }
                        }
                        showTransactionEntry = false
                        transactionBeingEdited = null
                    },
                    onSaveSplit = { entity, members ->
                        if (entity.id == 0L) {
                            ledgerViewModel.insertSplit(entity, members)
                        } else {
                            ledgerViewModel.updateSplit(entity, members)
                        }
                        showTransactionEntry = false
                        transactionBeingEdited = null
                    },
                )
            }

            if (showSplitEditor) {
                val initialTx = splitEditorTransaction
                val prefill = splitPrefill
                val total = initialTx?.amount ?: prefill?.first ?: 0
                val categoryId = initialTx?.categoryId ?: prefill?.second ?: 0L
                val memo = initialTx?.memo ?: prefill?.third
                if (total > 0 && categoryId != 0L) {
                    SplitEditorScreen(
                        initialTransaction = initialTx,
                        prefilledTotal = total,
                        prefilledCategoryId = categoryId,
                        prefilledMemo = memo,
                        ledgerViewModel = ledgerViewModel,
                        onDismiss = {
                            showSplitEditor = false
                            splitEditorTransaction = null
                            splitPrefill = null
                        },
                        onSave = { entity, members ->
                            if (entity.id == 0L) {
                                ledgerViewModel.insertSplit(entity, members)
                            } else {
                                ledgerViewModel.updateSplit(entity, members)
                            }
                            showSplitEditor = false
                            splitEditorTransaction = null
                            splitPrefill = null
                        },
                    )
                }
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

            workingPopup?.let { pending ->
                val label = if (pending == MainDestination.Trade) "거래" else "리포트"
                AlertDialog(
                    onDismissRequest = { workingPopup = null },
                    title = { Text(label) },
                    text = { Text("아직 작업중입니다.") },
                    confirmButton = {
                        TextButton(onClick = { workingPopup = null }) {
                            Text("확인")
                        }
                    },
                )
            }

            selectedDetailRow?.let { detailRow ->
                TransactionDetailScreen(
                    row = detailRow,
                    onBack = { selectedDetailTransactionId = null },
                    onEdit = { tx ->
                        selectedDetailTransactionId = null
                        if (TransactionType.fromKey(tx.type) == TransactionType.SPLIT) {
                            splitEditorTransaction = tx
                            splitPrefill = null
                            showSplitEditor = true
                        } else {
                            transactionBeingEdited = tx
                            showTransactionEntry = true
                        }
                    },
                    onDeleteRequest = { tx ->
                        selectedDetailTransactionId = null
                        transactionPendingDelete = tx
                    },
                    onSplitMemberPaidToggle = { member -> ledgerViewModel.updateSplitMember(member) },
                    onInstallmentPaidToggle = { payment -> ledgerViewModel.updateInstallmentPayment(payment) },
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    onOpenCategoryManager: () -> Unit,
    onOpenLegacyTrade: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "설정",
            style = MaterialTheme.typography.headlineSmall,
        )
        FilledTonalButton(
            onClick = onOpenCategoryManager,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("카테고리 관리")
        }
        FilledTonalButton(
            onClick = onOpenLegacyTrade,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("거래 내용 (이전 가계부)")
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "가계부",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private enum class SettingsSection {
    Root,
    CategoryManager,
    LegacyLedger,
}

private enum class TradeSubRoute {
    Monthly,
    All,
}
