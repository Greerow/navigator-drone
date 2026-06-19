package ru.drone.navigator.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MockDroneService {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private double currentLat = 55.7558; // Исходная точка дрона (Москва)
    private double currentLon = 37.6173;
    private double targetLat;
    private double targetLon;
    private boolean isFlying = false;

    public void startSimulation() {
        System.out.println("[Симулятор ИИ] Эмулятор дрона успешно запущен локально!");

        // Каждые 2 секунды имитируем отправку заряда батареи в консоль
        executor.scheduleAtFixedRate(() -> {
            int mockBattery = 89; // Всегда отличный заряд для теста
            System.out.println("[Телеметрия] Виртуальная батарея дрона: " + mockBattery + "%");

            if (isFlying) {
                // Плавно двигаем координаты в сторону клика на карте
                currentLat += (targetLat - currentLat) * 0.2;
                currentLon += (targetLon - currentLon) * 0.2;
                System.out.printf("[Телеметрия] Дрон летит -> Текущие координаты: %.6f, %.6f\n", currentLat, currentLon);

                // Если подлетели близко — останавливаемся
                if (Math.abs(currentLat - targetLat) < 0.0001 && Math.abs(currentLon - targetLon) < 0.0001) {
                    isFlying = false;
                    System.out.println("[Телеметрия] Дрон успешно прибыл в конечную точку пути!");
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    public void sendDroneToLocation(double lat, double lon, float altitude) {
        this.targetLat = lat;
        this.targetLon = lon;
        this.isFlying = true;
        System.out.printf("[Симулятор ИИ] Получен вектор движения. Скорость сближения: 7 м/с. Направление на цель: %.6f, %.6f\n", lat, lon);
    }
}