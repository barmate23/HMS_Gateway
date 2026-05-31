package com.gateway.SpringCloudGateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	@Autowired
	private RouteValidator validator;


	private String secret;
	private int refreshExpirationDateInMin;
	private int jwtExpirationInMin;

	/*
	@Value("${jwt.secret}")
	public void setSecret(String secret) {
		this.secret = secret;
	}
	*/

	public AuthenticationFilter() {
		super(Config.class);
	}
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);


	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			return chain.filter(exchange);
			/*
			if (exchange.getRequest().getPath().pathWithinApplication().value().equals(Const.PREFIX+Const.JWT_AUTHENTICATION_CONTROLLER) ||
					exchange.getRequest().getPath().pathWithinApplication().value().equals(Const.PREFIX+Const.RESET_PASSWORD) ||
					exchange.getRequest().getPath().pathWithinApplication().value().equals(Const.PREFIX+Const.GENERATE_OTP) ||
					exchange.getRequest().getPath().pathWithinApplication().value().equals(Const.PREFIX+Const.IS_FIRST_LOGIN)) {
				return chain.filter(exchange);
			} else {
				List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
				if (authHeaders == null || authHeaders.isEmpty()) {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				}
				String authHeader = authHeaders.get(0);
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					String token = authHeader.substring(7);



					String username = null;
					Boolean isValid = false;
					// JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
						try {
							isValid = isValidToken(token);
							username = getUsernameFromToken(token);
						} catch (IllegalArgumentException e) {

							exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
							exchange.getResponse().getHeaders().add("Content-Type", "text/plain");
							return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
									.bufferFactory().wrap("Unable to get JWT Token".getBytes())));

						} catch (ExpiredJwtException e) {

							exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
							exchange.getResponse().getHeaders().add("Content-Type", "text/plain");
							return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
									.bufferFactory().wrap("JWT Token has expired".getBytes())));
						}


					if (isValid) {
						Map<String, Object> claims = getAllClaimsFromToken(token);
						Integer userId=null,orgId=null,subOrgId=null;
						String userName=null,subModuleCode=null,subOrgName=null,orgName=null,subOrganizationCode=null;
						if (claims.containsKey("userId"))
							userId = Integer.parseInt(claims.get("userId").toString());

						if (claims.containsKey("userName"))
							userName=claims.get("userName").toString();

						if(claims.containsKey("orgId"))
							orgId = Integer.parseInt(claims.get("orgId").toString());

						if(claims.containsKey("orgName"))
							orgName = claims.get("orgName").toString();

						if(claims.containsKey("subOrgId"))
							subOrgId=Integer.parseInt(claims.get("subOrgId").toString());

						if (claims.containsKey("subOrgName"))
							subOrgName=claims.get("subOrgName").toString();

						if (claims.containsKey("subOrganizationCode"))
							subOrganizationCode=claims.get("subOrganizationCode").toString();

						if (claims.containsKey("subModuleCode"))
							subModuleCode=claims.get("subModuleCode").toString();

						if(userId!=null)
							exchange.getRequest().mutate().header("userId", userId.toString()).build();
						if(userName!=null)
							exchange.getRequest().mutate() .header("userName", userName) .build();
						if(orgId!=null)
							exchange.getRequest().mutate() .header("orgId", orgId.toString()) .build();
						if(orgId!=null)
							exchange.getRequest().mutate() .header("orgName", orgName) .build();
						if (subOrgId!=null)
							exchange.getRequest().mutate() .header("subOrgId", subOrgId.toString()) .build();
						if (subOrgName!=null)
							exchange.getRequest().mutate() .header("subOrgName", subOrgName) .build();
						if (subOrganizationCode!=null)
							exchange.getRequest().mutate() .header("subOrganizationCode", subOrganizationCode) .build();
						if (subModuleCode!=null)
							exchange.getRequest().mutate() .header("subModuleCode", subModuleCode) .build();

						Random random = new Random();
						int randomNumber = random.nextInt(999999 - 100000 + 1) + 100000;
						String logId=("LG"+ LocalDateTime.now().toLocalDate()+randomNumber).trim().replaceAll("-","");
						exchange.getRequest().mutate() .header("logId", logId) .build();
						return chain.filter(exchange);
					}
				}else {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					exchange.getResponse().getHeaders().add("Content-Type", "text/plain");
					return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
							.bufferFactory().wrap("JWT Token does not begin with Bearer String".getBytes())));

				}
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}
			*/
		});
	}

	public static class Config {

	}



	/*
	@Value("${jwt.expirationDateInMin}")
	public void setJwtExpirationInMin(int jwtExpirationInMin) {

		this.jwtExpirationInMin = jwtExpirationInMin;
	}
	*/

	private boolean isValidToken(String token) {

//    final String username = getUsernameFromToken(token);
		return (!isTokenExpired(token));
		// Validate and decode the token using a library like jjwt
		// Return true if the token is valid, false otherwise
	}


	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public Date getExpirationDateFromToken(String token) {

		return getClaimFromToken(token, Claims::getExpiration);
	}

	private static String removePrefix(String original, String prefix) {
		if (original.startsWith(prefix)) {
			return original.substring(prefix.length());
		}
		return original;
	}

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}
}