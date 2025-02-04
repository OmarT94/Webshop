import axios from "axios";


const API_URL = "/api/orders";

export type Order = {
    id: string;
    userEmail: string;
    items: {
        productId: string;
        name: string;
        imageBase64: string;
        quantity: number;
        price: number;
    }[];
    totalPrice: number;
    shippingAddress: Address;
    paymentStatus: "PAID" | "PENDING";
    orderStatus: "PROCESSING" | "SHIPPED" | "CANCELLED";
};

export type Address = {
    street: string;
    city: string;
    postalCode: string;
    country: string;
};



//  Bestellungen eines Nutzers abrufen
export const getUserOrders = async (token: string, userEmail: string): Promise<Order[]> => {
    const response = await axios.get(`${API_URL}/${userEmail}`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
};

//  Bestellung stornieren (löschen)
export const cancelOrder = async (orderId: string) => {
    return axios.delete(`${API_URL}/${orderId}`);
};

//  Alle Bestellungen abrufen (nur für Admins)
export const getAllOrders = async (): Promise<Order[]> => {
    const token = localStorage.getItem("token"); // Token holen
    const response = await axios.get(API_URL, {
        headers: { Authorization: `Bearer ${token}` }, //  Auth-Header setzen
    });
    return response.data;
};

//  Bestellstatus aktualisieren (z.B. Bearbeitung → Versendet)
export const updateOrderStatus = async (orderId: string, status: string): Promise<Order> => {
    const response = await axios.put(`${API_URL}/${orderId}/status?status=${status}`);
    return response.data;
};

//  Zahlungsstatus aktualisieren (Ausstehend → Bezahlt)
export const updatePaymentStatus = async (orderId: string, paymentStatus: string): Promise<Order> => {
    const response = await axios.put(`${API_URL}/${orderId}/payment?paymentStatus=${paymentStatus}`);
    return response.data;
};

//  Lieferadresse aktualisieren
export const updateShippingAddress = async (orderId: string, newAddress: Address ): Promise<Order> => {
    const response = await axios.put(`${API_URL}/${orderId}/address`, newAddress);
    return response.data;
};

//  Bestellung löschen
export const deleteOrder = async (orderId: string): Promise<void> => {
    await axios.delete(`${API_URL}/${orderId}`);
};