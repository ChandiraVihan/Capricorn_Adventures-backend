package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.RoomServiceOrder;
import com.capricorn_adventures.entity.User;

public interface RoomServicePushNotificationService {

    void sendOrderAssignmentNotification(User staffMember, RoomServiceOrder order);
}
