package ru.synergy;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected @NotNull SpringApplicationBuilder configure(@NotNull SpringApplicationBuilder application) {
        return application.sources(MusicStreamingPlatformApplication.class);
    }

}
