package com.matthewmcroberts.modules.rank.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.rank.models.DisplayNameUpdate;
import com.matthewmcroberts.modules.rank.models.InheritanceUpdate;
import com.matthewmcroberts.modules.rank.models.PermissionsUpdate;
import com.matthewmcroberts.modules.rank.models.PlayerRankAssignment;
import com.matthewmcroberts.modules.rank.models.PriorityUpdate;
import com.matthewmcroberts.modules.rank.models.Rank;
import com.matthewmcroberts.modules.rank.models.RankAssignment;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.bukkit.Bukkit.getLogger;

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
    public CompletableFuture<List<Rank>> getAllRanks() {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/secure/api/ranks"))
                .header(HEADER_KEY, HEADER_VALUE)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if(response.statusCode() != 200) {
                        throw new RuntimeException("API returned " + response.statusCode());
                    }

                    try {
                        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Rank.class));
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    // updateRankDisplayName
    public CompletableFuture<Rank> updateRankDisplayName(@NonNull final String rankId, @NonNull final String rankDisplayName) {
        try {
            final DisplayNameUpdate displayNameUpdate = DisplayNameUpdate.builder()
                    .displayName(rankDisplayName)
                    .build();

            final String json = objectMapper.writeValueAsString(displayNameUpdate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId + "/display-name"))
                    .header(HEADER_KEY, HEADER_VALUE)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
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
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // updateRankPriority
    public CompletableFuture<Rank> updateRankPriority(@NonNull final String rankId, final int priority) {
        try {
            final PriorityUpdate priorityUpdate = PriorityUpdate.builder()
                    .priority(priority)
                    .build();

            final String json = objectMapper.writeValueAsString(priorityUpdate);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId + "/priority"))
                    .header(HEADER_KEY, HEADER_VALUE)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
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
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // addRankPermissions
    public CompletableFuture<Rank> addRankPermissions(@NonNull final String rankId, @NonNull final Set<String> permissions) {
        try {
            final PermissionsUpdate permissionsUpdate = PermissionsUpdate.builder()
                    .permissions(permissions)
                    .build();

            String json = objectMapper.writeValueAsString(permissionsUpdate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId + "/permissions"))
                    .header(HEADER_KEY, HEADER_VALUE)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // removeRankPermissions
    public CompletableFuture<Rank> removeRankPermissions(@NonNull final String rankId, @NonNull final Set<String> permissions) {
        try {
            final PermissionsUpdate permissionsUpdate = PermissionsUpdate.builder()
                    .permissions(permissions)
                    .build();

            String json = objectMapper.writeValueAsString(permissionsUpdate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId + "/permissions"))
                    .header(HEADER_KEY, HEADER_VALUE)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(json))
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // addRankInheritance
    public CompletableFuture<Rank> addRankInheritance(@NonNull final String rankId, @NonNull final Set<String> inheritedRankIds) {
        try {
            final InheritanceUpdate inheritanceUpdate = InheritanceUpdate.builder()
                    .inheritedRankIds(inheritedRankIds)
                    .build();

            final String json = objectMapper.writeValueAsString(inheritanceUpdate);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId + "/inherited-ranks"))
                    .header(HEADER_KEY, HEADER_VALUE)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // removeRankInheritance
    public CompletableFuture<Rank> removeRankInheritance(@NonNull final String rankId, @NonNull final Set<String> inheritedRankIds) {
        try {
            final InheritanceUpdate inheritanceUpdate = InheritanceUpdate.builder()
                    .inheritedRankIds(inheritedRankIds)
                    .build();

            final String json = objectMapper.writeValueAsString(inheritanceUpdate);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId + "/inherited-ranks"))
                    .header(HEADER_KEY, HEADER_VALUE)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(json))
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // deleteRank
    public CompletableFuture<Void> deleteRank(@NonNull final String rankId) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/secure/api/ranks/" + rankId
                ))
                .header(HEADER_KEY, HEADER_VALUE)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 204) {
                        throw new CompletionException(new RuntimeException("API returned " + response.statusCode() + ": " + response.body()));
                    }

                    getLogger().info("Successfully deleted rank " + rankId);
                });
    }

    // getRankByName
    public CompletableFuture<Rank> getRankByName(@NonNull final String rankName) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/secure/api/ranks/by-name/" + rankName))
                .header(HEADER_KEY, HEADER_VALUE)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new CompletionException(
                                new RuntimeException("Failed to get rank by name. HTTP " + response.statusCode() + ": " + response.body()));
                    }

                    try {
                        return objectMapper.readValue(response.body(), Rank.class);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }


    // getPlayersWithRank
    public CompletableFuture<List<PlayerRankAssignment>> getPlayersWithRank(@NonNull final String rankId) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/secure/api/ranks/" + rankId + "/players"))
                .header(HEADER_KEY, HEADER_VALUE)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new CompletionException(
                                new RuntimeException("Failed to get players with rank. HTTP " + response.statusCode() + ": " + response.body()));
                    }

                    try {
                        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, PlayerRankAssignment.class)
                        );
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }


    // assignPlayerRank
    public CompletableFuture<PlayerRankAssignment> assignPlayerRank(
            @NonNull final String playerId,
            @NonNull final String assignedById,
            @NonNull final String rankId) {
        try {
            final RankAssignment rankAssignment = RankAssignment.builder()
                    .assignedById(assignedById)
                    .rankId(rankId)
                    .build();

            final String json = objectMapper.writeValueAsString(rankAssignment);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/secure/api/players/" + playerId + "/rank"))
                    .header(HEADER_KEY, HEADER_VALUE)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 201) {
                            throw new CompletionException(
                                    new RuntimeException("Failed to assign player rank. HTTP " + response.statusCode() + ": " + response.body()));
                        }

                        try {
                            return objectMapper.readValue(response.body(), PlayerRankAssignment.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    // getPlayerRankAssignments
    public CompletableFuture<List<PlayerRankAssignment>> getPlayerRankAssignments(@NonNull final String playerId) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/secure/api/players/" + playerId + "/rank-assignments"))
                .header(HEADER_KEY, HEADER_VALUE)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new CompletionException(
                                new RuntimeException("Failed to get player rank assignments. HTTP " + response.statusCode() + ": " + response.body()));
                    }

                    try {
                        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, PlayerRankAssignment.class));
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }


    // removePlayerRank
    public CompletableFuture<Void> removePlayerRank(@NonNull final String playerId) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/secure/api/players/"
                                + playerId
                                + "/rank"
                ))
                .header(HEADER_KEY, HEADER_VALUE)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 204) {
                        throw new CompletionException(
                                new RuntimeException("Failed to remove player rank. HTTP " + response.statusCode() + ": " + response.body())
                        );
                    }

                    getLogger().info("Successfully removed rank from player " + playerId);
                });
    }


    // getPlayerRanks
    public CompletableFuture<List<Rank>> getPlayerRanks(@NonNull final String playerId) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/secure/api/players/"
                                + playerId
                                + "/ranks"
                ))
                .header(HEADER_KEY, HEADER_VALUE)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new CompletionException(new RuntimeException("Failed to get player ranks. HTTP " + response.statusCode() + ": " + response.body()));
                    }

                    try {
                        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Rank.class));
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }
}
