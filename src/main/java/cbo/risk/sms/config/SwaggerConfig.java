//package cbo.risk.sms.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//
//@Configuration
//@EnableWebMvc
//public class SwaggerConfig implements WebMvcConfigurer {
//
//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2).select()
//                .apis(RequestHandlerSelectors.basePackage("com.cbo.ca_daily_checklist"))
//                .paths(PathSelectors.regex("/.*"))
//                .build().apiInfo(apiInfoMetaData());
//    }
//
//    private ApiInfo apiInfoMetaData() {
//
//        return new ApiInfoBuilder().title("CA daily Checklist")
//                .description("API Endpoint for CAO")
//                .contact(new Contact("Apps-Team", "https://www.app-cbo.com/", "apps-team@cbo.com"))
//                .license("")
//                .licenseUrl("")
//                .version("1.0.0")
//                .build();
//    }
//
//    @Bean
//    public RestTemplate template() {
//        return new RestTemplate();
//    }
//}