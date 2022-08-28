/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.gui.overlay.widgets;

import com.mojang.math.Vector4f;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccess;
import me.shedaniel.rei.impl.client.gui.menu.MenuEntry;
import me.shedaniel.rei.impl.client.gui.menu.entries.SubMenuEntry;
import me.shedaniel.rei.impl.client.gui.menu.entries.ToggleMenuEntry;
import me.shedaniel.rei.impl.client.gui.screen.ConfigReloadingScreen;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class CraftableFilterButtonWidget implements OverlayWidgetProvider {
    public static final UUID FILTER_MENU_UUID = UUID.fromString("2839e998-1679-4f9e-a257-37411d16f1e6");
    
    @Override
    public List<Widget> provide(ScreenOverlay overlay, MenuAccess access, Consumer<TextField> textFieldSink, UnaryOperator<Widget> lateRenderable) {
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            return List.of(create(overlay, access, lateRenderable));
        } else {
            return List.of();
        }
    }
    
    private static Widget create(ScreenOverlay overlay, MenuAccess access, UnaryOperator<Widget> lateRenderable) {
        Rectangle bounds = getCraftableFilterBounds();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack icon = new ItemStack(Blocks.CRAFTING_TABLE);
        Button filterButton = Widgets.createButton(bounds, NarratorChatListener.NO_TITLE)
                .focusable(false)
                .onClick(button -> {
                    ConfigManager.getInstance().toggleCraftableOnly();
                    REIRuntime.getInstance().getOverlay().map(ScreenOverlay::getEntryList).ifPresent(OverlayListWidget::queueReloadSearch);
                })
                .onRender((matrices, button) -> {
                    button.setTint(ConfigManager.getInstance().isCraftableOnlyEnabled() ? 0x3800d907 : 0x38ff0000);
                    
                    access.openOrClose(FILTER_MENU_UUID, button.getBounds(), CraftableFilterButtonWidget::menuEntries);
                })
                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && overlay.isNotInExclusionZones(point.x, point.y))
                .tooltipLineSupplier(button -> new TranslatableComponent(ConfigManager.getInstance().isCraftableOnlyEnabled() ? "text.rei.showing_craftable" : "text.rei.showing_all"));
        Widget overlayWidget = Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Vector4f vector = new Vector4f(bounds.x + 2, bounds.y + 2, helper.getBlitOffset() - 10, 1.0F);
            vector.transform(matrices.last().pose());
            itemRenderer.blitOffset = vector.z();
            itemRenderer.renderGuiItem(icon, (int) vector.x(), (int) vector.y());
            itemRenderer.blitOffset = 0.0F;
        });
        return lateRenderable.apply(Widgets.concat(filterButton, overlayWidget));
    }
    
    private static Collection<MenuEntry> menuEntries() {
        ConfigManagerInternal manager = ConfigManagerInternal.getInstance();
        ConfigObject config = ConfigObject.getInstance();
        ArrayList<MenuEntry> entries = new ArrayList<>(List.of(
                new SubMenuEntry(new TranslatableComponent("text.rei.config.menu.search_field.position"), Arrays.stream(SearchFieldLocation.values())
                        .<MenuEntry>map(location -> ToggleMenuEntry.of(new TextComponent(location.toString()),
                                        () -> config.getSearchFieldLocation() == location,
                                        bool -> manager.set("appearance.layout.searchFieldLocation", location))
                                .withActive(() -> config.getSearchFieldLocation() != location)
                        )
                        .toList())
        ));
        
        List<Map.Entry<ResourceLocation, InputMethod<?>>> applicableInputMethods = getApplicableInputMethods();
        if (applicableInputMethods.size() > 1) {
            entries.add(new SubMenuEntry(new TranslatableComponent("text.rei.config.menu.search_field.input_method"), createInputMethodEntries(applicableInputMethods)));
        }
        
        return entries;
    }
    
    public static List<Map.Entry<ResourceLocation, InputMethod<?>>> getApplicableInputMethods() {
        String languageCode = Minecraft.getInstance().options.languageCode;
        return InputMethodRegistry.getInstance().getAll().entrySet().stream()
                .filter(entry -> CollectionUtils.anyMatch(entry.getValue().getMatchingLocales(), locale -> locale.code().equals(languageCode)))
                .toList();
    }
    
    public static List<MenuEntry> createInputMethodEntries(List<Map.Entry<ResourceLocation, InputMethod<?>>> applicableInputMethods) {
        ConfigManagerInternal manager = ConfigManagerInternal.getInstance();
        ConfigObject config = ConfigObject.getInstance();
        return applicableInputMethods.stream()
                .<MenuEntry>map(pair -> ToggleMenuEntry.of(pair.getValue().getName(),
                                () -> Objects.equals(config.getInputMethodId(), pair.getKey()),
                                bool -> {
                                    ExecutorService service = Executors.newSingleThreadExecutor();
                                    InputMethod<?> active = InputMethod.active();
                                    active.dispose(service).whenComplete((unused, throwable) -> {
                                        if (throwable != null) {
                                            InternalLogger.getInstance().error("Failed to dispose input method", throwable);
                                        }
                                        
                                        manager.set("functionality.inputMethod", new ResourceLocation("rei:default"));
                                    }).join();
                                    CompletableFuture<Void> future = pair.getValue().prepare(service).whenComplete((unused, throwable) -> {
                                        if (throwable != null) {
                                            InternalLogger.getInstance().error("Failed to prepare input method", throwable);
                                            manager.set("functionality.inputMethod", new ResourceLocation("rei:default"));
                                        } else {
                                            manager.set("functionality.inputMethod", pair.getKey());
                                        }
                                    });
                                    Screen screen = Minecraft.getInstance().screen;
                                    Minecraft.getInstance().setScreen(new ConfigReloadingScreen(new TranslatableComponent("text.rei.input.methods.initializing"),
                                            () -> !future.isDone(), () -> {
                                        Minecraft.getInstance().setScreen(screen);
                                    }));
                                    future.whenComplete((unused, throwable) -> {
                                        service.shutdown();
                                    });
                                })
                        .withActive(() -> !Objects.equals(config.getInputMethodId(), pair.getKey()))
                        .withTooltip(() -> Tooltip.create(TooltipContext.ofMouse(), pair.getValue().getDescription()))
                )
                .toList();
    }
    
    private static Rectangle getCraftableFilterBounds() {
        Rectangle area = REIRuntime.getInstance().getSearchTextField().asWidget().getBounds().clone();
        area.setLocation(area.x + area.width + 4, area.y - 1);
        area.setSize(20, 20);
        return area;
    }
}