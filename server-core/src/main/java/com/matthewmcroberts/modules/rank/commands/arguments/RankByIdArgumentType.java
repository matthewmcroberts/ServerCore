package com.matthewmcroberts.modules.rank.commands.arguments;

import com.matthewmcroberts.modules.rank.RankMessages;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.models.Rank;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.adventure.AdventureComponent;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * Custom argument type for parsing a rank by its id.
 */
@RequiredArgsConstructor
public class RankByIdArgumentType implements CustomArgumentType<Rank, String> {
    /**
     * Reference to the rank module implementation.
     */
    private final WeakReference<RankModule> rankModuleReference;

    /**
     * Parses a rank by its id from the provided string reader.
     *
     * @param reader string reader input
     * @return the Rank object if found
     * @throws CommandSyntaxException if the rank id is unknown
     */
    @Override
    public @NonNull Rank parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final String input = reader.readUnquotedString();

        final RankModule module = Objects.requireNonNull(this.rankModuleReference.get(), "Module is null");
        return module.getRankById(input).orElseThrow(() -> {
            reader.setCursor(start);

            return new SimpleCommandExceptionType(
                            new AdventureComponent(RankMessages.Error.RANK_ID_NOT_FOUND.apply(Component.text(input))))
                    .createWithContext(reader);
        });
    }

    /**
     * Get the native type of the argument.
     *
     * @return the native argument type for rank ids
     */
    @Override
    public @NonNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
