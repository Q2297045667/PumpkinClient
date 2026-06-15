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

    @SuppressWarnings("unchecked")
    private Screen createConfigScreen(Screen parent) {
        PumpkinConfig config = Pumpkinclient.getConfig();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(translatable("pumpkinclient.config.title"))
                .setSavingRunnable(Pumpkinclient::saveConfig);

        ConfigCategory category = builder.getOrCreateCategory(translatable("pumpkinclient.config.title"));
        ConfigEntryBuilder eb = builder.entryBuilder();

        category.addEntry(eb.startEnumSelector(
                        translatable("pumpkinclient.config.compression_algorithm"),
                        CompressionAlgorithm.class,
                        config.getCompressionAlgorithm())
                .setDefaultValue(CompressionAlgorithm.AUTO)
                .setSaveConsumer(value -> {
                    config.setCompressionAlgorithm((CompressionAlgorithm) value);
                    Pumpkinclient.resetEffectiveAlgorithm();
                })
                .setEnumNameProvider(value -> translatable("pumpkinclient.algorithm." + ((CompressionAlgorithm) value).getId()))
                .setTooltip(translatable("pumpkinclient.config.restart_required"))
                .build());

        category.addEntry(eb.startIntSlider(
                        translatable("pumpkinclient.config.compression_level"),
                        config.getCompressionLevel(),
                        0, 22)
                .setDefaultValue(4)
                .setSaveConsumer(config::setCompressionLevel)
                .build());

        category.addEntry(eb.startIntSlider(
                        translatable("pumpkinclient.config.max_threads"),
                        config.getMaxThreads(),
                        0, 32)
                .setDefaultValue(0)
                .setTooltip(translatable("pumpkinclient.config.max_threads.tooltip"))
                .setSaveConsumer(config::setMaxThreads)
                .build());

        return builder.build();
    }

    private static Component translatable(String key) {
        return Component.translatable(key);
    }
}
