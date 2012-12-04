/*
 * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
 */
package poker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A set of poker cards
 *
 * @author Cameron Zemek <grom358@gmail.com>
 */
public final class CardSet {
    private long bitset;

    public CardSet() {
        bitset = 0;
    }

    CardSet(long val) {
        bitset = val;
    }

    public CardSet(CardSet cs) {
        bitset = cs.bitset;
    }

    public CardSet(List<Card> cardList) {
        bitset = 0;
        addAll(cardList);
    }

    public boolean has(Card c) {
        return (bitset & c.longValue()) != 0;
    }

    public void add(Card c) {
        bitset |= c.longValue();
    }

    public void addAll(Collection<Card> cards) {
        for (Card card : cards) {
            add(card);
        }
    }

    public void insect(CardSet cs) {
        bitset &= cs.bitset;
    }

    public void union(CardSet cs) {
        bitset |= cs.bitset;
    }

    public void subtract(CardSet cs) {
        bitset &= ~cs.bitset;
    }

    public int size() {
        return Long.bitCount(bitset);
    }

    long longValue() {
        return bitset;
    }

    private int nextSetBit(int fromIndex) {
        long mask = 0xffffffffffffffffL << fromIndex;
        long word = bitset & mask;
        if (word == 0) {
            return -1;
        }
        return Long.numberOfTrailingZeros(word);
    }

    public List<Card> toList() {
        return subList(size());
    }

    List<Card> subList(int max) {
        List<Card> cards = new ArrayList<>(max);
        for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
            cards.add(Card.valueOf(i));
            if (cards.size() >= max) {
                break;
            }
        }
        return cards;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int i = nextSetBit(0);
        if (i >= 0) {
            sb.append(Card.valueOf(i));
        }
        for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
            sb.append(',');
            sb.append(Card.valueOf(i));
        }
        sb.append(']');
        return sb.toString();
    }
}
