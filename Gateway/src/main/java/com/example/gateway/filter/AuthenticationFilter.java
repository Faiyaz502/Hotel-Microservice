package com.example.gateway.filter;

import com.example.gateway.exception.InvalidTokenException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import javax.swing.text.StyledEditorKit;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    private final RouteValidator validator;
    private final AuthClient authClient;
    private final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    public AuthenticationFilter(RouteValidator validator,AuthClient authClient){
        super(Config.class);
        this.validator = validator;
        this.authClient = authClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {

            log.info("apply Method Called {} -> ", exchange.getRequest().getHeaders());


            if(validator.isSercured.test(exchange.getRequest())){

                log.info("----Secured API END Point Hit by The Request----");

                if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                    throw new RuntimeException("Missing Authorization header");
                }

                log.info("-----Valid Authentication Header Found---");

                String authHeader = exchange.getRequest()
                        .getHeaders()
                        .getFirst("Authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                    log.info("-----Bearer Not found or Null Auth Header----");

                    throw new RuntimeException("Invalid Authorization header");
                }

                String token = authHeader.substring(7);

                log.info("Bearer Token Found {} -> ",token);



                return authClient.validateToken(token)
                        .flatMap(isValid -> {
                            if (!Boolean.TRUE.equals(isValid)) {
                                return Mono.error(new InvalidTokenException("The Token Is Invalid"));
                            }
                            return chain.filter(exchange); // continue the filter chain
                        });



            }


            return chain.filter(exchange);

        }));
    }






    public static class Config{


 }

}
