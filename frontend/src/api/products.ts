import axios from "axios";

const API_URL = "/api/products"; // Backend-URL

export const getProducts = async () => {
    const response = await axios.get(API_URL);
    return response.data;
};
