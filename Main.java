import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main extends Application {

    // 🔑 API KEY
    private static final String API_KEY = "6ed43d61560a8a065159059d7523a1b8";

    // 📁 history file
    private static final String HISTORY_FILE = "weather_history.txt";

    // 🌡️ temperature unit
    private boolean isCelsius = true;

    // 🕘 history area
    private TextArea historyArea = new TextArea();

    @Override
    public void start(Stage stage) {

        // App title
        Label title = new Label("🌤 Weather Information App");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Input field
        TextField cityInput = new TextField();
        cityInput.setPromptText("Enter city (e.g., Kabul, AF)");

        // Buttons
        Button searchButton = new Button("Search Weather");
        Button unitButton = new Button("Switch to °F");

        // Result labels
        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-font-size: 14px;");

        Label forecastLabel = new Label();
        forecastLabel.setStyle("-fx-font-size: 14px;");

        // History area
        historyArea.setEditable(false);
        historyArea.setPrefHeight(120);
        historyArea.setPromptText("Search History");

        // 🔍 Search action
        searchButton.setOnAction(e -> {
            String city = cityInput.getText().trim();
            if (!city.isEmpty()) {
                fetchCurrentWeather(city, resultLabel);
                fetchForecast(city, forecastLabel);
            } else {
                resultLabel.setText("Please enter a city name.");
            }
        });

        // 🌡️ Unit toggle
        unitButton.setOnAction(e -> {
            isCelsius = !isCelsius;
            unitButton.setText(isCelsius ? "Switch to °F" : "Switch to °C");
        });

        // Layout
        VBox root = new VBox(12,
                title,
                cityInput,
                searchButton,
                unitButton,
                resultLabel,
                forecastLabel,
                new Label("History:"),
                historyArea
        );

        root.setStyle("-fx-padding: 15; -fx-background-color: #e6f2ff;");

        Scene scene = new Scene(root, 480, 520);
        stage.setTitle("Weather App");
        stage.setScene(scene);

        loadHistoryFromFile();
        stage.show();
    }

    // 🌤 Fetch current weather
    private void fetchCurrentWeather(String city, Label resultLabel) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String cityEncoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String unit = isCelsius ? "metric" : "imperial";
            String unitSymbol = isCelsius ? "°C" : "°F";

            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + cityEncoded + "&units=" + unit + "&appid=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        JSONObject obj = new JSONObject(response.body());

                        if (obj.has("main")) {

    double temp = obj.getJSONObject("main").getDouble("temp");
    int humidity = obj.getJSONObject("main").getInt("humidity");
    String weather = obj.getJSONArray("weather")
            .getJSONObject(0)
            .getString("description");

    String text = String.format(
            "City: %s\nTemperature: %.1f%s\nHumidity: %d%%\nCondition: %s",
            city, temp, unitSymbol, humidity, weather
    );

    String time = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

    Platform.runLater(() -> {
        resultLabel.setText(text);
        historyArea.appendText(time + " - " + city + "\n");
        saveHistoryToFile(time + " - " + city);
    });

} else {

    Platform.runLater(() -> {
        resultLabel.setText("❌ City not found. Please enter a valid city name.");
    });

}

                    });

        } catch (Exception e) {
            resultLabel.setText("Error fetching weather.");
        }
    }

    // 📅 Fetch 3-day forecast with icons
    private void fetchForecast(String city, Label forecastLabel) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String cityEncoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String unit = isCelsius ? "metric" : "imperial";
            String unitSymbol = isCelsius ? "°C" : "°F";

            String url = "https://api.openweathermap.org/data/2.5/forecast?q="
                    + cityEncoded + "&units=" + unit + "&appid=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {

                        JSONObject obj = new JSONObject(response.body());
                        JSONArray list = obj.getJSONArray("list");

                        StringBuilder forecast = new StringBuilder("📅 3-Day Forecast\n");

                        // every 8 items ≈ 1 day
                        for (int i = 0; i < list.length(); i += 8) {

                            JSONObject item = list.getJSONObject(i);

                            String date = item.getString("dt_txt").substring(0, 10);
                            double temp = item.getJSONObject("main").getDouble("temp");

                            String condition = item
                                    .getJSONArray("weather")
                                    .getJSONObject(0)
                                    .getString("main")
                                    .toLowerCase();

                            // 🌈 icon selection
                            String icon;
                            if (condition.contains("clear")) {
                                icon = "☀️";
                            } else if (condition.contains("cloud")) {
                                icon = "☁️";
                            } else if (condition.contains("rain")) {
                                icon = "🌧";
                            } else if (condition.contains("snow")) {
                                icon = "❄️";
                            } else {
                                icon = "🌤";
                            }

                            forecast.append(icon)
                                    .append(" ")
                                    .append(date)
                                    .append("  →  ")
                                    .append(String.format("%.1f", temp))
                                    .append(unitSymbol)
                                    .append("\n");
                        }

                        Platform.runLater(() -> forecastLabel.setText(forecast.toString()));
                    });

        } catch (Exception e) {
            forecastLabel.setText("Error loading forecast.");
        }
    }

    // 📂 Load history from file
    private void loadHistoryFromFile() {
        try {
            File file = new File(HISTORY_FILE);
            if (file.exists()) {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    historyArea.appendText(line + "\n");
                }
            }
        } catch (IOException ignored) {
        }
    }

    // 💾 Save history to file
    private void saveHistoryToFile(String entry) {
        try (FileWriter writer = new FileWriter(HISTORY_FILE, true)) {
            writer.write(entry + System.lineSeparator());
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
