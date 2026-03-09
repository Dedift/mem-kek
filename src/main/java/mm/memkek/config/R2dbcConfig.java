package mm.memkek.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

/**
 * Configuration class for R2DBC database settings.
 * Enables auditing for R2DBC entities (automatic population of created_at, updated_at fields).
 */
@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {
}