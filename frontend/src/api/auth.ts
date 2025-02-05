import axios from "axios";
import {useAuthStore} from "../store/authStore.ts";

const API_URL = "/api/auth";

export const register = async (email: string, password: string, role: string) => {
    return axios.post(`${API_URL}/register`, { email, password, role });
};

export const login = async (email: string, password: string) => {
    try {
        const response = await axios.post(`${API_URL}/login`, { email, password });
        console.log("Login_Response",response.data);
        useAuthStore.getState().setToken(response.data.token); //  Setzt Token
    return response.data;
    } catch (error) {
        console.error("Fehler beim Login:", error);
        throw error;
    }
};
