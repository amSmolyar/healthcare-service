package ru.netology.service.medical;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.entity.BloodPressure;
import ru.netology.entity.HealthInfo;
import ru.netology.entity.PatientInfo;
import ru.netology.repository.PatientInfoFileRepository;
import ru.netology.service.alert.SendAlertService;
import ru.netology.service.alert.SendAlertServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

class MedicalServiceImplTest {

    @ParameterizedTest
    @ValueSource(ints = {150, 145, 140, 155, 135, 130, 110, 115, 125})
    void checkBloodPressure_test(int highPressure) {
        // Mock для PatientInfoFileRepository:
        String id = String.valueOf(highPressure);   // id пациента
                PatientInfoFileRepository patientInfoFileRepositoryMock = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepositoryMock.getById(id))
                .thenReturn(new PatientInfo(id,"Иван", "Петров", LocalDate.of(1980, 11, 26),
                        new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80))));
        Mockito.when(patientInfoFileRepositoryMock.add(any())).thenReturn(id);

        // Spy для SendAlertService:
        SendAlertServiceImpl sendAlertServiceImplMock = Mockito.spy(SendAlertServiceImpl.class);

        // сообщение, которое должно быть выведено:
        String messageExpected = String.format("Warning, patient with id: %s, need help",id);
        // Давление пациента:
        BloodPressure bloodPressure = new BloodPressure(highPressure, (highPressure-40));

        // вызов метода checkBloodPressure:
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRepositoryMock, sendAlertServiceImplMock);
        medicalService.checkBloodPressure(id,bloodPressure);

        // 1ый вариант проверки:
        Mockito.verify(sendAlertServiceImplMock, Mockito.times(1)).send(messageExpected);

        // 2ой вариант проверки (с использованием ArgumentCaptor):
        ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertServiceImplMock, Mockito.times(1)).send(requestCaptor.capture());

        assertTrue((requestCaptor.getAllValues()).size() == 1);
        String capturedArgument = requestCaptor.getValue();
        assertEquals(messageExpected,capturedArgument);
    }

    @Test
    void checkTemperature_test() {
        // Spy для SendAlertService:
        SendAlertServiceImpl sendAlertServiceImplMock = Mockito.spy(SendAlertServiceImpl.class);
        // Mock для PatientInfoFileRepository:
        PatientInfoFileRepository patientInfoFileRepositoryMock = Mockito.mock(PatientInfoFileRepository.class);
        //
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRepositoryMock, sendAlertServiceImplMock);
        ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);
        for (int ii = 1; ii < 10; ii++) {
            String id = String.valueOf(ii);   // id пациента
            Mockito.when(patientInfoFileRepositoryMock.getById(anyString()))
                    .thenReturn(new PatientInfo(id,"Иван", "Петров", LocalDate.of(1980, 11, 26),
                            new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80))));
            Mockito.when(patientInfoFileRepositoryMock.add(any())).thenReturn(id);

            // сообщение, которое должно быть выведено:
            String messageExpected = String.format("Warning, patient with id: %s, need help",id);

            // Температура пациента:
            BigDecimal temperature = new BigDecimal(25 + 0.1*ii);
            // вызов метода checkTemperature:
            medicalService.checkTemperature(id,temperature);

            Mockito.verify(sendAlertServiceImplMock, Mockito.times(1)).send(messageExpected);

            // 2ой вариант проверки (с использованием ArgumentCaptor):
            Mockito.verify(sendAlertServiceImplMock, Mockito.times(ii)).send(requestCaptor.capture());
            String capturedArgument = requestCaptor.getValue();
            assertEquals(messageExpected,capturedArgument);
        }
        Mockito.verify(sendAlertServiceImplMock, Mockito.times(9)).send(any());

    }

    @Test
    void noMessage_test() {
        // Spy для SendAlertService:
        SendAlertServiceImpl sendAlertServiceImplMock = Mockito.spy(SendAlertServiceImpl.class);
        // Mock для PatientInfoFileRepository:
        PatientInfoFileRepository patientInfoFileRepositoryMock = Mockito.mock(PatientInfoFileRepository.class);
        //
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRepositoryMock, sendAlertServiceImplMock);
        for (int ii = 1; ii < 10; ii++) {
            String id = String.valueOf(ii);   // id пациента
            Mockito.when(patientInfoFileRepositoryMock.getById(anyString()))
                    .thenReturn(new PatientInfo(id,"Иван", "Петров", LocalDate.of(1980, 11, 26),
                            new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120-ii, 80))));
            Mockito.when(patientInfoFileRepositoryMock.add(any())).thenReturn(id);

            // Давление пациента:
            BloodPressure bloodPressure = new BloodPressure(120-ii, 80);
            // Температура пациента:
            BigDecimal temperature = new BigDecimal(36 + 0.1*ii);
            // вызов метода checkBloodPressure:
            medicalService.checkBloodPressure(id,bloodPressure);
            // вызов метода checkTemperature:
            medicalService.checkTemperature(id,temperature);

            Mockito.verify(sendAlertServiceImplMock, Mockito.never()).send(any());
        }
        Mockito.verify(sendAlertServiceImplMock, Mockito.never()).send(any());

    }
}