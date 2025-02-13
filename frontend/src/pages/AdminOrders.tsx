import { useEffect, useState } from "react";
import {
    getAllOrders,
    updateOrderStatus,
    updatePaymentStatus,
    updateShippingAddress,
    deleteOrder,
    Order,
    approveReturn, PaymentStatus, OrderStatus
} from "../api/orders";
import { useAuthStore } from "../store/authStore";

export default function AdminOrders() {
    const [orders, setOrders] = useState<Order[]>([]);
    const isAdmin = useAuthStore((state) => state.isAdmin);



    useEffect(() => {
        async function fetchOrders() {
            if (!isAdmin) return;
            const data = await getAllOrders();
            setOrders(data);
        }
        fetchOrders();
    }, [isAdmin]);

    const handleStatusChange = async (orderId: string, status: string) => {
        const updatedOrder = await updateOrderStatus(orderId, status);
        setOrders((prev) => prev.map((o) => (o.id === orderId ? updatedOrder : o)));
    };

    const handlePaymentChange = async (orderId: string, paymentStatus: string) => {
        const updatedOrder = await updatePaymentStatus(orderId, paymentStatus);
        setOrders((prev) => prev.map((o) => (o.id === orderId ? updatedOrder : o)));
    };

    const handleAddressChange = async (orderId: string, newAddress: any) => {
        const updatedOrder = await updateShippingAddress(orderId, newAddress);
        setOrders((prev) => prev.map((o) => (o.id === orderId ? updatedOrder : o)));
    };

    const handleDeleteOrder = async (orderId: string) => {
        await deleteOrder(orderId);
        setOrders((prev) => prev.filter((o) => o.id !== orderId));
    };

    //  Angepasste handleApproveReturn-Funktion
    const handleApproveReturn = async (orderId: string) => {
        const token = useAuthStore.getState().token; //  Token korrekt holen

        if (!token) {
            alert("Kein Authentifizierungs-Token verfügbar.");
            return;
        }

        try {
            const success = await approveReturn(token, orderId); //  Token statt isAdmin verwenden
            if (success) {
                setOrders((prev) =>
                    prev.map((o) =>
                        o.id === orderId
                            ? { ...o, orderStatus: OrderStatus.RETURNED, paymentStatus: PaymentStatus.REFUNDED }
                            : o
                    )
                );
                alert("Rückgabe erfolgreich genehmigt und Erstattung ausgelöst.");
            } else {
                alert("Rückgabe konnte nicht genehmigt werden.");
            }
        } catch (error) {
            console.error("Fehler beim Genehmigen der Rückgabe:", error);
            alert("Es gab ein Problem beim Genehmigen der Rückgabe.");
        }
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">Bestellungen verwalten</h2>
            {orders.map((order) => (
                <div key={order.id} className="p-4 border mt-4">
                    <p><strong>Kunde:</strong> {order.userEmail}</p>
                    <p><strong>Status:</strong> {order.orderStatus}</p>
                    <p><strong>Zahlungsstatus:</strong> {order.paymentStatus}</p>
                    <p><strong>Gesamtpreis:</strong> {order.totalPrice} €</p>

                    <label>Bestellstatus:</label>
                    <select
                        onChange={(e) => handleStatusChange(order.id, e.target.value)}
                        value={order.orderStatus}
                        disabled={order.orderStatus === "RETURN_REQUESTED" || order.orderStatus === "RETURNED"}
                    >
                        <option value="PROCESSING">Bearbeitung</option>
                        <option value="SHIPPED">Versendet</option>
                        <option value="CANCELLED">Storniert</option>
                    </select>


                    <label>Zahlungsstatus:</label>
                    <select onChange={(e) => handlePaymentChange(order.id, e.target.value)} value={order.paymentStatus}>
                        <option value="PENDING">Ausstehend</option>
                        <option value="PAID">Bezahlt</option>
                        <option value="REFUNDED">Erstattet</option>
                    </select>

                    <label>Lieferadresse:</label>
                    <input
                        type="text"
                        placeholder="Straße"
                        value={order.shippingAddress.street}
                        onChange={(e) =>
                            handleAddressChange(order.id, {...order.shippingAddress, street: e.target.value})
                        }
                    />
                    <input
                        type="text"
                        placeholder="Stadt"
                        value={order.shippingAddress.city}
                        onChange={(e) =>
                            handleAddressChange(order.id, {...order.shippingAddress, city: e.target.value})
                        }
                    />
                    <input
                        type="text"
                        placeholder="PLZ"
                        value={order.shippingAddress.postalCode}
                        onChange={(e) =>
                            handleAddressChange(order.id, {...order.shippingAddress, postalCode: e.target.value})
                        }
                    />
                    <input
                        type="text"
                        placeholder="Land"
                        value={order.shippingAddress.country}
                        onChange={(e) =>
                            handleAddressChange(order.id, {...order.shippingAddress, country: e.target.value})
                        }
                    />

                    <button onClick={() => handleDeleteOrder(order.id)}
                            className="p-2 bg-red-500 text-white rounded mt-2">
                        Bestellung löschen
                    </button>

                    {order.orderStatus === "RETURN_REQUESTED" && (
                        <button
                            onClick={() => handleApproveReturn(order.id)}
                            className="p-2 bg-green-600 text-white rounded"
                        >
                            ✅ Rückgabe genehmigen & erstatten
                        </button>
                    )}

                </div>
            ))}
        </div>
    );
}
