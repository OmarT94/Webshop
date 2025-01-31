import axios from "axios";

const API_URL = "/api/products"; // Backend-URL

export const getProducts = async () => {
    const response = await axios.get(API_URL);
    return response.data;
};

//  Neues Produkt hinzufügen
export const addProduct = async (name: string, description: string, price: number, token: string) => {
    return axios.post(
        API_URL,
        { name, description, price },
        { headers: { Authorization: `Bearer ${token}` } }
    );
};


//  Produkt löschen
export const deleteProduct = async (id: string, token: string) => {
    return axios.delete(`${API_URL}/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
    });
};

// Produkt bearbeiten
export const updateProduct = async (id: string, name: string, description: string, price: number, token: string) => {
    return axios.put(
        `${API_URL}/${id}`,
        { name, description, price },
        { headers: { Authorization: `Bearer ${token}` } }
    );
};