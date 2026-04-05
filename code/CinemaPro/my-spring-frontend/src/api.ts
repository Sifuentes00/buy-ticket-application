// api.ts
import axios from "axios";
import type { InternalAxiosRequestConfig } from "axios";
import keycloak from "./keycloak";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api",
    headers: {
        "Content-Type": "application/json",
    },
});

// Пути, для которых Authorization не нужен
const PUBLIC_GETS = [
    "/movies",
    "/theaters",
    "/seats",
    "/tickets",
    "/reviews",
    "/showtimes"
];

api.interceptors.request.use(
    async (config: InternalAxiosRequestConfig) => {

        const isPublicGet =
            config.method === "get" &&
            config.url &&
            PUBLIC_GETS.some((path) => config.url === path);

        if (isPublicGet) {
            if (config.headers) {
                delete config.headers.Authorization;
            }
            return config;
        }

        if (keycloak.authenticated) {
            try {
                await keycloak.updateToken(30);
                if (config.headers) {
                    config.headers.Authorization = `Bearer ${keycloak.token}`;
                }
            } catch (err) {
                console.error("Не удалось обновить токен, будет logout", err);
                keycloak.logout();
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export const apiGet = async <T>(url: string) => api.get<T>(url);
export const apiPost = async <T>(url: string, data: any) => api.post<T>(url, data);
export const apiPut = async <T>(url: string, data: any) => api.put<T>(url, data);
export const apiDelete = async <T>(url: string) => api.delete<T>(url);

export default api;
