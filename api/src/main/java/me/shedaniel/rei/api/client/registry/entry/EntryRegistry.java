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

package me.shedaniel.rei.api.client.registry.entry;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.ingredient.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public interface EntryRegistry extends Reloadable<REIClientPlugin> {
    /**
     * @return the instance of {@link EntryRegistry}
     */
    static EntryRegistry getInstance() {
        return PluginManager.getClientInstance().get(EntryRegistry.class);
    }
    
    /**
     * @return the size of entry stacks, before being filtered by filtering rules.
     */
    int size();
    
    /**
     * @return the unmodifiable stream of entry stacks, before being filtered by filtering rules.
     */
    Stream<EntryStack<?>> getEntryStacks();
    
    /**
     * @return the unmodifiable list of filtered entry stacks,
     * only available after plugins reload.
     */
    List<EntryStack<?>> getPreFilteredList();
    
    /**
     * Applies the filtering rules to the entry list, is rather computational expensive.
     * The filtered entries are retrievable at {@link EntryRegistry#getPreFilteredList()}
     */
    void refilter();
    
    /**
     * Gets all possible stacks from an item, tries to invoke {@link Item#appendStacks(net.minecraft.item.ItemGroup, net.minecraft.util.collection.DefaultedList)}.
     *
     * @param item the item to find
     * @return the list of possible stacks, will never be empty.
     */
    List<ItemStack> appendStacksForItem(Item item);
    
    /**
     * Registers an new stack to the entry list.
     *
     * @param stack the stack to register
     */
    default void registerEntry(EntryStack<?> stack) {
        registerEntryAfter(null, stack);
    }
    
    /**
     * Registers an new stack to the entry list, after a certain stack.
     *
     * @param afterEntry the stack to put after
     * @param stack      the stack to register
     */
    void registerEntryAfter(@Nullable EntryStack<?> afterEntry, EntryStack<?> stack);
    
    /**
     * Registers multiple stacks to the item list, after a certain stack.
     *
     * @param afterStack the stack to put after
     * @param stacks     the stacks to register
     */
    default void registerEntriesAfter(@Nullable EntryStack<?> afterStack, EntryStack<?>... stacks) {
        registerEntriesAfter(afterStack, Arrays.asList(stacks));
    }
    
    /**
     * Registers multiple stacks to the item list, after a certain stack.
     *
     * @param afterStack the stack to put after
     * @param stacks     the stacks to register
     */
    void registerEntriesAfter(@Nullable EntryStack<?> afterStack, Collection<? extends EntryStack<?>> stacks);
    
    /**
     * Registers multiple stacks to the item list.
     *
     * @param stacks the stacks to register
     */
    default void registerEntries(EntryStack<?>... stacks) {
        registerEntries(Arrays.asList(stacks));
    }
    
    /**
     * Registers multiple stacks to the item list.
     *
     * @param stacks the stacks to register
     */
    default void registerEntries(Collection<? extends EntryStack<?>> stacks) {
        registerEntriesAfter(null, stacks);
    }
    
    /**
     * Checks if a stack is already registered.
     *
     * @param stack the stack to check
     * @return whether the stack has been registered
     */
    boolean alreadyContain(EntryStack<?> stack);
    
    /**
     * Removes an entry from the entry list, if it exists.
     *
     * @param stack the stack to remove
     * @return whether it was successful to remove the entry
     */
    boolean removeEntry(EntryStack<?> stack);
    
    /**
     * Removes entries from the entry list, if it matches the predicate.
     *
     * @param filter a predicate which returns {@code true} for the entries to be removed
     * @return whether it was successful to remove any entry
     */
    boolean removeEntryIf(Predicate<? extends EntryStack<?>> filter);
}