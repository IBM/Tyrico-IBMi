package com.tyrico;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.*;

import org.json.JSONArray;

import java.util.concurrent.Executors;
import com.tyrico.TyricoLib;

public class IrisClassifierServer {
    public static void main(String[] args) throws Exception {
        // Create and start the server
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8080), 2000);
        server.setExecutor(Executors.newFixedThreadPool(100)); // Set a thread pool with 10 threads

        server.createContext("/classify", new ClassifyHandler());
        server.start();
        System.out.println("Server is running on http://localhost:8080");
    }

    // Handler for the /classify endpoint
    static class ClassifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Allow CORS for all origins
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            HashMap<String, String> keysNeeded = new HashMap<>();

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                // Handle pre-flight request (for CORS)
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the request body
                InputStream requestBody = exchange.getRequestBody();
                byte[] requestBytes = requestBody.readAllBytes();
                String requestBodyString = new String(requestBytes);

                // Parse the input JSON into a list (we will skip detailed parsing for simplicity)
                // Here we assume the input is a simple JSON array of arrays
                JSONArray flowers = new JSONArray(requestBodyString);

                // Classify each flower
                for (int i=0; i < flowers.length(); i++){
                    try {
                        String key = TyricoLib.writeDataQueue(flowers.get(i).toString());
                        keysNeeded.put(key, "0");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                List<String[]> classifications = new ArrayList<>();


                int retrieved = 0;
                while (retrieved < flowers.length()){
                    Iterator<String> it = keysNeeded.keySet().iterator();
                    try{
                        while (it.hasNext()) {
                            String key = it.next();
                            System.out.println("waiting for key " + key);
                            String value = TyricoLib.readDataQueue(key);
                            if (!value.equals("")){
                                classifications.add(new String[]{key, value});
                                it.remove();
                                retrieved++;
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();

                    }
                }


                // Convert the result into a JSON array for the response
                JSONArray responseJson = new JSONArray(classifications);

                // Send response headers
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseJson.toString().getBytes().length);

                // Send the response body
                OutputStream responseBody = exchange.getResponseBody();
                System.out.println("Sending data " + responseBody);
                responseBody.write(responseJson.toString().getBytes());
                responseBody.close();
            } else {
                // If method is not POST, send method not allowed
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }
}
