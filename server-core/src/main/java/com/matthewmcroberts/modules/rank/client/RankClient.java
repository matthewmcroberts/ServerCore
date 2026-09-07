package com.matthewmcroberts.modules.rank.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.rank.client.dto.DisplayNameUpdateDto;
import com.matthewmcroberts.modules.rank.client.dto.InheritanceUpdateDto;
import com.matthewmcroberts.modules.rank.client.dto.PermissionsUpdateDto;
import com.matthewmcroberts.modules.rank.client.dto.PlayerRankAssignmentDto;
import com.matthewmcroberts.modules.rank.client.dto.PriorityUpdateDto;
import com.matthewmcroberts.modules.rank.client.dto.RankDto;
import com.matthewmcroberts.modules.rank.client.dto.RankAssignmentDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

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
public class RankClient {
    // TODO: Handle key value more securely
    private static final String HEADER_KEY = "RANKS-API-KEY";
    private static final String HEADER_VALUE = "matthew.mcroberts.key";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper;

    // createRank
    public CompletableFuture<RankDto> createRank(@NonNull final RankDto rankDto) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(rankDto);
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
                        return objectMapper.readValue(response.body(), RankDto.class);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    // getRankById
    public CompletableFuture<RankDto> getRankById(@NonNull final String rankId) {
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
                        return objectMapper.readValue(response.body(), RankDto.class);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    // getAllRanks
    public CompletableFuture<List<RankDto>> getAllRanks() {
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
                        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, RankDto.class));
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    // updateRankDisplayName
    public CompletableFuture<RankDto> updateRankDisplayName(@NonNull final String rankId, @NonNull final String rankDisplayName) {
        try {
            final DisplayNameUpdateDto displayNameUpdateDto = DisplayNameUpdateDto.builder()
                    .displayName(rankDisplayName)
                    .build();

            final String json = objectMapper.writeValueAsString(displayNameUpdateDto);

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
                            return objectMapper.readValue(response.body(), RankDto.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // updateRankPriority
    public CompletableFuture<RankDto> updateRankPriority(@NonNull final String rankId, final int priority) {
        try {
            final PriorityUpdateDto priorityUpdateDto = PriorityUpdateDto.builder()
                    .priority(priority)
                    .build();

            final String json = objectMapper.writeValueAsString(priorityUpdateDto);

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
                            return objectMapper.readValue(response.body(), RankDto.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // addRankPermissions
    public CompletableFuture<RankDto> addRankPermissions(@NonNull final String rankId, @NonNull final Set<String> permissions) {
        try {
            final PermissionsUpdateDto permissionsUpdateDto = PermissionsUpdateDto.builder()
                    .permissions(permissions)
                    .build();

            String json = objectMapper.writeValueAsString(permissionsUpdateDto);

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
                            return objectMapper.readValue(response.body(), RankDto.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // removeRankPermissions
    public CompletableFuture<RankDto> removeRankPermissions(@NonNull final String rankId, @NonNull final Set<String> permissions) {
        try {
            final PermissionsUpdateDto permissionsUpdateDto = PermissionsUpdateDto.builder()
                    .permissions(permissions)
                    .build();

            String json = objectMapper.writeValueAsString(permissionsUpdateDto);

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
                            return objectMapper.readValue(response.body(), RankDto.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // addRankInheritance
    public CompletableFuture<RankDto> addRankInheritance(@NonNull final String rankId, @NonNull final Set<String> inheritedRankIds) {
        try {
            final InheritanceUpdateDto inheritanceUpdateDto = InheritanceUpdateDto.builder()
                    .inheritedRankIds(inheritedRankIds)
                    .build();

            final String json = objectMapper.writeValueAsString(inheritanceUpdateDto);

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
                            return objectMapper.readValue(response.body(), RankDto.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // removeRankInheritance
    public CompletableFuture<RankDto> removeRankInheritance(@NonNull final String rankId, @NonNull final Set<String> inheritedRankIds) {
        try {
            final InheritanceUpdateDto inheritanceUpdateDto = InheritanceUpdateDto.builder()
                    .inheritedRankIds(inheritedRankIds)
                    .build();

            final String json = objectMapper.writeValueAsString(inheritanceUpdateDto);

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
                            return objectMapper.readValue(response.body(), RankDto.class);
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
    public CompletableFuture<RankDto> getRankByName(@NonNull final String rankName) {
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
                        return objectMapper.readValue(response.body(), RankDto.class);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }


    // getPlayersWithRank
    public CompletableFuture<List<PlayerRankAssignmentDto>> getPlayersWithRank(@NonNull final String rankId) {
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
                                .constructCollectionType(List.class, PlayerRankAssignmentDto.class)
                        );
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }


    // assignPlayerRank
    public CompletableFuture<PlayerRankAssignmentDto> assignPlayerRank(
            @NonNull final String playerId,
            @Nullable final String assignedById,
            @NonNull final String rankId) {
        try {
            final RankAssignmentDto rankAssignmentDto = RankAssignmentDto.builder()
                    .assignedById(assignedById)
                    .rankId(rankId)
                    .build();

            final String json = objectMapper.writeValueAsString(rankAssignmentDto);

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
                            return objectMapper.readValue(response.body(), PlayerRankAssignmentDto.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException(e);
                        }
                    });
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    // getPlayerRankAssignments
    public CompletableFuture<PlayerRankAssignmentDto> getPlayerRankAssignment(@NonNull final String playerId) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/secure/api/players/" + playerId + "/rank-assignment"))
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
                        return objectMapper.readValue(response.body(), PlayerRankAssignmentDto.class);
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
}
