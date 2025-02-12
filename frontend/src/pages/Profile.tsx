import { useEffect, useState } from "react";
import {getUserOrders, cancelOrder, Order, OrderStatus, requestReturn} from "../api/orders";
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

    const handleReturnRequest = async (orderId: string) => {
        if (!token) return;
        console.log("📡 API-Call wird ausgeführt für Bestellung:", orderId);

        try {
            const success = await requestReturn(token, orderId);
            console.log(" Rückgabe angefordert:", success);

            if (success) {
                setOrders((order) =>
                    order.map((order) =>
                        order.id === orderId
                            ? { ...order, orderStatus: OrderStatus.RETURN_REQUESTED }
                            : order
                    )
                );
            } else {
                console.error(" requestReturn API-Call fehlgeschlagen!");
            }
        } catch (error) {
            console.error(" Fehler bei requestReturn:", error);
        }
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">📦 Meine Bestellungen</h2>
            {orders.length === 0 ? (
                <p>Keine Bestellungen gefunden.</p>
            ) : (
                orders.map((order) => (
                    <div key={order.id} className="p-4 border mt-4">
                        <p><strong>🛒 Status:</strong> {order.orderStatus}</p>
                        <p><strong>💰 Gesamtpreis:</strong> {order.totalPrice} €</p>
                        {order.items.map((item) => (
                            <div key={item.productId} className="flex gap-4 items-center">
                                <img src={item.imageBase64} alt={item.name} className="w-20 h-20"/>
                                <p>{item.name} - {item.price} € x {item.quantity}</p>
                            </div>
                        ))}

                        <div className="mt-4 flex gap-4">
                            <button
                                onClick={() => handleCancelOrder(order.id)}
                                className="p-2 bg-red-500 text-white rounded"
                                disabled={order.orderStatus === OrderStatus.CANCELLED || order.orderStatus === OrderStatus.SHIPPED}
                            >
                                {order.orderStatus === OrderStatus.CANCELLED
                                    ? " Bereits storniert"
                                    : order.orderStatus === OrderStatus.SHIPPED
                                        ? " Nicht stornierbar"
                                        : " Bestellung stornieren"}
                            </button>

                            <button
                                onClick={() => handleReturnRequest(order.id)}
                                className="p-2 bg-blue-500 text-white rounded"
                                disabled={order.orderStatus === OrderStatus.RETURN_REQUESTED || order.orderStatus === OrderStatus.SHIPPED}
                            >
                                {order.orderStatus === OrderStatus.RETURN_REQUESTED
                                    ? " Rückgabe angefordert"
                                    : order.orderStatus === OrderStatus.SHIPPED
                                        ? " Nicht zurückgebbar"
                                        : " Rückgabe anfordern"}
                            </button>
                        </div>
                    </div>
                ))
            )}
        </div>
    );
}
