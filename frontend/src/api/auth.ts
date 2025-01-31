import axios from "axios";

const API_URL = "/api/auth";

export const register = async (email: string, password: string, role: string) => {
    return axios.post(`${API_URL}/register`, { email, password, role });
};

export const login = async (email: string, password: string) => {
    const response = await axios.post(`${API_URL}/login`, { email, password });
    return response.data;
};
