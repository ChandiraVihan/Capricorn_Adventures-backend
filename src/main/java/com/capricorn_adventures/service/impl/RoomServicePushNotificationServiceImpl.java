package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.entity.RoomServiceOrder;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.service.RoomServicePushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoomServicePushNotificationServiceImpl implements RoomServicePushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RoomServicePushNotificationServiceImpl.class);

    @Override
    public void sendOrderAssignmentNotification(User staffMember, RoomServiceOrder order) {
        log.info("Push notification queued for staff {} about room service order {} (room {}, items: {})",
                staffMember.getEmail(),
                order.getId(),
                order.getRoomNumber(),
                String.join(", ", order.getItemsOrdered()));
    }
}
