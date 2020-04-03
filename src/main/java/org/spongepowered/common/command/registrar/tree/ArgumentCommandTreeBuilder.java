/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.command.registrar.tree;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.command.brigadier.argument.SpongeArgumentTypeAdapter;
import org.spongepowered.common.util.Constants;

public class ArgumentCommandTreeBuilder<T extends CommandTreeBuilder<T>>
        extends AbstractCommandTreeBuilder<T, CommandNode<CommandSource>> {

    private final ClientCompletionKey<T> parameterType;

    public ArgumentCommandTreeBuilder(ClientCompletionKey<T> parameterType) {
        this.parameterType = parameterType;
    }

    @Override
    void setType(JsonObject object) {
        object.addProperty(Constants.Command.TYPE, Constants.Command.ARGUMENT);
        object.addProperty(Constants.Command.PARSER, this.parameterType.getKey().getFormatted());
    }

    @Override
    public byte getNodeMask() {
        return (byte) (Constants.Command.ARGUMENT_NODE_BIT | this.customSuggestionsMask());
    }

    @Override
    protected final CommandNode<CommandSource> createArgumentTree(String nodeKey, Command<CommandSource> command) {
        ArgumentType<T> type = getArgumentType();
        RequiredArgumentBuilder<CommandSource, T> builder = RequiredArgumentBuilder.argument(nodeKey, type);
        this.applySpecificsToNode(builder);
        this.addChildNodesToTree(builder, command);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    protected ArgumentType<T> getArgumentType() {
        return ((SpongeArgumentTypeAdapter<T>) SpongeImpl.getRegistry().getCatalogRegistry()
                .get(CatalogedValueParameter.class, this.parameterType.getKey())
                .orElseThrow(() -> new IllegalStateException("ID " + this.parameterType.getKey().toString() + " could not be found.")))
                .getUnderlyingType();
    }

    /**
     * Applies any specific requirements that the tree builder indicates.
     *
     * @param node The node to apply to
     */
    protected void applySpecificsToNode(RequiredArgumentBuilder<CommandSource, T> node) {
    }

    private byte customSuggestionsMask() {
        return this.isCustomSuggestions() ? Constants.Command.CUSTOM_SUGGESTIONS_BIT : 0;
    }

    public ClientCompletionKey<T> getClientCompletionKey() {
        return this.parameterType;
    }

    public void applyProperties(PacketBuffer packetBuffer) {
    }

}
