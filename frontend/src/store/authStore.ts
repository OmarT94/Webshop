import { create } from "zustand";
import {jwtDecode} from "jwt-decode";


type AuthState = {
    token: string | null;
    isAdmin: boolean;
    setToken: (token: string) => void;
    logout: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
    token: localStorage.getItem("token"),
    isAdmin: false,

    setToken: (token) => {
        localStorage.setItem("token", token);
        const decoded: any = jwtDecode(token);
        set({ token, isAdmin: decoded.role === "ROLE_ADMIN" });
    },

    logout: () => {
        localStorage.removeItem("token");
        set({ token: null, isAdmin: false });
    },
}));
