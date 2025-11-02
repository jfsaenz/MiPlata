package com.miplata.domain;

import java.util.ArrayList;
import java.util.List;
import com.miplata.data.Transaction;

public class FinanceManager {
    private final List<Transaction> transactions = new ArrayList<>();

    public void processTransaction(Transaction t) {
        if (t != null) transactions.add(t);
    }

    public double totalGastos() {
        return transactions.stream()
                .filter(tx -> "DEBIT".equals(tx.getType()))
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double totalIngresos() {
        return transactions.stream()
                .filter(tx -> "CREDIT".equals(tx.getType()))
                .mapToDouble(Transaction::getAmount).sum();
    }
}
