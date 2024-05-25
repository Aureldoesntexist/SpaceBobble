module com.bobble.spacebobble {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;

    opens com.bobble.spacebobble to javafx.fxml;
    exports com.bobble.spacebobble;
    exports com.bobble.spacebobble.ui;
    exports com.bobble.spacebobble.controller;
    exports com.bobble.spacebobble.core;
    exports com.bobble.spacebobble.core.entities;
    exports com.bobble.spacebobble.gestion;
    exports com.bobble.spacebobble.core.scores;
    exports com.bobble.spacebobble.core.utilities;
    exports com.bobble.spacebobble.core.world;
    exports com.bobble.spacebobble.config;
    exports com.bobble.spacebobble.controller.service;
    exports com.bobble.spacebobble.core.entities.Server;
    exports com.bobble.spacebobble.network;
    exports com.bobble.spacebobble.network.Packet;
}