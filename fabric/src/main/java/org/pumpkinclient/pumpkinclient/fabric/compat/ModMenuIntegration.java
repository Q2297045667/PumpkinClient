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
                .setDefaultValue(CompressionAlgorithm.AUTO)
                .setSaveConsumer(algo -> {
                    config.setCompressionAlgorithm((CompressionAlgorithm) algo);
                    Pumpkinclient.resetEffectiveAlgorithm();
                })
                .setEnumNameProvider(anEnum -> Component.translatable("pumpkinclient.algorithm." + ((CompressionAlgorithm) anEnum).getId()))
                .setTooltip(Component.translatable("pumpkinclient.config.restart_required"))
                .build());

        category.addEntry(entryBuilder.startIntSlider(
                        Component.translatable("pumpkinclient.config.compression_level"),
                        config.getCompressionLevel(),
                        0, 9)
                .setDefaultValue(4)
                .setSaveConsumer(config::setCompressionLevel)
                .build());

        category.addEntry(entryBuilder.startIntSlider(
                        Component.translatable("pumpkinclient.config.max_threads"),
                        config.getMaxThreads(),
                        0, 32)
                .setDefaultValue(0)
                .setTooltip(Component.translatable("pumpkinclient.config.max_threads.tooltip"))
                .setSaveConsumer(config::setMaxThreads)
                .build());

        return builder.build();
    }
}
