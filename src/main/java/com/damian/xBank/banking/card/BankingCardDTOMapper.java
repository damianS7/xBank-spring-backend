package com.damian.xBank.banking.card;

import java.util.Set;
import java.util.stream.Collectors;

public class BankingCardDTOMapper {
    public static BankingCardDTO toBankingCardDTO(BankingCard bankingCard) {
        return new BankingCardDTO(
                bankingCard.getId(),
                bankingCard.getAssociatedBankingAccount().getId(),
                bankingCard.getHolderName(),
                bankingCard.getCardNumber(),
                bankingCard.getCardCvv(),
                bankingCard.getCardPin(),
                bankingCard.getDailyLimit(),
                bankingCard.getExpiredDate(),
                bankingCard.getCardType(),
                bankingCard.getCardStatus(),
                bankingCard.getLockStatus(),
                bankingCard.getCreatedAt(),
                bankingCard.getUpdatedAt()
        );
    }

    public static Set<BankingCardDTO> toBankingCardSetDTO(Set<BankingCard> bankingCards) {
        return bankingCards.stream().map(
                BankingCardDTOMapper::toBankingCardDTO
        ).collect(Collectors.toSet());
    }
}
