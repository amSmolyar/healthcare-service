package ru.netology.service.medical;

import java.math.BigDecimal;

import ru.netology.entity.BloodPressure;

public interface MedicalService {

    void checkBloodPressure(String patientId, BloodPressure bloodPressure);

    void checkTemperature(String patientId, BigDecimal temperature);
}
