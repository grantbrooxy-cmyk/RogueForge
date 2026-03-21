package com.rogueforge.game.event;

/**
 * Event fired when currency is earned (from defeating enemies, rewards, etc).
 */
public class CurrencyEarnedEvent {
    private long amount;
    private String source;

    public CurrencyEarnedEvent(long amount, String source) {
        this.amount = amount;
        this.source = source;
    }

    public long getAmount() {
        return amount;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "CurrencyEarnedEvent{" +
                "amount=" + amount +
                ", source='" + source + '\'' +
                '}';
    }
}
