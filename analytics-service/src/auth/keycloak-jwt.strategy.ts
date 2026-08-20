import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { Strategy, ExtractJwt } from 'passport-jwt';
import * as jwksRsa from 'jwks-rsa';

interface KeycloakJwtPayload {
  sub: string;
  realm_access?: { roles: string[] };
}

@Injectable()
export class KeycloakJwtStrategy extends PassportStrategy(Strategy) {
  constructor() {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      secretOrKeyProvider: jwksRsa.passportJwtSecret({
        jwksUri: process.env.KEYCLOAK_JWKS_URI as string,
      }),
      algorithms: ['RS256'],
    });
  }

  validate(payload: KeycloakJwtPayload) {
    const roles: string[] = payload.realm_access?.roles ?? [];
    return { userId: payload.sub, roles };
  }
}
