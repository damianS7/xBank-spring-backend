package com.damian.xBank.customer.profile;

public class ProfileDTOMapper {
    public static ProfileDTO toProfileDTO(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getBirthdate(),
                profile.getGender(),
                profile.getPhotoPath(),
                profile.getAddress(),
                profile.getPostalCode(),
                profile.getCountry(),
                profile.getNationalId(),
                profile.getUpdatedAt()
        );
    }
}
