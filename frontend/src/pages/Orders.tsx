import { useEffect, useState } from "react";
import {getUserOrders, cancelOrder, Order, OrderStatus, requestReturn} from "../api/orders";
import { useAuthStore } from "../store/authStore";


export default function Orders() {
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
        console.log(" API-Call wird ausgeführt für Bestellung:", orderId);

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
        <div className="orders-user-container">
            <h2 className="orders-user-title">📦 Meine Bestellungen</h2>
            {orders.length === 0 ? (
                <p className="no-orders-user">Keine Bestellungen gefunden.</p>
            ) : (
                orders.map((order) => (
                    <div key={order.id} className="order-user-card">
                        <p className="order-user-status"><strong>🛒 Status:</strong> {order.orderStatus}</p>
                        <p className="order-user-status"><strong>💳 Bezahlstatus:</strong> {order.paymentStatus} €</p>
                        <p className="order-user-total"><strong>💰 Gesamtpreis:</strong> {order.totalPrice} €</p>
                        {order.items.map((item) => (
                            <div key={item.productId} className="order-user-item">
                                {Array.isArray(item.images) && item.images.length > 0 ? (
                                    <img src={item.images[0]} alt={item.name} className="order-user-item-image"/>
                                ) : (
                                    <p className="no-image-text-user">Kein Bild verfügbar</p>
                                )}
                                <p className="order-item-info-user">
                                    {item.name} - {item.price} € x {item.quantity}
                                </p>
                            </div>
                        ))}
                        <div className="order-actions-user">
                            <button
                                onClick={() => handleCancelOrder(order.id)}
                                className="cancel-button-user"
                                disabled={order.orderStatus === OrderStatus.CANCELLED || order.orderStatus === OrderStatus.SHIPPED}
                            >
                                {order.orderStatus === OrderStatus.CANCELLED
                                    ? " Bereits storniert"
                                    : order.orderStatus === OrderStatus.SHIPPED
                                        ? " Nicht stornierbar"
                                        : " Bestellung stornieren"}
                            </button>
                            {order.orderStatus === "SHIPPED" && (
                                <button
                                    onClick={() => handleReturnRequest(order.id)}
                                    className="return-button-user"
                                >
                                    🔄 Rückgabe anfordern
                                </button>
                            )}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
}
