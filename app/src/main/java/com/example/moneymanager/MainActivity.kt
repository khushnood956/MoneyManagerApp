package com.example.moneymanager

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Person(
    val name: String,
    val balance: Int
)

data class Transaction(
    val personName: String,
    val amount: Int,
    val type: String,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MoneyManagerScreen()
            }
        }
    }
}

@Composable
fun MoneyManagerScreen(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("money_manager", Context.MODE_PRIVATE)

    var persons by remember { mutableStateOf(loadPersons(prefs)) }
    var transactions by remember { mutableStateOf(loadTransactions(prefs)) }
    var nameInput by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(persons, transactions) {
        savePersons(prefs, persons)
        saveTransactions(prefs, transactions)
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Money Manager", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Enter Person Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val newPersonName = nameInput.text.trim()
                if (newPersonName.isNotBlank() && persons.none { it.name == newPersonName }) {
                    persons = persons + Person(newPersonName, 0)
                    nameInput = TextFieldValue("")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Person")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(persons) { person ->
                PersonItem(
                    person = person,
                    transactions = transactions.filter { it.personName == person.name },
                    onMoneyIn = { amount: Int ->
                        persons = persons.map {
                            if (it == person) Person(it.name, it.balance + amount) else it
                        }
                        transactions = transactions + Transaction(
                            personName = person.name,
                            amount = amount,
                            type = "Money In",
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        )
                    },
                    onMoneyOut = { amount: Int ->
                        persons = persons.map {
                            if (it == person) Person(it.name, it.balance - amount) else it
                        }
                        transactions = transactions + Transaction(
                            personName = person.name,
                            amount = -amount,
                            type = "Money Out",
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        )
                    },
                    onDelete = {
                        persons = persons - person
                        transactions = transactions.filter { it.personName != person.name }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val totalBalance by remember { derivedStateOf { persons.sumOf { it.balance } } }
        Text(
            text = "Total Balance: Rs $totalBalance",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Developed by Khushnood Ahmad © 2025",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun PersonItem(
    person: Person,
    transactions: List<Transaction>,
    onMoneyIn: (Int) -> Unit,
    onMoneyOut: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var amountInput by remember { mutableStateOf(TextFieldValue("")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${person.name}: Rs ${person.balance}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val amount = amountInput.text.toIntOrNull()
                        if (amount != null && amount > 0) {
                            onMoneyIn(amount)
                            amountInput = TextFieldValue("")
                        }
                    },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Money In")
                }

                Button(
                    onClick = {
                        val amount = amountInput.text.toIntOrNull()
                        if (amount != null && amount > 0) {
                            onMoneyOut(amount)
                            amountInput = TextFieldValue("")
                        }
                    },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("Money Out")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isNotEmpty()) {
                Text(
                    text = "Transaction History:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                transactions.forEach { transaction ->
                    Text(
                        text = "${transaction.type}: ${if (transaction.amount > 0) "+" else ""}${transaction.amount} on ${transaction.date}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDelete() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    }
}

private fun savePersons(prefs: android.content.SharedPreferences, persons: List<Person>) {
    val personsString = persons.joinToString(";") { "${it.name},${it.balance}" }
    prefs.edit().putString("persons", personsString).apply()
}

private fun loadPersons(prefs: android.content.SharedPreferences): List<Person> {
    val personsString = prefs.getString("persons", "") ?: ""
    if (personsString.isEmpty()) return emptyList()
    return personsString.split(";").mapNotNull {
        val parts = it.split(",")
        if (parts.size == 2) {
            try {
                Person(parts[0], parts[1].toInt())
            } catch (e: NumberFormatException) {
                null
            }
        } else {
            null
        }
    }
}

private fun saveTransactions(prefs: android.content.SharedPreferences, transactions: List<Transaction>) {
    val transactionsString = transactions.joinToString(";") {
        "${it.personName},${it.amount},${it.type},${it.date}"
    }
    prefs.edit().putString("transactions", transactionsString).apply()
}

private fun loadTransactions(prefs: android.content.SharedPreferences): List<Transaction> {
    val transactionsString = prefs.getString("transactions", "") ?: ""
    if (transactionsString.isEmpty()) return emptyList()
    return transactionsString.split(";").mapNotNull {
        val parts = it.split(",")
        if (parts.size == 4) {
            try {
                Transaction(parts[0], parts[1].toInt(), parts[2], parts[3])
            } catch (e: NumberFormatException) {
                null
            }
        } else {
            null
        }
    }
}