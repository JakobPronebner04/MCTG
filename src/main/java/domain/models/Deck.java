package domain.models;

import java.util.Collections;
import java.util.Stack;

public class Deck
{
    private Stack<Card> cards = new Stack<>();

    public void addCard(Card card) {
        cards.push(card);
    }

    public void shuffle()
    {
        Collections.shuffle(cards);
    }
    public int getSize()
    {
        return cards.size();
    }
    public Card getTopCard()
    {
        return cards.pop();
    }
    public String formattedCards()
    {
        StringBuilder deckDetails = new StringBuilder();
        for(Card c : cards)
        {
            deckDetails.append("ID: ").append(c.getId()).append(" | ")
                    .append("Name: ").append(c.getName()).append(" | ")
                    .append("CardType: ").append(c.getCardType()).append(" | ")
                    .append("ElementType: ").append(c.getElementType()).append(" | ")
                    .append("Damage:").append(c.getDamage()).append(System.lineSeparator());
        }
        return deckDetails.toString();
    }

    public String formattedPlainCards()
    {
        StringBuilder deckDetails = new StringBuilder();
        for(Card c : cards)
        {
            deckDetails.append(c.getId()).append(System.lineSeparator())
                    .append(c.getName()).append(System.lineSeparator())
                    .append(c.getCardType()).append(System.lineSeparator())
                    .append(c.getElementType()).append(System.lineSeparator())
                    .append(c.getDamage()).append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        return deckDetails.toString();
    }
}