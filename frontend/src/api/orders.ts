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
    shippingAddress: {
        street: string;
        city: string;
        postalCode: string;
        country: string;
    };
    paymentStatus: "PAID" | "PENDING";
    orderStatus: "PROCESSING" | "SHIPPED" | "CANCELLED";
};



//  Bestellungen eines Nutzers abrufen
export const getUserOrders = async (userEmail: string): Promise<Order[]> => {
    const response = await axios.get(`${API_URL}/${userEmail}`);
    return response.data;
};

//  Bestellung stornieren (löschen)
export const cancelOrder = async (orderId: string) => {
    return axios.delete(`${API_URL}/${orderId}`);
};

