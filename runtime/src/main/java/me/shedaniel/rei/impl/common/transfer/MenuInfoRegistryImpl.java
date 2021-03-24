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

package me.shedaniel.rei.impl.common.transfer;

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;

public class MenuInfoRegistryImpl implements MenuInfoRegistry {
    private final Map<String, Map<Class<? extends AbstractContainerMenu>, MenuInfo<?, ?>>> map = Maps.newLinkedHashMap();
    
    @Override
    public <D extends Display> void register(CategoryIdentifier<D> category, MenuInfo<?, D> menuInfo) {
        if (!map.containsKey(category.toString()))
            map.put(category.toString(), Maps.newLinkedHashMap());
        map.get(category.toString()).put(menuInfo.getContainerClass(), menuInfo);
    }
    
    @Override
    public <T extends AbstractContainerMenu, D extends Display> MenuInfo<T, D> get(CategoryIdentifier<D> category, Class<T> menuClass) {
        Map<Class<? extends AbstractContainerMenu>, MenuInfo<?, ?>> infoMap = map.get(category.toString());
        if (infoMap == null || infoMap.isEmpty()) {
            return null;
        }
        if (infoMap.containsKey(menuClass)) {
            return (MenuInfo<T, D>) infoMap.get(menuClass);
        }
        for (Map.Entry<Class<? extends AbstractContainerMenu>, MenuInfo<?, ?>> entry : infoMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(menuClass)) {
                return (MenuInfo<T, D>) entry.getValue();
            }
        }
        return null;
    }
    
    @Override
    public void startReload() {
        map.clear();
    }
    
    @Override
    public void acceptPlugin(REIServerPlugin plugin) {
        plugin.registerMenuInfo(this);
    }
}