package service.CSFC.CSFC_auth_service.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Parse: rediss://default:password@host:port
        URI uri = URI.create(redisUrl.replace("rediss://", "https://")
                .replace("redis://", "http://"));

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(uri.getHost());
        config.setPort(uri.getPort());

        String userInfo = uri.getUserInfo(); // "default:password"
        if (userInfo != null && userInfo.contains(":")) {
            config.setPassword(userInfo.split(":", 2)[1]);
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl()
                .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(Object.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JacksonJsonRedisSerializer<>(Object.class));
        template.afterPropertiesSet();
        return template;
    }
}
