package com.capricorn_adventures.entity;

public enum CarRentalStatus {
    /** Car is held by the partner while the user completes checkout. */
    RESERVED,

    /** Payment completed – reservation is locked with the partner. */
    CONFIRMED,

    /** User removed the car add-on from the cart; hold released at partner. */
    RELEASED,

    /** Post-payment cancellation of the adventure; partner notified. */
    CANCELLED
}
