package ru.drone.navigator.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainWindow extends Application {

    private static WebEngine webEngine;
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    // Начальные координаты дрона (Москва)
    private static double droneLat = 55.7558;
    private static double droneLon = 37.6173;

    // Конечная точка, куда дрон должен лететь
    private static double targetLat = 55.7558;
    private static double targetLon = 37.6173;
    private static boolean isMoving = false;
    private static double batteryPercent = 100.0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Drone Navigator & Tracker B2B - Автономная симуляция");

        WebView webView = new WebView();
        webEngine = webView.getEngine();

        // Перенаправляем логи JavaScript в консоль Java
        webEngine.setOnAlert(event -> {

            String data = event.getData();

            System.out.println("[JS Alert] " + data);

            if (data.startsWith("FLY_TO:")) {

                String coords =
                        data.replace("FLY_TO:", "");

                String[] parts =
                        coords.split(",");

                double lat =
                        Double.parseDouble(parts[0]);

                double lon =
                        Double.parseDouble(parts[1]);

                System.out.println(
                        "[Навигатор] Новая цель: "
                                + lat
                                + ", "
                                + lon
                );

                targetLat = lat;
                targetLon = lon;
                isMoving = true;
            }
        });

        URL url = getClass().getResource("/map.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        }

        // Настраиваем мост между JS и Java
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
//                JSObject window = (JSObject) webEngine.executeScript("window");
//                window.setMember("javaApp", new MapBridge());
                System.out.println("Мост Java-JS успешно запущен.");

                // Запускаем игровой цикл симулятора (обновление каждые 100 миллисекунд)
                startFlightEngine();
            }
        });

        BorderPane root = new BorderPane();
        root.setCenter(webView);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Движок симуляции полета. Считает векторы движения и передвигает маркер на карте Яндекса.
     */
    private void startFlightEngine() {
        executor.scheduleAtFixedRate(() -> {
            if (isMoving) {
                System.out.println("Летим: "
                + droneLat
                + ", "
                + droneLon);
                // Вычисляем расстояние (вектор) до цели
                double deltaLat = targetLat - droneLat;
                double deltaLon = targetLon - droneLon;
                double distance = Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);

                // Скорость сближения
                double step = 0.0001;

                if (distance > step) {
                    // Шагаем по вектору в сторону цели
                    droneLat += (deltaLat / distance) * step;
                    droneLon += (deltaLon / distance) * step;

                    // Тратим батарею при полете
                    if (batteryPercent > 0) batteryPercent -= 0.1;
                } else {
                    // Прилетели в точку
                    droneLat = targetLat;
                    droneLon = targetLon;
                    isMoving = false;
                    System.out.println("[Навигатор] Дрон успешно прибыл в заданную точку пути!");
                }

                // Передаем новые координаты на карту Яндекса (вызываем JS функцию)
                Platform.runLater(() -> {
                    String jsCommand = String.format(java.util.Locale.US,"setDroneMarker(%.6f, %.6f);", droneLat, droneLon);
                    System.out.println(jsCommand);
                    webEngine.executeScript(jsCommand);
                });
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() throws Exception {
        executor.shutdown();
        super.stop();
    }
    /**
     * Класс-мост для приема кликов с карты
     */

    public static void setTarget(double lat, double lon) {

        System.out.println(
                "[Навигатор] Новая цель: "
                        + lat + ", "
                        + lon
        );

        targetLat = lat;
        targetLon = lon;
        isMoving = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}