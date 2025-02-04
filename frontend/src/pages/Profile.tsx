import { useEffect, useState } from "react";
import { getUserOrders, cancelOrder, Order } from "../api/orders";
import { useAuthStore } from "../store/authStore";

export default function Profile() {
    const [orders, setOrders] = useState<Order[]>([]);
    const token = useAuthStore((state) => state.token);
    const userEmail = useAuthStore((state) => state.tokenEmail);

    useEffect(() => {
        async function fetchOrders() {
            if (!token || !userEmail) return;
            try {
            const data = await getUserOrders(token,userEmail);
            setOrders(data);

        } catch (error) {
            console.error("Fehler beim Abrufen der Bestellungen:", error);
        }
        }
        fetchOrders();
    }, [token,userEmail]);

    const handleCancelOrder = async (orderId: string) => {
        await cancelOrder(orderId);
        setOrders((prev) => prev.filter((order) => order.id !== orderId));
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">Mein Profil</h2>
            <h3 className="mt-4 text-lg font-semibold">Meine Bestellungen</h3>

            {orders.length === 0 ? (
                <p>Keine Bestellungen gefunden.</p>
            ) : (
                orders.map((order) => (
                    <div key={order.id} className="p-4 border mt-4">
                        <p><strong>Status:</strong> {order.orderStatus}</p>
                        <p><strong>Zahlung:</strong> {order.paymentStatus}</p>
                        <p><strong>Gesamtpreis:</strong> {order.totalPrice} €</p>

                        {order.items.map((item) => (
                            <div key={item.productId}>
                                <img src={item.imageBase64} alt={item.name} className="w-20 h-20" />
                                <p>{item.name} - {item.price} € x {item.quantity}</p>
                            </div>
                        ))}

                        <button onClick={() => handleCancelOrder(order.id)} className="p-2 bg-red-500 text-white rounded mt-2">
                            Bestellung stornieren
                        </button>
                    </div>
                ))
            )}
        </div>
    );
}
