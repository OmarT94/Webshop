import { useState, useEffect } from "react";
import {
    getAllOrders,
    searchOrdersByEmail,
    searchOrdersByStatus,
    searchOrdersByPaymentStatus,
    updateOrderStatus,
    updatePaymentStatus,
    updateShippingAddress,
    deleteOrder,
    Order,

} from "../api/orders";
import { useAuthStore } from "../store/authStore";

export default function AdminOrders() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [email, setEmail] = useState("");
    const [status, setStatus] = useState("");
    const [paymentStatus, setPaymentStatus] = useState("");
    const isAdmin = useAuthStore((state) => state.isAdmin);

    useEffect(() => {
        async function fetchOrders() {
            if (!isAdmin) return;
            const data = await getAllOrders();
            setOrders(data);
        }
        fetchOrders();
    }, [isAdmin]);

    // 🔍 Suche nach Bestellungen
    const handleEmailSearch = async () => {
        if (!email.trim()) return;
        const data = await searchOrdersByEmail(email);
        setOrders(data);
    };

    const handleStatusSearch = async () => {
        if (!status.trim()) return;
        const data = await searchOrdersByStatus(status);
        setOrders(data);
    };

    const handlePaymentStatusSearch = async () => {
        if (!paymentStatus.trim()) return;
        const data = await searchOrdersByPaymentStatus(paymentStatus);
        setOrders(data);
    };

    const handleStatusChange = async (orderId: string, status: string) => {
        const updatedOrder = await updateOrderStatus(orderId, status);
        setOrders((prev) => prev.map((o) => (o.id === orderId ? updatedOrder : o)));
    };

    const handlePaymentChange = async (orderId: string, paymentStatus: string) => {
        try {
            const response = await updatePaymentStatus(orderId, paymentStatus);
            setOrders((prev) => prev.map((o) => (o.id === orderId ? response : o)));
        } catch (error) {
            console.error("Fehler beim Aktualisieren des Zahlungsstatus:", error);
            alert(" Fehler beim Aktualisieren des Zahlungsstatus. Prüfe die Server-Konfiguration.");
        }
    };

    const handleAddressChange = async (orderId: string, newAddress: any) => {
        const updatedOrder = await updateShippingAddress(orderId, newAddress);
        setOrders((prev) => prev.map((o) => (o.id === orderId ? updatedOrder : o)));
    };

    const handleDeleteOrder = async (orderId: string) => {
        await deleteOrder(orderId);
        setOrders((prev) => prev.filter((o) => o.id !== orderId));
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold">🛒 Bestellungen verwalten & suchen</h2>

            {/* 🔍 Suchfelder */}
            <div className="mb-6 grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                    <input
                        type="text"
                        placeholder="Benutzer-E-Mail"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handleEmailSearch} className="bg-blue-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>

                <div>
                    <input
                        type="text"
                        placeholder="Bestellstatus"
                        value={status}
                        onChange={(e) => setStatus(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handleStatusSearch} className="bg-green-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>

                <div>
                    <input
                        type="text"
                        placeholder="Zahlungsstatus"
                        value={paymentStatus}
                        onChange={(e) => setPaymentStatus(e.target.value)}
                        className="border p-2 w-full"
                    />
                    <button onClick={handlePaymentStatusSearch} className="bg-purple-500 text-white p-2 w-full mt-2">Suchen</button>
                </div>
            </div>

            {/* 🛒 Bestellliste */}
            {orders.length > 0 ? (
                orders.map((order) => (
                    <div key={order.id} className="p-4 border mt-4">
                        <p><strong>Kunde:</strong> {order.userEmail}</p>
                        <p><strong>Status:</strong> {order.orderStatus}</p>
                        <p><strong>Zahlungsstatus:</strong> {order.paymentStatus}</p>
                        <p><strong>Gesamtpreis:</strong> {order.totalPrice} €</p>

                        <label>Bestellstatus:</label>
                        <select
                            onChange={(e) => handleStatusChange(order.id, e.target.value)}
                            value={order.orderStatus}
                        >
                            <option value="PROCESSING">Bearbeitung</option>
                            <option value="SHIPPED">Versendet</option>
                            <option value="CANCELLED">Storniert</option>
                        </select>

                        <label>Zahlungsstatus:</label>
                        <select
                            onChange={(e) => handlePaymentChange(order.id, e.target.value)}
                            value={order.paymentStatus}
                        >
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
                    </div>
                ))
            ) : (
                <p className="text-center text-gray-500">Keine Bestellungen gefunden</p>
            )}
        </div>
    );
}
