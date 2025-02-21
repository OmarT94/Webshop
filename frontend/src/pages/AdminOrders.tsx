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

    const handleSearch = async () => {
        let data = [];
        if (email.trim()) {
            data = await searchOrdersByEmail(email);
        } else if (status.trim()) {
            data = await searchOrdersByStatus(status);
        } else if (paymentStatus.trim()) {
            data = await searchOrdersByPaymentStatus(paymentStatus);
        }
        setOrders(data);
    };

    // 🔍 Suche nach Bestellungen
    /*const handleEmailSearch = async () => {
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
    };*/

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
        <div className="admin-orders">
            <h2>🛒 Bestellungen verwalten & suchen</h2>

            {/* 🔍 Suchfelder */}
            <div className="search-container-admin">
                <input type="text" placeholder="Benutzer-E-Mail" value={email} onChange={(e) => setEmail(e.target.value)} />
                <input type="text" placeholder="Bestellstatus" value={status} onChange={(e) => setStatus(e.target.value)} />
                <input type="text" placeholder="Zahlungsstatus" value={paymentStatus} onChange={(e) => setPaymentStatus(e.target.value)} />
                <button onClick={handleSearch}>Suchen</button>
            </div>

            {/*  Bestellliste */}
            {orders.length > 0 ? (
                <div className="orders-grid">
                    {orders.map((order) => (
                        <div key={order.id} className="order-card">
                            <p><strong>Kunde:</strong> {order.userEmail}</p>
                            <p><strong>Status:</strong> {order.orderStatus}</p>
                            <p><strong>Zahlungsstatus:</strong> {order.paymentStatus}</p>
                            <p><strong>Gesamtpreis:</strong> {order.totalPrice} €</p>

                            <label>Bestellstatus:</label>
                            <select onChange={(e) => handleStatusChange(order.id, e.target.value)}
                                    value={order.orderStatus}>
                                <option value="PROCESSING">Bearbeitung</option>
                                <option value="SHIPPED">Versendet</option>
                                <option value="CANCELLED">Storniert</option>
                            </select>

                            <label>Zahlungsstatus:</label>
                            <select onChange={(e) => handlePaymentChange(order.id, e.target.value)}
                                    value={order.paymentStatus}>
                                <option value="PENDING">Ausstehend</option>
                                <option value="PAID">Bezahlt</option>
                                <option value="REFUNDED">Erstattet</option>
                            </select>

                            <label>Lieferadresse:</label>
                            <input
                                type="text"
                                placeholder="Straße"
                                value={order.shippingAddress.street}
                                onChange={(e) => handleAddressChange(order.id, {
                                    ...order.shippingAddress,
                                    street: e.target.value
                                })}
                            />
                            <input
                                type="text"
                                placeholder="Hausnummer"
                                value={order.shippingAddress.houseNumber}
                                onChange={(e) => handleAddressChange(order.id, {
                                    ...order.shippingAddress,
                                    houseNumber: e.target.value
                                })}
                            />
                            <input
                                type="text"
                                placeholder="Stadt"
                                value={order.shippingAddress.city}
                                onChange={(e) => handleAddressChange(order.id, {
                                    ...order.shippingAddress,
                                    city: e.target.value
                                })}
                            />
                            <input
                                type="text"
                                placeholder="PLZ"
                                value={order.shippingAddress.postalCode}
                                onChange={(e) => handleAddressChange(order.id, {
                                    ...order.shippingAddress,
                                    postalCode: e.target.value
                                })}
                            />
                            <input
                                type="text"
                                placeholder="Land"
                                value={order.shippingAddress.country}
                                onChange={(e) => handleAddressChange(order.id, {
                                    ...order.shippingAddress,
                                    country: e.target.value
                                })}
                            />
                            <input
                                type="text"
                                placeholder="Handy"
                                value={order.shippingAddress.telephoneNumber}
                                onChange={(e) => handleAddressChange(order.id, {
                                    ...order.shippingAddress,
                                    telephoneNumber: e.target.value
                                })}
                            />

                            <div className="button-container">
                                <button className="btn-delete" onClick={() => handleDeleteOrder(order.id)}>
                                    Bestellung löschen
                                </button>
                                <button className="btn-save" onClick={() => {
                                    handleStatusChange(order.id, order.orderStatus);
                                    handlePaymentChange(order.id, order.paymentStatus);
                                    handleAddressChange(order.id, order.shippingAddress);
                                }}>
                                    Speichern
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <p className="no-orders">Keine Bestellungen gefunden</p>
            )}
        </div>
    );
}
