package ru.drone.navigator.ui;



public class MapBridge {

    public void onMapClick(double lat, double lon) {

        System.out.println("ON MAP CLICK ВЫЗВАН");

        MainWindow.setTarget(lat, lon);
    }
}