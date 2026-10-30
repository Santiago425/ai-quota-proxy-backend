package com.aiplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ClockService {

    private final ZoneId zone;

    public ClockService(@Value("${app.zone:America/Bogota}") String zone) {
        this.zone = ZoneId.of(zone);
    }

    public ZonedDateTime now() {
        return ZonedDateTime.now(zone);
    }

    public LocalDate today() {
        return now().toLocalDate();
    }

    public LocalDate nextMonthlyReset() {
        return today().withDayOfMonth(1).plusMonths(1);
    }

    public long secondsUntilNextMinute() {
        ZonedDateTime current = now();
        ZonedDateTime next = current.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        return Math.max(1, (long) Math.ceil(Duration.between(current, next).toMillis() / 1000.0));
    }

    public ZoneId zone() {
        return zone;
    }
}
