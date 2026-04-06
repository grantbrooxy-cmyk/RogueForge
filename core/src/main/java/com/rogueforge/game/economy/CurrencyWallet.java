package com.rogueforge.game.economy;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.core.EventHandler;
import com.rogueforge.game.event.CurrencyEarnedEvent;

/**
 * Manages player currency balance.
 * Subscribes to CurrencyEarnedEvent to auto-credit earnings.
 * Serializable for JSON persistence.
 */
public class CurrencyWallet {
    private long balance;

    // No-arg constructor for JSON serialization
    public CurrencyWallet() {
        this.balance = 0;
    }

    public CurrencyWallet(long initialBalance) {
        this.balance = initialBalance;
    }

    /**
     * Adds currency to the wallet.
     *
     * @param amount The amount to earn
     */
    public void earn(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot earn negative amount");
        }
        balance += amount;
    }

    /**
     * Removes currency from the wallet.
     *
     * @param amount The amount to spend
     * @throws IllegalArgumentException if insufficient funds
     */
    public void spend(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot spend negative amount");
        }
        if (amount > balance) {
            throw new IllegalArgumentException(
                String.format("Insufficient funds: need %d, have %d", amount, balance)
            );
        }
        balance -= amount;
    }

    /**
     * Attempts to spend currency, returning success without throwing.
     *
     * @param amount The amount to spend
     * @return true if successful, false if insufficient funds
     */
    public boolean trySpend(long amount) {
        if (amount < 0) {
            return false;
        }
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    /**
     * Checks if wallet can afford a cost.
     *
     * @param cost The cost to check
     * @return true if balance >= cost
     */
    public boolean canAfford(long cost) {
        return balance >= cost;
    }

    /**
     * Gets the current balance.
     *
     * @return The current balance
     */
    public long getBalance() {
        return balance;
    }

    /**
     * Sets the balance directly (for serialization/testing).
     *
     * @param balance The new balance
     */
    public void setBalance(long balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        this.balance = balance;
    }

    /**
     * Resets the wallet to zero balance.
     */
    public void reset() {
        balance = 0;
    }

    /**
     * Subscribes this wallet to the EventBus to listen for CurrencyEarnedEvents.
     *
     * @param eventBus The event bus to subscribe to
     */
    public void subscribeToEvents(EventBus eventBus) {
        eventBus.subscribe(this);
    }

    /**
     * Event handler for CurrencyEarnedEvent.
     * Called automatically when subscribed to EventBus.
     *
     * @param event The currency earned event
     */
    @EventHandler
    public void onCurrencyEarned(CurrencyEarnedEvent event) {
        earn(event.getAmount());
    }

    @Override
    public String toString() {
        return "CurrencyWallet{" +
                "balance=" + balance +
                '}';
    }
}
