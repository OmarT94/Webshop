import { useEffect, useState } from "react";
import {getUserOrders, cancelOrder, Order, OrderStatus} from "../api/orders";
import { useAuthStore } from "../store/authStore";

export default function Profile() {
    const [orders, setOrders] = useState<Order[]>([]);
    const token = useAuthStore((state) => state.token);
    const userEmail = useAuthStore((state) => state.tokenEmail);
    const restoreSession = useAuthStore((state) => state.restoreSession);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        restoreSession();
        setTimeout(() => setLoading(false), 200);
    }, []);

    useEffect(() => {
        async function fetchOrders() {
            if (!token || !userEmail) {
                console.warn("Kein Token oder keine E-Mail vorhanden! Warte auf restoreSession...");
                return;
            }
            try {
                const data = await getUserOrders(userEmail);
                setOrders(data);
            } catch (error) {
                console.error("Fehler beim Laden der Bestellungen:", error);
            }
        }
        if (!loading) fetchOrders();
    }, [token, userEmail, loading]);

    if (loading) return <p>🔄 Lade Daten...</p>;

    if (!token || !userEmail) {
        return <p>Bitte einloggen, um Bestellungen zu sehen.</p>;
    }

    const handleCancelOrder = async (orderId: string) => {
        try {
            const response = await cancelOrder(orderId);
            alert(response); // Erfolg oder Fehlermeldung aus dem Backend anzeigen

            setOrders(orders.map(order =>
                order.id === orderId
                    ? { ...order, orderStatus: OrderStatus.CANCELLED } //  Korrekt: Verwende das `OrderStatus`-Enum
                    : order
            ));
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (error) {
            alert("Bestellung konnte nicht storniert werden!");
        }
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">Meine Bestellungen</h2>
            {orders.length === 0 ? (
                <p>Keine Bestellungen gefunden.</p>
            ) : (
                orders.map((order) => (
                    <div key={order.id} className="p-4 border mt-4">
                        <p><strong>Status:</strong> {order.orderStatus}</p>
                        <p><strong>Gesamtpreis:</strong> {order.totalPrice} €</p>
                        {order.items.map((item) => (
                            <div key={item.productId}>
                                <img src={item.imageBase64} alt={item.name} className="w-20 h-20"/>
                                <p>{item.name} - {item.price} € x {item.quantity}</p>
                            </div>
                        ))}
                        <button
                            onClick={() => handleCancelOrder(order.id)}
                            className="p-2 bg-red-500 text-white rounded mt-2"
                            disabled={order.orderStatus === "CANCELLED" || order.orderStatus === "SHIPPED"}
                        >
                            {order.orderStatus === "CANCELLED" ? "Bereits storniert" :
                                order.orderStatus === "SHIPPED" ? "Nicht stornierbar" :
                                    "Bestellung stornieren"}
                        </button>
                    </div>
                ))
            )}
        </div>
    );
}
