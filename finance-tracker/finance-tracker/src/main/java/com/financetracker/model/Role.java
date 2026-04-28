package com.financetracker.model;

/**
 * User roles for role-based access control.
 * USER  → regular user (can manage own transactions)
 * ADMIN → can view all users and transactions
 */
public enum Role {
    USER,
    ADMIN
}
