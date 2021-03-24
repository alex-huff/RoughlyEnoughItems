/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api.common.ingredient.entry.type;

import me.shedaniel.rei.api.client.ingredient.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.ingredient.EntryStack;
import me.shedaniel.rei.api.common.ingredient.entry.EntrySerializer;
import me.shedaniel.rei.api.common.ingredient.entry.comparison.ComparisonContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface EntryDefinition<T> {
    Class<T> getValueType();
    
    EntryType<T> getType();
    
    @Environment(EnvType.CLIENT)
    EntryRenderer<T> getRenderer();
    
    @Nullable ResourceLocation getIdentifier(EntryStack<T> entry, T value);
    
    boolean isEmpty(EntryStack<T> entry, T value);
    
    T copy(EntryStack<T> entry, T value);
    
    T normalize(EntryStack<T> entry, T value);
    
    int hash(EntryStack<T> entry, T value, ComparisonContext context);
    
    boolean equals(T o1, T o2, ComparisonContext context);
    
    @Nullable
    EntrySerializer<T> getSerializer();
    
    Component asFormattedText(EntryStack<T> entry, T value);
    
    Collection<ResourceLocation> getTagsFor(EntryStack<T> entry, T value);
    
    @ApiStatus.NonExtendable
    default <O> EntryDefinition<O> cast() {
        return (EntryDefinition<O>) this;
    }
}
