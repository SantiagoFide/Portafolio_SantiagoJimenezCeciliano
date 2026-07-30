package TechShop.Santiago;

import TechShop.Santiago.domain.Ruta;
import TechShop.Santiago.service.RutaService;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ProjectConfig implements WebMvcConfigurer {

    @Autowired
    private RutaService rutaService;

    /*
     * Configuración de seguridad utilizando las rutas
     * almacenadas en la base de datos.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        var rutas = rutaService.getRutas();

        http.authorizeHttpRequests(requests -> {

            for (Ruta ruta : rutas) {

                if (ruta.isRequiereRol()) {

                    requests
                            .requestMatchers(ruta.getRuta())
                            .hasRole(ruta.getRol().getRol());

                } else {

                    requests
                            .requestMatchers(ruta.getRuta())
                            .permitAll();
                }
            }

            requests.anyRequest().authenticated();
        });

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        http.exceptionHandling(exception -> exception
                .accessDeniedPage("/acceso_denegado")
        );

        http.sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
        );

        return http.build();
    }

    /*
     * Controladores simples para mostrar vistas
     * que no necesitan un controlador adicional.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/")
                .setViewName("index");

        registry.addViewController("/ejemplo2")
                .setViewName("ejemplo2");

        registry.addViewController("/multimedia")
                .setViewName("multimedia");

        registry.addViewController("/iframes")
                .setViewName("iframes");

        registry.addViewController("/login")
                .setViewName("login");

        registry.addViewController("/registro/nuevo")
                .setViewName("registro/nuevo");

        registry.addViewController("/acceso_denegado")
                .setViewName("acceso_denegado");
    }

    /*
     * Resolver de plantillas Thymeleaf.
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver_0() {

        SpringResourceTemplateResolver resolver
                = new SpringResourceTemplateResolver();

        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setOrder(0);
        resolver.setCheckExistence(true);

        return resolver;
    }

    /*
     * Configuración del idioma de la sesión.
     */
    @Bean
    public LocaleResolver localeResolver() {

        var slr = new SessionLocaleResolver();

        slr.setDefaultLocale(Locale.getDefault());
        slr.setLocaleAttributeName("session.current.locale");
        slr.setTimeZoneAttributeName("session.current.timezone");

        return slr;
    }

    /*
     * Permite cambiar el idioma mediante ?lang=es, ?lang=en, etc.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {

        var lci = new LocaleChangeInterceptor();

        lci.setParamName("lang");

        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(localeChangeInterceptor());
    }

    /*
     * Archivo base para los mensajes de internacionalización.
     */
    @Bean
    public MessageSource messageSource() {

        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasenames("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }
}