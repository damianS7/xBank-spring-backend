package com.damian.xBank.banking.card;

public class BankingCardDTOMapper {
    public static BankingCardDTO toBankingCardDTO(BankingCard bankingCard) {
        return new BankingCardDTO(
                bankingCard.getId(),
                bankingCard.getCardNumber(),
                bankingCard.getCardCvv(),
                bankingCard.getCardPin(),
                bankingCard.getExpiredDate(),
                bankingCard.getCardType(),
                bankingCard.getCardStatus(),
                bankingCard.getCreatedAt(),
                bankingCard.getUpdatedAt()
        );
    }
}
