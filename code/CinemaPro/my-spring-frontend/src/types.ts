export interface User {
    id: string;           // строго string, совпадает с Keycloak sub
    username: string;
    email: string;
    roles: string[];
    token?: string; // всегда массив, даже пустой
}

export type AuthUser = User | null;

export interface UserFormData {
    username: string;
    email: string;
    password: string;
}

export interface ReviewFormData {
    id?: number;
    movieId: number;
    userId?: string;  // <-- string, чтобы совпадало с Keycloak
    rating: number;
    content: string;
}
