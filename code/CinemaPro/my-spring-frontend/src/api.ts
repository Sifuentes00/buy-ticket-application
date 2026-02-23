// api.ts
import axios from "axios";
import type { InternalAxiosRequestConfig } from "axios";
import keycloak from "./keycloak";

const api = axios.create({
    baseURL: "http://localhost:8081/api",
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

// --- Интерцептор запросов ---
api.interceptors.request.use(
    async (config: InternalAxiosRequestConfig) => {

        // Проверяем, публичный ли GET
        const isPublicGet =
            config.method === "get" &&
            config.url &&
            PUBLIC_GETS.some((path) => config.url!.startsWith(path));

        if (isPublicGet) {
            if (config.headers) {
                delete config.headers.Authorization;
            }
            return config;
        }

        // --- Для защищённых запросов обновляем токен ---
        if (keycloak.authenticated) {
            try {
                // Попытка обновить токен, если срок жизни < 30 секунд
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

// ===== Универсальные методы =====
export const apiGet = async <T>(url: string) => api.get<T>(url);
export const apiPost = async <T>(url: string, data: any) => api.post<T>(url, data);
export const apiPut = async <T>(url: string, data: any) => api.put<T>(url, data);
export const apiDelete = async <T>(url: string) => api.delete<T>(url);

export default api;
