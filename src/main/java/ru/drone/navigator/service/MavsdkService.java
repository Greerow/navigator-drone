package ru.drone.navigator.service;

import io.mavsdk.System;
import io.mavsdk.action.Action;
import io.mavsdk.telemetry.Telemetry;
import io.reactivex.schedulers.Schedulers;

public class MavsdkService {

    private System drone;
    private Action action;
    private Telemetry telemetry;

    /**
     * Подключение к дрону (для симулятора используем localhost)
     */
    public void connect(String host, int port) {
        java.lang.System.out.println("[MAVSDK] Попытка подключения к дрону: " + host + ":" + port);

        // Инициализируем систему MAVSDK (по умолчанию порт gRPC прокси)
        drone = new System(host, port);
        action = drone.getAction();
        telemetry = drone.getTelemetry();

        // Запускаем фоновый мониторинг батареи, чтобы видеть, что связь есть
        monitorBattery();
    }

    /**
     * Метод отправки дрона в точку по заданному вектору (координатам)
     */
    /**
     * Метод отправки дрона в точку по заданному вектору (координатам)
     */
    public void flyToLocation(double lat, double lon, float altitude) {
        if (action == null) {
            java.lang.System.err.println("[MAVSDK] Ошибка: Нет подключения к дрону!");
            return;
        }

        java.lang.System.out.println("[MAVSDK] Команда: Лететь в точку Lat=" + lat + ", Lon=" + lon);

        // Базовая безопасная цепочка: арминг -> взлет -> полет
        action.arm()
                .andThen(action.takeoff())
                .andThen(action.gotoLocation(lat, lon, altitude, 0f))
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> java.lang.System.out.println("[MAVSDK] Команда на полет принята."),
                        throwable -> java.lang.System.err.println("[MAVSDK] Ошибка: " + throwable.getMessage())
                );
    }

    /**
     * Постоянное прослушивание заряда батареи в фоновом потоке
     */
    private void monitorBattery() {
        telemetry.getBattery()
                .subscribeOn(Schedulers.io())
                .distinctUntilChanged()
                .subscribe(battery -> {
                    int percent = (int) (battery.getRemainingPercent() * 100);
                    java.lang.System.out.println("[Телеметрия] Батарея дрона: " + percent + "%");
                }, throwable -> {
                    java.lang.System.err.println("Ошибка получения телеметрии: " + throwable.getMessage());
                });
    }
}