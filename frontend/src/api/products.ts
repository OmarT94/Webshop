import axios from "axios";


const API_URL = "/api/products"; // Backend-URL
export type Product = {
    id: string;
    name: string;
    description: string;
    price: number;
    stock: number;
    images: string [];
    category: string;
};

export const getProducts = async () => {
    const response = await axios.get(API_URL);
    return response.data.map((product: Product, index: number) => ({
        ...product,
        id: product.id || `temp-${index}`, // Falls ID fehlt, setze eine temporäre
    }));
};


//  Neues Produkt hinzufügen
export const addProduct = async (token: string, product: Omit<Product, "id">): Promise<Product> => {
    console.log("Sende folgendes Produkt an das Backend:", product); // Debugging hinzufügen

    try {
        const response = await axios.post<Product>(API_URL, product, {
            headers: { Authorization: `Bearer ${token}` },
        });

        console.log("Produkt erfolgreich gespeichert:", response.data);
        return response.data;
    } catch (error) {
        console.error("Fehler beim Hinzufügen des Produkts:", error); // Fehlerprotokollierung
        throw error; // Fehler weitergeben
    }
};


//  Produkt löschen
export const deleteProduct = async (token: string, id: string) => {
    return axios.delete(`${API_URL}/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
    });
};

// Produkt bearbeiten
export const updateProduct = async (token: string, id: string, product: Product): Promise<Product> => {
    const response = await axios.put<Product>(`${API_URL}/${id}`, product, {
        headers: { Authorization: `Bearer ${token}` },
    });
    return response.data; //  Gib direkt das `Product` zurück
};

//  Produkte nach Name suchen
export const getProductsByName = async (name: string) => {
    const response = await axios.get(`${API_URL}/search/name`, { params: { name } });
    return response.data;
};

//  Produkte nach Kategorie (Beschreibung) suchen
export const getProductsByDescription = async (description: string) => {
    const response = await axios.get(`${API_URL}/search/description`, { params: { description } });
    return response.data;
};

//  Produkte nach Preisbereich suchen
export const getProductsByPrice = async (minPrice: number, maxPrice: number) => {
    const response = await axios.get(`${API_URL}/search/price`, { params: { minPrice, maxPrice } });
    return response.data;
};

