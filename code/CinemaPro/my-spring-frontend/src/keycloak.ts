import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "http://localhost:8080",
    realm: "cinema-app",
    clientId: "cinema_frontend"
});

export const initKeycloak = () =>
    keycloak.init({
        onLoad: "check-sso",
        pkceMethod: "S256"
    });

export const getAccessToken = async () => {
    if (!keycloak.authenticated) return null;

    try {
        await keycloak.updateToken(30);
        return keycloak.token;
    } catch {
        keycloak.logout();
        return null;
    }
};

export const hasRole = (role: string) => {
    return keycloak.hasRealmRole(role);
};

export default keycloak;
