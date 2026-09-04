package com.matthewmcroberts.modules.rank.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.rank.models.Rank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
public class RankApiClient {
    // TODO: Handle key value more securely
    private static final String HEADER_KEY = "RANKS-API-KEY";
    private static final String HEADER_VALUE = "matthew.mcroberts.key";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper;

    // createRank
    public CompletableFuture<Rank> createRank(@NonNull final Rank rank) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(rank);
        } catch(JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/secure/api/ranks"))
                .header(HEADER_KEY, HEADER_VALUE)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new RuntimeException("Failed to create rank. HTTP " + response.statusCode() + ": " + response.body());
                    }

                    try {
                        return objectMapper.readValue(response.body(), Rank.class);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    // getRankById
    public CompletableFuture<Rank> getRankById(@NonNull final String rankId) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId))
                .header(HEADER_KEY, HEADER_VALUE)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("API returned " + response.statusCode());
                    }

                    try {
                        return objectMapper.readValue(response.body(), Rank.class);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    // getAllRanks
    // updateRankDisplayName
    // updateRankPriority
    // addRankPermissions
    // removeRankPermissions
    // addRankInheritance
    // removeRankInheritance
    // deleteRank
    // assignPlayerRank
    // removePlayerRank
    // getPlayerRankAssignment
}
