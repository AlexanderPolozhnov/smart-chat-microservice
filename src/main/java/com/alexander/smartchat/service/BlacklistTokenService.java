package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.JwtResponse;
import com.alexander.smartchat.service.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class BlacklistTokenService {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    public boolean isTokenExistInBlacklist(String token) {
        log.info("Проверка, существует ли токен в черном списке: {}", token);
        if (token == null) {
            log.info("Токен НЕ занесен в черный список");
            return false;
        }

        Claims claims = jwtProvider.getAccessClaims(token);
        String username = claims.getSubject();

        boolean isExists = tokenService.getTokenBlackList(username)
            .map(JwtResponse::accessToken)
            .filter(accessToken -> accessToken.equals(token))
            .isPresent();

        log.info("Токен {} занесен в черный список", isExists ? "" : "НЕ");
        return isExists;
    }

    public void saveTokenBlackList(String bearer) {
        String token = bearer;
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            token = bearer.substring(7);
        }

        Claims claims = jwtProvider.getAccessClaims(token);
        String username = claims.getSubject();
        tokenService.saveTokenBlackList(username, new JwtResponse(token, null));
    }
}
