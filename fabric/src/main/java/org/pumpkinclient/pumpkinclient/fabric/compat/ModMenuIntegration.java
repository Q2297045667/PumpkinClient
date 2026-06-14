package org.pumpkinclient.pumpkinclient.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.pumpkinclient.pumpkinclient.Pumpkinclient;
import org.pumpkinclient.pumpkinclient.config.PumpkinConfig;
import org.pumpkinclient.pumpkinclient.network.CompressionAlgorithm;

public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        PumpkinConfig config = Pumpkinclient.getConfig();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("pumpkinclient.config.title"))
                .setSavingRunnable(Pumpkinclient::saveConfig);

        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("pumpkinclient.config.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        category.addEntry(entryBuilder.startEnumSelector(
                        Component.translatable("pumpkinclient.config.compression_algorithm"),
                        CompressionAlgorithm.class,
                        config.getCompressionAlgorithm())
                .setDefaultValue(CompressionAlgorithm.ZSTD)
                .setSaveConsumer(config::setCompressionAlgorithm)
                .setEnumNameProvider(anEnum -> Component.translatable("pumpkinclient.algorithm." + ((CompressionAlgorithm) anEnum).getId()))
                .build());

        category.addEntry(entryBuilder.startIntSlider(
                        Component.translatable("pumpkinclient.config.compression_level"),
                        config.getCompressionLevel(),
                        0, 22)
                .setDefaultValue(3)
                .setSaveConsumer(config::setCompressionLevel)
                .build());

        return builder.build();
    }
}
